package ru.bardinpetr.delivery.libs.messages.kafka.producers;

import org.springframework.kafka.core.KafkaTemplate;
import ru.bardinpetr.delivery.libs.messages.MessageRequest;

import java.util.concurrent.ExecutionException;

public class MonitoredKafkaProducerService {
    public static final String MONITOR_TOPIC = "monitor";

    private final String senderName;
    private final KafkaTemplate<String, MessageRequest> kafkaTemplate;

    public MonitoredKafkaProducerService(String senderName, MonitoredKafkaProducerFactory producerFactory) {
        this.senderName = senderName;
        this.kafkaTemplate = new KafkaTemplate<>(producerFactory);
    }

    public boolean sendMessage(String recipient, MessageRequest request) {
        request.setSender(senderName);
        request.setRecipient(recipient);
        try {
            kafkaTemplate.send(MONITOR_TOPIC, request).get();
            return true;
        } catch (InterruptedException | ExecutionException e) {
            return false;
        }
    }
}
