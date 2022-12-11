package ru.bardinpetr.delivery.robot.positioning_driver;

import ru.bardinpetr.delivery.libs.messages.kafka.consumers.MonitoredKafkaConsumerFactory;
import ru.bardinpetr.delivery.libs.messages.kafka.consumers.MonitoredKafkaConsumerService;
import ru.bardinpetr.delivery.libs.messages.kafka.consumers.MonitoredKafkaConsumerServiceBuilder;
import ru.bardinpetr.delivery.libs.messages.kafka.producers.MonitoredKafkaProducerFactory;
import ru.bardinpetr.delivery.libs.messages.kafka.producers.MonitoredKafkaProducerService;
import ru.bardinpetr.delivery.libs.messages.msg.location.PositionReply;
import ru.bardinpetr.delivery.libs.messages.msg.location.PositionRequest;

/**
 * Provides low-level communication with hardware positioning unit.
 * For each hardware device there should be own HardwarePositioningService module with realization of IPositionService
 */
public class HardwarePositioningService {

    private final MonitoredKafkaConsumerService consumerService;
    private final MonitoredKafkaProducerService producerService;

    private final IPositionService positionService;


    public HardwarePositioningService(
            String serviceName,
            MonitoredKafkaConsumerFactory consumerFactory,
            MonitoredKafkaProducerFactory producerFactory, IPositionService positionService) {

        consumerService = new MonitoredKafkaConsumerServiceBuilder(serviceName)
                .setConsumerFactory(consumerFactory)
                .subscribe(PositionRequest.class, this::replyPosition)
                .build();

        producerService = new MonitoredKafkaProducerService(
                serviceName,
                producerFactory
        );

        this.positionService = positionService;
    }

    private void replyPosition(PositionRequest request) {
        producerService.sendReply(request,
                new PositionReply(
                        positionService.getCurrentPosition(), 1
                ));
    }

    public void start() {
        consumerService.start();
    }
}
