package ru.bardinpetr.delivery.robot.communication;

import lombok.extern.slf4j.Slf4j;
import ru.bardinpetr.delivery.libs.messages.kafka.consumers.MonitoredKafkaConsumerFactory;
import ru.bardinpetr.delivery.libs.messages.kafka.producers.MonitoredKafkaProducerFactory;
import ru.bardinpetr.delivery.libs.messages.kafka.producers.MonitoredKafkaProducerService;
import ru.bardinpetr.delivery.libs.messages.msg.MessageRequest;
import ru.bardinpetr.delivery.libs.messages.msg.Units;

/**
 * Acts as a HTTP bridge between two message busses with own monitors
 */
@Slf4j
public class MainService {

    public static final String SERVICE_NAME = Units.COMM.toString();

    //    private final MonitoredKafkaConsumerService consumerService;
    private final MonitoredKafkaProducerService producerService;

    private final CommHTTPServerService serverService;

    public MainService(MonitoredKafkaConsumerFactory consumerFactory,
                       MonitoredKafkaProducerFactory producerFactory, CommHTTPServerService serverService) {
//        consumerService = new MonitoredKafkaConsumerServiceBuilder(SERVICE_NAME)
//                .setConsumerFactory(consumerFactory)
//                .build();

        producerService = new MonitoredKafkaProducerService(
                SERVICE_NAME,
                producerFactory
        );
        this.serverService = serverService;

        serverService.setRequestCallback(this::onIncomingMessage);
    }

    private void onIncomingMessage(MessageRequest request) {
        log.info("New HTTP message arrived: {}", request);
        request.setSender(SERVICE_NAME);
        producerService.sendMessage(request);
    }

    public void start() {
//        consumerService.start();
        serverService.start();
        log.info("Started");
    }
}
