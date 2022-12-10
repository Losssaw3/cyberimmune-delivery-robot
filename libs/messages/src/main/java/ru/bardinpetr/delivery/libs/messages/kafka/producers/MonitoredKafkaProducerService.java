package ru.bardinpetr.delivery.libs.messages.kafka.producers;

import org.springframework.kafka.core.KafkaTemplate;
import ru.bardinpetr.delivery.libs.messages.MessageRequest;
import ru.bardinpetr.delivery.libs.messages.ReplyableMessageRequest;

import java.util.concurrent.ExecutionException;

public class MonitoredKafkaProducerService {
    public static final String MONITOR_TOPIC = "monitor";

    private final String senderName;
    private final KafkaTemplate<String, MessageRequest> kafkaTemplate;

    public MonitoredKafkaProducerService(String senderName, MonitoredKafkaProducerFactory producerFactory) {
        this.senderName = senderName;
        this.kafkaTemplate = new KafkaTemplate<>(producerFactory);
    }

    public boolean sendMessage(MessageRequest request) {
        System.out.printf("[SEND] to %s msg %s\n", request.getRecipient(), request);
        try {
            kafkaTemplate.send(
                            MONITOR_TOPIC,
                            request.getClass().getCanonicalName(),
                            request)
                    .get();
            return true;
        } catch (InterruptedException | ExecutionException e) {
            return false;
        }
    }

    public boolean sendMessage(String recipient, MessageRequest request) {
        request.setSender(senderName);
        request.setRecipient(recipient);
        return sendMessage(request);
    }

    public boolean sendReply(ReplyableMessageRequest request, MessageRequest reply) {
        reply.setRequestId(request.getRequestId());
        reply.setSender(request.getRecipient());
        reply.setRecipient(request.getSender());
        return sendMessage(reply);
    }
}
