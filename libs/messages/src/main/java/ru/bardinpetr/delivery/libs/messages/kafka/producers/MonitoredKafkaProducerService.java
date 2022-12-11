package ru.bardinpetr.delivery.libs.messages.kafka.producers;

import org.springframework.kafka.core.KafkaTemplate;
import ru.bardinpetr.delivery.libs.messages.msg.MessageRequest;
import ru.bardinpetr.delivery.libs.messages.msg.ReplyableMessageRequest;

import java.util.UUID;
import java.util.concurrent.ExecutionException;

public class MonitoredKafkaProducerService {
    public static final String MONITOR_TOPIC = "monitor";

    private final String senderName;
    private final KafkaTemplate<String, MessageRequest> kafkaTemplate;

    public MonitoredKafkaProducerService(String senderName, MonitoredKafkaProducerFactory producerFactory) {
        this.senderName = senderName;
        this.kafkaTemplate = new KafkaTemplate<>(producerFactory);
    }

    /**
     * Sends prepared request to kafka;
     *
     * @param request MessageRequest with all required fields set
     * @return UUID of message if sent successfully, otherwise null
     */
    public String sendMessage(MessageRequest request) {
        if (request.getRecipient().isEmpty() || request.getSender().isEmpty() || request.getRequestId().isEmpty())
            throw new IllegalArgumentException("Message must have sender and destination");

        System.out.printf("[SEND] to %s msg %s\n", request.getRecipient(), request);
        try {
            kafkaTemplate.send(
                            MONITOR_TOPIC,
                            request.getActionType(),
                            request)
                    .get();
            return request.getRequestId();
        } catch (InterruptedException | ExecutionException e) {
            return null;
        }
    }

    /**
     * Sends request to kafka and sets sender and recipient;
     * Adds request ID as random UUID;
     *
     * @param request MessageRequest with all required fields set
     * @return UUID of message if sent successfully, otherwise null
     */
    public String sendMessage(String recipient, MessageRequest request) {
        request.setRequestId(UUID.randomUUID().toString());
        request.setSender(senderName);
        request.setRecipient(recipient);
        return sendMessage(request);
    }

    /**
     * Sends request to kafka as reply to other request what means that their IDs are equal;
     *
     * @param request MessageRequest with all required fields set
     * @return UUID of message if sent successfully, otherwise null
     */
    public String sendReply(ReplyableMessageRequest request, MessageRequest reply) {
        reply.asReplyTo(request);
        return sendMessage(reply);
    }
}
