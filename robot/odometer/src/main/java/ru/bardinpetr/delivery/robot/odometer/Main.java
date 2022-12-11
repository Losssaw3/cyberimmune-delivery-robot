package ru.bardinpetr.delivery.robot.odometer;

import ru.bardinpetr.delivery.libs.messages.kafka.CommonKafkaConfiguration;
import ru.bardinpetr.delivery.libs.messages.kafka.consumers.MonitoredKafkaConsumerFactory;
import ru.bardinpetr.delivery.libs.messages.kafka.producers.MonitoredKafkaProducerFactory;
import ru.bardinpetr.delivery.robot.odometer.hardware.MockHardwareOdometer;
import ru.bardinpetr.delivery.robot.positioning_driver.HardwarePositioningService;


public class Main {
    public static void main(String[] args) {
        var kafkaConfig = CommonKafkaConfiguration.getKafkaGlobalParams(
                Configuration.getKafkaURI(),
                "odometer"
        );
        var producerFactory = new MonitoredKafkaProducerFactory(kafkaConfig);
        var consumerFactory = new MonitoredKafkaConsumerFactory(kafkaConfig);

        var positionService = new MockHardwareOdometer(
                consumerFactory,
                producerFactory,
                30
        );

        var service = new HardwarePositioningService(
                "odometer",
                consumerFactory, producerFactory,
                positionService);
        service.start();
    }
}
