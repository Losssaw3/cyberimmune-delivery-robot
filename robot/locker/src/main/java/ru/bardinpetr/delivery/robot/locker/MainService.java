package ru.bardinpetr.delivery.robot.locker;

import lombok.extern.slf4j.Slf4j;
import ru.bardinpetr.delivery.libs.messages.kafka.consumers.MonitoredKafkaConsumerFactory;
import ru.bardinpetr.delivery.libs.messages.kafka.consumers.MonitoredKafkaConsumerService;
import ru.bardinpetr.delivery.libs.messages.kafka.consumers.MonitoredKafkaConsumerServiceBuilder;
import ru.bardinpetr.delivery.libs.messages.kafka.producers.MonitoredKafkaProducerFactory;
import ru.bardinpetr.delivery.libs.messages.kafka.producers.MonitoredKafkaProducerService;
import ru.bardinpetr.delivery.libs.messages.msg.Units;
import ru.bardinpetr.delivery.libs.messages.msg.locker.LockerDoorClosedRequest;
import ru.bardinpetr.delivery.libs.messages.msg.locker.LockerOpenRequest;
import ru.bardinpetr.delivery.robot.locker.hardware.LockerController;

/**
 * Service for controlling locker hardware.
 * Allows to open locker for specific time via LockerOpenRequest
 */
@Slf4j
public class MainService {

    public static final String SERVICE_NAME = "locker";

    private final MonitoredKafkaConsumerService consumerService;
    private final MonitoredKafkaProducerService producerService;
    private final LockerController controller;

    public MainService(MonitoredKafkaConsumerFactory consumerFactory,
                       MonitoredKafkaProducerFactory producerFactory,
                       LockerController controller) {

        consumerService = new MonitoredKafkaConsumerServiceBuilder(SERVICE_NAME)
                .setConsumerFactory(consumerFactory)
                .subscribe(LockerOpenRequest.class, this::onRequest)
                .build();


        producerService = new MonitoredKafkaProducerService(
                SERVICE_NAME,
                producerFactory
        );

        this.controller = controller;
    }

    private void onRequest(LockerOpenRequest request) {
        this.controller.openLocker(
                5,
                () -> producerService.sendMessage(Units.CCU, new LockerDoorClosedRequest())
        );
    }

    public void start() {
        consumerService.start();
        log.info("Started");
    }
}
