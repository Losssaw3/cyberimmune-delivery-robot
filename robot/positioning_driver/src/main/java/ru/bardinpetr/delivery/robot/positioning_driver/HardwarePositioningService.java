package ru.bardinpetr.delivery.robot.positioning_driver;

import ru.bardinpetr.delivery.common.libs.messages.kafka.consumers.MonitoredKafkaConsumerFactory;
import ru.bardinpetr.delivery.common.libs.messages.kafka.consumers.MonitoredKafkaConsumerService;
import ru.bardinpetr.delivery.common.libs.messages.kafka.consumers.MonitoredKafkaConsumerServiceBuilder;
import ru.bardinpetr.delivery.common.libs.messages.kafka.producers.MonitoredKafkaProducerFactory;
import ru.bardinpetr.delivery.common.libs.messages.kafka.producers.MonitoredKafkaProducerService;
import ru.bardinpetr.delivery.common.libs.messages.msg.location.PositionReply;
import ru.bardinpetr.delivery.common.libs.messages.msg.location.PositionRequest;

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
        var pos = positionService.getCurrentPosition();
        if (pos == null) return;
        producerService.sendReply(request,
                new PositionReply(
                        pos, 1
                ));
    }

    public void start() {
        positionService.start();
        consumerService.start();
    }
}
