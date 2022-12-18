package ru.bardinpetr.delivery.robot.communication;

import lombok.extern.slf4j.Slf4j;
import ru.bardinpetr.delivery.libs.messages.kafka.consumers.MonitoredKafkaConsumerFactory;
import ru.bardinpetr.delivery.libs.messages.kafka.consumers.MonitoredKafkaConsumerService;
import ru.bardinpetr.delivery.libs.messages.kafka.producers.MonitoredKafkaProducerFactory;
import ru.bardinpetr.delivery.libs.messages.kafka.producers.MonitoredKafkaProducerService;
import ru.bardinpetr.delivery.libs.messages.msg.ForwardableMessageRequest;
import ru.bardinpetr.delivery.libs.messages.msg.MessageRequest;
import ru.bardinpetr.delivery.libs.messages.msg.Units;
import ru.bardinpetr.delivery.robot.communication.client.CommHTTPClientService;
import ru.bardinpetr.delivery.robot.communication.server.CommHTTPServerService;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Acts as a HTTP bridge between two message busses with own monitors
 */
@Slf4j
public class MainService {

    public static final String SERVICE_NAME = Units.COMM.toString();

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

        log.info("New MB message arrived: {}", fRequest);

        fRequest.setRecipient(fRequest.getForwardTo());
        fRequest.setForwarded(true);

        clientService.send(fRequest);
    }

    private void onIncomingMessage(MessageRequest request) {
        log.info("New HTTP message arrived: {}", request);
        if (request.getRecipient().equals(SERVICE_NAME)) return;

        producerService.sendMessage(request);
    }

    public void start() {
        consumerService.start();
        serverService.start();
        log.info("Started");
    }
}
