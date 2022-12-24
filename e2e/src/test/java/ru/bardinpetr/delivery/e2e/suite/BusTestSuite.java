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

import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;

@Slf4j
public class BusTestSuite extends Thread {
    private final ConcurrentMessageListenerContainer<String, MessageRequest> container;
    private final Map<String, TopicHistory> history;

    private final MonitoredKafkaProducerService monitoredProducer;
    private final KafkaTemplate<String, MessageRequest> classicProducer;

    private final long startupTimestamp;
    private final long backTimeSearchMillis;

    public BusTestSuite(MonitoredKafkaConsumerFactory consumerFactory, MonitoredKafkaProducerFactory producerFactory, long backTimeSearchMillis) {
        this.backTimeSearchMillis = backTimeSearchMillis;
        history = new HashMap<>();

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

    /**
     * Each received message is stored in TopicHistory for each topic (receiver + msg type).
     */
    @SuppressWarnings("unchecked")
    private void onMessage(ConsumerRecord<String, MessageRequest> msg) {
        var topic = msg.topic();
        var time = msg.timestamp();

        if (time < startupTimestamp) return;

        history.putIfAbsent(topic, new TopicHistory<>());
        var hist = history.get(topic);
        hist.put(msg);
    }

    /**
     * Deletes all messages received from cache
     */
    public void flushHistory() {
        history.clear();
        log.info("history after clean: {}", history);
    }

    /**
     * Get reusable listener {@link HistoryListener} for messages of type {@code msgType} arriving for {@code receiver}.
     *
     * @param msgType  message type to listen for
     * @param receiver receiving unit. topic looks like $receiver_$msgType.actionType
     * @param timeout  timeout for underlying message waiting. returns null if exceeded.
     * @param timeUnit timeout unit
     */
    @SuppressWarnings("unchecked")
    public <T extends MessageRequest> HistoryListener<T> awaitMessages(Class<T> msgType,
                                                                       Unit receiver,
                                                                       int timeout, TimeUnit timeUnit) {
        var topic = MessageRequest.getTargetTopic(msgType, receiver.toString());

        history.putIfAbsent(topic, new TopicHistory<T>());
        var hist = history.get(topic);
        return new HistoryListener<T>(hist, timeout, timeUnit);
    }

    public void produceUnmonitored(MessageRequest request) {
        request.setRequestId(UUID.randomUUID().toString());
        classicProducer.send(request.getTargetTopic(), request.getActionType(), request);
    }

    @Override
    public void run() {
        container.start();
    }
}
