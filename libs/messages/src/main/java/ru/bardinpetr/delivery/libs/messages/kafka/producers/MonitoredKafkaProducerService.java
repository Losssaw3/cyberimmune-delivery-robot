package ru.bardinpetr.delivery.libs.messages.kafka.producers;

import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;
import org.springframework.kafka.support.SendResult;
import org.springframework.util.concurrent.ListenableFuture;
import ru.bardinpetr.delivery.libs.messages.MessageRequest;

public class MonitoredKafkaProducerService {
    public static final String MONITOR_TOPIC = "monitor";

    private final String senderName;
    private final KafkaTemplate<String, MessageRequest> kafkaTemplate;

    public MonitoredKafkaProducerService(String senderName, ProducerFactory<String, MessageRequest> producerFactory) {
        this.senderName = senderName;
        this.kafkaTemplate = new KafkaTemplate<>(producerFactory);
    }

    public ListenableFuture<SendResult<String, MessageRequest>> sendMessage(String recipient, MessageRequest request) {
        request.setSender(senderName);
        request.setRecipient(recipient);
        return kafkaTemplate.send(MONITOR_TOPIC, request);
    }
}
