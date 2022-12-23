package ru.bardinpetr.delivery.robot.locker;

import ru.bardinpetr.delivery.common.libs.messages.kafka.CommonKafkaConfiguration;
import ru.bardinpetr.delivery.common.libs.messages.kafka.consumers.MonitoredKafkaConsumerFactory;
import ru.bardinpetr.delivery.common.libs.messages.kafka.producers.MonitoredKafkaProducerFactory;
import ru.bardinpetr.delivery.robot.locker.hardware.LockerController;

import static ru.bardinpetr.delivery.robot.locker.MainService.SERVICE_NAME;


public class Main {
    public static void main(String[] args) {
        var kafkaConfig = CommonKafkaConfiguration.getKafkaGlobalParams(
                Configuration.getKafkaURI(),
                SERVICE_NAME
        );
        var consumerFactory = new MonitoredKafkaConsumerFactory(kafkaConfig);
        var producerFactory = new MonitoredKafkaProducerFactory(kafkaConfig);

        var controller = new LockerController();
        var service = new MainService(consumerFactory, producerFactory, controller);
        service.start();
    }
}
