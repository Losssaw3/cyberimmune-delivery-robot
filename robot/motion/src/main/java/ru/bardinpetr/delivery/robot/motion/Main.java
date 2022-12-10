package ru.bardinpetr.delivery.robot.motion;

import ru.bardinpetr.delivery.libs.messages.kafka.CommonKafkaConfiguration;
import ru.bardinpetr.delivery.libs.messages.kafka.consumers.MonitoredKafkaConsumerFactory;
import ru.bardinpetr.delivery.libs.messages.kafka.producers.MonitoredKafkaProducerFactory;
import ru.bardinpetr.delivery.robot.motion.hardware.MotorController;
import ru.bardinpetr.delivery.robot.motion.hardware.MotorRestrictions;

import static ru.bardinpetr.delivery.robot.motion.MainService.SERVICE_NAME;


public class Main {
    public static void main(String[] args) {
        var kafkaConfig = CommonKafkaConfiguration.getKafkaGlobalParams(
                Configuration.getKafkaURI(),
                SERVICE_NAME
        );
        var producerFactory = new MonitoredKafkaProducerFactory(kafkaConfig);
        var consumerFactory = new MonitoredKafkaConsumerFactory(kafkaConfig);

        var motors = new MotorController(new MotorRestrictions(5));

        var service = new MainService(consumerFactory, producerFactory, motors);
        service.start();
    }
}
