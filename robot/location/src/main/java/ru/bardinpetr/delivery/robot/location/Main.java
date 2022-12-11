package ru.bardinpetr.delivery.robot.location;

import ru.bardinpetr.delivery.libs.messages.kafka.CommonKafkaConfiguration;
import ru.bardinpetr.delivery.libs.messages.kafka.consumers.MonitoredKafkaConsumerFactory;
import ru.bardinpetr.delivery.libs.messages.kafka.producers.MonitoredKafkaProducerFactory;
import ru.bardinpetr.delivery.libs.messages.msg.Units;
import ru.bardinpetr.delivery.robot.location.aggregator.PositionAggregator;


public class Main {
    public static void main(String[] args) {
        var kafkaConfig = CommonKafkaConfiguration.getKafkaGlobalParams(
                Configuration.getKafkaURI(),
                Units.LOC.toString()
        );
        var producerFactory = new MonitoredKafkaProducerFactory(kafkaConfig);
        var consumerFactory = new MonitoredKafkaConsumerFactory(kafkaConfig);

        var aggregator = new PositionAggregator(
                Configuration.getSpeedTrigger(),
                Configuration.getDistanceTrigger()
        );

        var service = new MainService(
                consumerFactory, producerFactory,
                Configuration.getServices(),
                aggregator,
                Configuration.getUpdateInterval()
        );
        service.start();
    }
}
