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

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;

@Slf4j
public class BusTestSuite extends Thread {

    private final ConcurrentMessageListenerContainer<String, MessageRequest> container;
    private final Map<Class<? extends MessageRequest>, Map<String, TimedListener>> listeners;
    private final MonitoredKafkaProducerService monitoredProducer;
    private final KafkaTemplate<String, MessageRequest> classicProducer;

    public BusTestSuite(MonitoredKafkaConsumerFactory consumerFactory, MonitoredKafkaProducerFactory producerFactory) {
        listeners = new HashMap<>();

        var containerProperties = new ContainerProperties(Pattern.compile("\\w+"));
        containerProperties.setMessageListener((MessageListener<String, MessageRequest>) this::onMessage);

        container = new ConcurrentMessageListenerContainer<>(consumerFactory, containerProperties);

        monitoredProducer = new MonitoredKafkaProducerService(
                (String) producerFactory.getConfigurationProperties().get(CommonClientConfigs.GROUP_ID_CONFIG),
                producerFactory);
        classicProducer = new KafkaTemplate<>(producerFactory);
    }

    private void onMessage(ConsumerRecord<String, MessageRequest> msg) {
        var msgData = msg.value();
        var time = msg.timestamp();
        var recipient = msgData.getRecipient();

        var futures = listeners.get(msgData.getClass());
        if (futures == null) return;

        var listener = futures.get(recipient);
        if (listener == null || listener.timestamp > time) return;

        log.info("MSG {}->{} of {} @ {} ", msgData.getSender(), recipient, msgData.getActionType(), time);

        listener.future.complete(msgData);
        futures.remove(recipient);
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

        if (!listeners.containsKey(msgType)) listeners.put(msgType, new HashMap<>());
        listeners.get(msgType).put(receiver, new TimedListener(future));

        return future.completeOnTimeout(null, timeout, timeUnit);
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
        private final long timestamp;

        public TimedListener(CompletableFuture<MessageRequest> future) {
            timestamp = Instant.now().getEpochSecond();
            this.future = future;
        }
    }
}
