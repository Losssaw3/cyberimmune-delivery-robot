package ru.bardinpetr.delivery.common.communication;

import lombok.extern.slf4j.Slf4j;
import ru.bardinpetr.delivery.common.communication.client.CommHTTPClientService;
import ru.bardinpetr.delivery.common.communication.server.CommHTTPServerService;
import ru.bardinpetr.delivery.common.libs.messages.kafka.consumers.MonitoredKafkaConsumerFactory;
import ru.bardinpetr.delivery.common.libs.messages.kafka.consumers.MonitoredKafkaConsumerService;
import ru.bardinpetr.delivery.common.libs.messages.kafka.producers.MonitoredKafkaProducerFactory;
import ru.bardinpetr.delivery.common.libs.messages.kafka.producers.MonitoredKafkaProducerService;
import ru.bardinpetr.delivery.common.libs.messages.msg.ForwardableMessageRequest;
import ru.bardinpetr.delivery.common.libs.messages.msg.MessageRequest;
import ru.bardinpetr.delivery.common.libs.messages.msg.Unit;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Acts as a HTTP bridge between two message busses with own monitors
 */
@Slf4j
public class MainService {

    public static final String SERVICE_NAME = Unit.COMM.toString();

    private final MonitoredKafkaConsumerService consumerService;
    private final MonitoredKafkaProducerService producerService;

    private final CommHTTPServerService serverService;
    private final CommHTTPClientService clientService;

    public MainService(MonitoredKafkaConsumerFactory consumerFactory,
                       MonitoredKafkaProducerFactory producerFactory,
                       CommHTTPServerService serverService,
                       CommHTTPClientService clientService,
                       List<Class<? extends MessageRequest>> supportedMessages) {
        this.serverService = serverService;
        this.clientService = clientService;

        consumerService = new MonitoredKafkaConsumerService(
                consumerFactory,
                supportedMessages.stream()
                        .collect(Collectors.toMap(
                                i -> MessageRequest.getTargetTopic(i, SERVICE_NAME),
                                i -> this::onOutgoingMessage
                        ))
        );

        producerService = new MonitoredKafkaProducerService(
                SERVICE_NAME,
                producerFactory
        );

        serverService.setRequestCallback(this::onIncomingMessage);
    }

    /**
     * Forward message from local MB to receiving side via HTTP;
     * Rewrite message recipient according to forwardTo field.
     *
     * @param request Request to be sent via bridge. Should be ForwardableMessageRequest
     */
    private void onOutgoingMessage(MessageRequest request) {
        if (!ForwardableMessageRequest.class.isAssignableFrom(request.getClass())) {
            log.error("Got message without forwarding marker: {}", request);
            return;
        }
        var fRequest = (ForwardableMessageRequest) request;

        log.debug("New MB message arrived: {}", fRequest);
        log.info("New MB message {} ({}->{}) ID{}",
                request.getActionType(), request.getSender(), request.getRecipient(), request.getRequestId());

        fRequest.setRecipient(fRequest.getForwardTo());
        fRequest.setForwarded(true);

        var targetURL = fRequest.getRecipientBridgeURL();

        boolean result;
        if (targetURL != null && !targetURL.isEmpty()) {
            log.info("Passing message ID{} to {}", fRequest.getRequestId(), targetURL);
            result = clientService.send(targetURL, fRequest);
        } else {
            log.info("Passing message ID{} to default endpoint", fRequest.getRequestId());
            result = clientService.send(fRequest);
        }

        log.info("HTTP sending ID{} Ok?={}", fRequest.getRequestId(), result);
    }

    private void onIncomingMessage(String physicalSender, MessageRequest request) {
        log.debug("New HTTP message arrived: {}", request);
        log.info("New HTTP message from {}: {} ({}->{}) ID{}", physicalSender,
                request.getActionType(), request.getSender(), request.getRecipient(), request.getRequestId());

        if (request.getRecipient().equals(SERVICE_NAME)) return;

        var real = (ForwardableMessageRequest) request;
        real.setSenderBridgeURL(physicalSender);

        producerService.sendMessage(real);
    }

    public void start() {
        consumerService.start();
        serverService.start();
        log.info("Started");
    }
}
