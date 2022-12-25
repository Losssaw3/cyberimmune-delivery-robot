package ru.bardinpetr.delivery.robot.odometer;

import ru.bardinpetr.delivery.common.libs.messages.kafka.CommonKafkaConfiguration;
import ru.bardinpetr.delivery.common.libs.messages.kafka.consumers.MonitoredKafkaConsumerFactory;
import ru.bardinpetr.delivery.common.libs.messages.kafka.producers.MonitoredKafkaProducerFactory;
import ru.bardinpetr.delivery.robot.odometer.hardware.MockHardwareOdometer;
import ru.bardinpetr.delivery.robot.positioning_driver.HardwarePositioningService;


public class Main {
    public static void main(String[] args) {
        var name = Configuration.getName();

        var kafkaConfig = CommonKafkaConfiguration.getKafkaGlobalParams(
                Configuration.getKafkaURI(),
                name
        );
        var producerFactory = new MonitoredKafkaProducerFactory(kafkaConfig);
        var consumerFactory = new MonitoredKafkaConsumerFactory(kafkaConfig);

        var positionService = new MockHardwareOdometer(
                name, consumerFactory, producerFactory
        );

        var service = new HardwarePositioningService(
                name, consumerFactory, producerFactory,
                positionService
        );

        service.start();
    }
}
