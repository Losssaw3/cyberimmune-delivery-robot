package ru.bardinpetr.delivery.robot.sensors;

import ru.bardinpetr.delivery.libs.messages.kafka.CommonKafkaConfiguration;
import ru.bardinpetr.delivery.libs.messages.kafka.consumers.MonitoredKafkaConsumerFactory;
import ru.bardinpetr.delivery.libs.messages.kafka.producers.MonitoredKafkaProducerFactory;
import ru.bardinpetr.delivery.robot.sensors.hardware.PositioningHumanDetector;

import static ru.bardinpetr.delivery.robot.sensors.MainService.SERVICE_NAME;


public class Main {
    public static void main(String[] args) {
        var kafkaConfig = CommonKafkaConfiguration.getKafkaGlobalParams(
                Configuration.getKafkaURI(),
                SERVICE_NAME
        );
        var producerFactory = new MonitoredKafkaProducerFactory(kafkaConfig);
        var consumerFactory = new MonitoredKafkaConsumerFactory(kafkaConfig);

        var detector = new PositioningHumanDetector(consumerFactory, producerFactory, 15);
        var service = new MainService(consumerFactory, producerFactory, detector);
        service.start();
    }
}
