package ru.bardinpetr.delivery.common.libs.messages.kafka.producers;

import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import ru.bardinpetr.delivery.common.libs.messages.msg.ForwardableMessageRequest;
import ru.bardinpetr.delivery.common.libs.messages.msg.MessageRequest;
import ru.bardinpetr.delivery.common.libs.messages.msg.ReplyableMessageRequest;
import ru.bardinpetr.delivery.common.libs.messages.msg.Unit;

import java.util.UUID;
import java.util.concurrent.ExecutionException;


@Slf4j
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

        log.debug("[SEND] to {} msg {}", request.getRecipient(), request);
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

    public String sendMessage(Unit recipient, MessageRequest request) {
        return sendMessage(recipient.toString(), request);
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

    /**
     * Send message to destination via another service working with ForwardableMessageRequests
     */
    public String sendVia(String viaService, String targetService, ForwardableMessageRequest request) {
        request.setForwardTo(targetService);
        return sendMessage(viaService, request);
    }

    /**
     * Send message to destination via Communication service
     */
    public String sendVia(String viaService, String targetURL, String targetService, ForwardableMessageRequest request) {
        request.setForwardTo(targetService);
        request.setRecipientBridgeURL(targetURL);
        return sendMessage(viaService, request);
    }
}
