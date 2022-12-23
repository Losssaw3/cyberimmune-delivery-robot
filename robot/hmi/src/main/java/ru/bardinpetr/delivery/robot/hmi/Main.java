package ru.bardinpetr.delivery.robot.hmi;

import ru.bardinpetr.delivery.common.libs.messages.kafka.CommonKafkaConfiguration;
import ru.bardinpetr.delivery.common.libs.messages.kafka.consumers.MonitoredKafkaConsumerFactory;
import ru.bardinpetr.delivery.common.libs.messages.kafka.producers.MonitoredKafkaProducerFactory;
import ru.bardinpetr.delivery.robot.hmi.interactors.http.HTTPUserInteractor;

import static ru.bardinpetr.delivery.robot.hmi.MainService.SERVICE_NAME;

public class Main {
    public static void main(String[] args) {
        var kafkaConfig = CommonKafkaConfiguration.getKafkaGlobalParams(
                Configuration.getKafkaURI(),
                SERVICE_NAME
        );
        var producerFactory = new MonitoredKafkaProducerFactory(kafkaConfig);
        var consumerFactory = new MonitoredKafkaConsumerFactory(kafkaConfig);

        var server = new HTTPUserInteractor(8888);

        var service = new MainService(consumerFactory, producerFactory, server);
        service.start();
    }
}
