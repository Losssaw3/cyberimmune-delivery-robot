package ru.bardinpetr.delivery.robot.locker;

import ru.bardinpetr.delivery.libs.messages.kafka.consumers.MonitoredKafkaConsumerFactory;
import ru.bardinpetr.delivery.libs.messages.kafka.consumers.MonitoredKafkaConsumerService;
import ru.bardinpetr.delivery.libs.messages.kafka.consumers.MonitoredKafkaConsumerServiceBuilder;
import ru.bardinpetr.delivery.libs.messages.models.locker.LockerOpenRequest;
import ru.bardinpetr.delivery.robot.locker.hardware.LockerController;

/**
 * Service for controlling locker hardware.
 * Allows to open locker for specific time via LockerOpenRequest
 */
public class MainService {

    public static final String SERVICE_NAME = "locker";

    private final MonitoredKafkaConsumerService consumerService;
    private final LockerController controller;

    public MainService(MonitoredKafkaConsumerFactory consumerFactory, LockerController controller) {

        consumerService = new MonitoredKafkaConsumerServiceBuilder(SERVICE_NAME)
                .setConsumerFactory(consumerFactory)
                .subscribe(LockerOpenRequest.class, this::onRequest)
                .build();

        this.controller = controller;
    }

    private void onRequest(LockerOpenRequest request) {
        this.controller.openLocker(5);
    }

    public void start() {
        consumerService.start();
    }
}
