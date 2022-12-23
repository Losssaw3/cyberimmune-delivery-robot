package ru.bardinpetr.delivery.robot.sensors;

import lombok.extern.slf4j.Slf4j;
import ru.bardinpetr.delivery.libs.messages.kafka.consumers.MonitoredKafkaConsumerFactory;
import ru.bardinpetr.delivery.libs.messages.kafka.consumers.MonitoredKafkaConsumerService;
import ru.bardinpetr.delivery.libs.messages.kafka.consumers.MonitoredKafkaConsumerServiceBuilder;
import ru.bardinpetr.delivery.libs.messages.kafka.producers.MonitoredKafkaProducerFactory;
import ru.bardinpetr.delivery.libs.messages.kafka.producers.MonitoredKafkaProducerService;
import ru.bardinpetr.delivery.libs.messages.msg.Unit;
import ru.bardinpetr.delivery.libs.messages.msg.sensors.HumanDetectedRequest;
import ru.bardinpetr.delivery.libs.messages.msg.sensors.HumanDetectionConfigRequest;
import ru.bardinpetr.delivery.robot.sensors.hardware.PositioningHumanDetector;

/**
 * Provides events from sensors to central unit.
 * The main purpose is to detect human when robot arrived.
 * Detectors are pluggable with IHumanDetector
 */
@Slf4j
public class MainService {

    public static final String SERVICE_NAME = Unit.SENSORS.toString();

    private final MonitoredKafkaConsumerService consumerService;
    private final MonitoredKafkaProducerService producerService;
    private final PositioningHumanDetector detector;


    public MainService(MonitoredKafkaConsumerFactory consumerFactory,
                       MonitoredKafkaProducerFactory producerFactory,
                       PositioningHumanDetector detector) {

        consumerService = new MonitoredKafkaConsumerServiceBuilder(SERVICE_NAME)
                .setConsumerFactory(consumerFactory)
                .subscribe(HumanDetectionConfigRequest.class, this::onConfig)
                .build();

        producerService = new MonitoredKafkaProducerService(
                SERVICE_NAME,
                producerFactory
        );

        this.detector = detector;
        this.detector.setCallback(() -> {
            log.info("Human detected");
            producerService.sendMessage(Unit.CCU.toString(), new HumanDetectedRequest());
                }
        );
    }

    private void onConfig(HumanDetectionConfigRequest request) {
        log.info("Set configuration: {} D{}", request.getLocation(), request.getAccuracy());
        detector.config(request);
    }

    public void start() {
        detector.start();
        consumerService.start();
        log.info("Started");
    }
}
