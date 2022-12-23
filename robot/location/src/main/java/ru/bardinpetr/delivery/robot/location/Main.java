package ru.bardinpetr.delivery.robot.location;

import ru.bardinpetr.delivery.common.libs.messages.kafka.CommonKafkaConfiguration;
import ru.bardinpetr.delivery.common.libs.messages.kafka.consumers.MonitoredKafkaConsumerFactory;
import ru.bardinpetr.delivery.common.libs.messages.kafka.producers.MonitoredKafkaProducerFactory;
import ru.bardinpetr.delivery.common.libs.messages.msg.Unit;
import ru.bardinpetr.delivery.robot.location.aggregator.PositionAggregator;


public class Main {
    public static void main(String[] args) {
        var kafkaConfig = CommonKafkaConfiguration.getKafkaGlobalParams(
                Configuration.getKafkaURI(),
                Unit.LOC.toString()
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
