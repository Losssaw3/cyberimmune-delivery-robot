package ru.bardinpetr.delivery.robot.motion;

import lombok.extern.slf4j.Slf4j;
import ru.bardinpetr.delivery.libs.messages.kafka.consumers.MonitoredKafkaConsumerFactory;
import ru.bardinpetr.delivery.libs.messages.kafka.consumers.MonitoredKafkaConsumerService;
import ru.bardinpetr.delivery.libs.messages.kafka.consumers.MonitoredKafkaConsumerServiceBuilder;
import ru.bardinpetr.delivery.libs.messages.kafka.producers.MonitoredKafkaProducerFactory;
import ru.bardinpetr.delivery.libs.messages.kafka.producers.MonitoredKafkaProducerService;
import ru.bardinpetr.delivery.libs.messages.msg.Unit;
import ru.bardinpetr.delivery.libs.messages.msg.motion.*;
import ru.bardinpetr.delivery.robot.motion.hardware.MotorController;
import ru.bardinpetr.delivery.robot.motion.hardware.models.MotorParams;

/**
 * Service for controlling motors.
 * Provides interface to control throttle and direction.
 * Automatically calculates position based on input speed data
 */
@Slf4j
public class MainService {

    public static final String SERVICE_NAME = Unit.MOTION.toString();

    private final MonitoredKafkaConsumerService consumerService;
    private final MonitoredKafkaProducerService producerService;

    private final MotorController motorController;

    public MainService(MonitoredKafkaConsumerFactory consumerFactory,
                       MonitoredKafkaProducerFactory producerFactory,
                       MotorController motorController) {
        this.motorController = motorController;

        consumerService = new MonitoredKafkaConsumerServiceBuilder(SERVICE_NAME)
                .setConsumerFactory(consumerFactory)
                .subscribe(SetSpeedRequest.class, this::setSpeed)
                .subscribe(GetRestrictionsRequest.class, this::replyWithRestrictions)
                .subscribe(GetMotionDataRequest.class, this::replyWithMotionData)
                .build();

        producerService = new MonitoredKafkaProducerService(
                SERVICE_NAME,
                producerFactory
        );
    }

    private void setSpeed(SetSpeedRequest request) {
        log.info("New speed: {}", request);
        motorController.set(new MotorParams(request.getSpeed(), request.getAngle()));
    }

    private void replyWithRestrictions(GetRestrictionsRequest request) {
        producerService.sendReply(request,
                new GetRestrictionsReply(motorController.getRestrictions().getMaxSpeed())
        );
    }

    private void replyWithMotionData(GetMotionDataRequest request) {
        var status = motorController.getCurrentTarget();
        var pos = motorController.getReferenceIdealPosition();
        log.info("Sending current state: speed {} angle {} pos {}", status.getSpeed(), status.getDirection(), pos);

        producerService.sendReply(request,
                new GetMotionDataReply(
                        status.getSpeed(),
                        status.getDirection(),
                        pos
                )
        );
    }

    public void start() {
        consumerService.start();
        log.info("Started");
    }
}
