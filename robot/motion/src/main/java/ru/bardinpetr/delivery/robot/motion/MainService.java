package ru.bardinpetr.delivery.robot.motion;

import ru.bardinpetr.delivery.libs.messages.kafka.consumers.MonitoredKafkaConsumerFactory;
import ru.bardinpetr.delivery.libs.messages.kafka.consumers.MonitoredKafkaConsumerService;
import ru.bardinpetr.delivery.libs.messages.kafka.consumers.MonitoredKafkaConsumerServiceBuilder;
import ru.bardinpetr.delivery.libs.messages.kafka.producers.MonitoredKafkaProducerFactory;
import ru.bardinpetr.delivery.libs.messages.kafka.producers.MonitoredKafkaProducerService;
import ru.bardinpetr.delivery.libs.messages.msg.motion.*;
import ru.bardinpetr.delivery.robot.motion.hardware.MotorController;
import ru.bardinpetr.delivery.robot.motion.hardware.models.MotorParams;

/**
 *
 */
public class MainService {

    public static final String SERVICE_NAME = "motion";

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
        motorController.set(new MotorParams(request.getSpeed(), request.getAngle()));
    }

    private void replyWithRestrictions(GetRestrictionsRequest request) {
        producerService.sendReply(request,
                new GetRestrictionsReply(motorController.getRestrictions().getMaxSpeed())
        );
    }

    private void replyWithMotionData(GetMotionDataRequest request) {
        var status = motorController.getCurrentTarget();
        producerService.sendReply(request,
                new GetMotionDataReply(
                        status.getSpeed(),
                        status.getDirection(),
                        motorController.getReferenceIdealPosition()
                )
        );
    }

    public void start() {
        consumerService.start();
    }
}
