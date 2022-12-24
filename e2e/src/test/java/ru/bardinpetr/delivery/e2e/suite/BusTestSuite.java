package ru.bardinpetr.delivery.e2e.suite;

import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.CommonClientConfigs;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.listener.ConcurrentMessageListenerContainer;
import org.springframework.kafka.listener.ContainerProperties;
import org.springframework.kafka.listener.MessageListener;
import ru.bardinpetr.delivery.common.libs.messages.kafka.consumers.MonitoredKafkaConsumerFactory;
import ru.bardinpetr.delivery.common.libs.messages.kafka.producers.MonitoredKafkaProducerFactory;
import ru.bardinpetr.delivery.common.libs.messages.kafka.producers.MonitoredKafkaProducerService;
import ru.bardinpetr.delivery.common.libs.messages.msg.MessageRequest;
import ru.bardinpetr.delivery.common.libs.messages.msg.Unit;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;

@Slf4j
public class BusTestSuite extends Thread {
    private final ConcurrentMessageListenerContainer<String, MessageRequest> container;
    private final Map<String, TimedListener> listeners;
    private final Map<String, Queue<ConsumerRecord<String, MessageRequest>>> lastMessages;

    private final MonitoredKafkaProducerService monitoredProducer;
    private final KafkaTemplate<String, MessageRequest> classicProducer;

    private final long startupTimestamp;
    private final long backTimeSearchMillis;

    public BusTestSuite(MonitoredKafkaConsumerFactory consumerFactory, MonitoredKafkaProducerFactory producerFactory, long backTimeSearchMillis) {
        this.backTimeSearchMillis = backTimeSearchMillis;
        listeners = new HashMap<>();
        lastMessages = new HashMap<>();

        var containerProperties = new ContainerProperties(Pattern.compile("\\w+"));
        containerProperties.setMessageListener((MessageListener<String, MessageRequest>) this::onMessage);

        container = new ConcurrentMessageListenerContainer<>(consumerFactory, containerProperties);

        monitoredProducer = new MonitoredKafkaProducerService(
                (String) producerFactory.getConfigurationProperties().get(CommonClientConfigs.GROUP_ID_CONFIG),
                producerFactory);
        classicProducer = new KafkaTemplate<>(producerFactory);

        startupTimestamp = millis();
    }

    public static long millis() {
        return Calendar.getInstance().getTimeInMillis();
    }

    private void onMessage(ConsumerRecord<String, MessageRequest> msg) {
        var msgData = msg.value();
        var topic = msg.topic();
        var time = msg.timestamp();

        if (time < startupTimestamp) return;

        var listener = listeners.get(topic);
        if (listener == null || listener.fired) {
            if ((millis() - time) < backTimeSearchMillis) {
                lastMessages
                        .putIfAbsent(topic, new ArrayDeque<>())
                        .add(msg);
            }
            return;
        }

        log.info("MSG {}->{} of {} @ {}", msgData.getSender(), topic, msgData.getActionType(), time);

        listener.future.complete(msgData);
    }

    private void clearHistory(Queue<ConsumerRecord<String, MessageRequest>> q) {
        while (!q.isEmpty() && (millis() - Objects.requireNonNull(q.peek()).timestamp()) > backTimeSearchMillis)
            q.remove();
    }

    /**
     * Wait specified time for specific message to be sent to selected topic by receiver
     *
     * @param msgType  type of message to listen for
     * @param receiver topic unit prefix (unitName_actionName in kafka)
     * @param timeout  timeout value. returns null if timeout
     * @return CompletableFuture resolving to null if timeout else to received message
     */
    public CompletableFuture<MessageRequest> awaitMessage(Class<? extends MessageRequest> msgType,
                                                          String receiver,
                                                          int timeout, TimeUnit timeUnit) {
        var future = new CompletableFuture<MessageRequest>();

        var topic = (receiver.equals(Unit.MONITOR.toString()) ?
                Unit.MONITOR.toString() : MessageRequest.getTargetTopic(msgType, receiver));

        var history = lastMessages.get(topic);
        if (history != null) {
            clearHistory(history);
            if (!history.isEmpty()) {
                log.info("Found already received message {} fired before consumer registed", topic);
                return CompletableFuture.completedFuture(history.remove().value());
            }
        }

        listeners.put(topic, new TimedListener(future));

        return future
                .completeOnTimeout(null, timeout, timeUnit)
                .thenApply(res -> {
                    listeners.get(topic).fired = true;
                    return res;
                });
    }

    public void produceUnmonitored(MessageRequest request) {
        classicProducer.send(request.getTargetTopic(), request.getActionType(), request);
    }

    @Override
    public void run() {
        container.start();
    }

    private static class TimedListener {
        private final CompletableFuture<MessageRequest> future;
        private boolean fired;

        public TimedListener(CompletableFuture<MessageRequest> future) {
            this.future = future;
            this.fired = false;
        }
    }
}
