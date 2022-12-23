package ru.bardinpetr.delivery.backend.fms;

import lombok.extern.slf4j.Slf4j;
import ru.bardinpetr.delivery.backend.fms.server.FMSHTTPServer;
import ru.bardinpetr.delivery.common.libs.messages.kafka.CommonKafkaConfiguration;
import ru.bardinpetr.delivery.common.libs.messages.kafka.consumers.MonitoredKafkaConsumerFactory;
import ru.bardinpetr.delivery.common.libs.messages.kafka.producers.MonitoredKafkaProducerFactory;

import static ru.bardinpetr.delivery.backend.fms.MainService.SERVICE_NAME;

@Slf4j
public class Main {

    public static void main(String[] args) {
        var kafkaConfig = CommonKafkaConfiguration.getKafkaGlobalParams(
                Configuration.getKafkaURI(),
                SERVICE_NAME
        );
        var producerFactory = new MonitoredKafkaProducerFactory(kafkaConfig);
        var consumerFactory = new MonitoredKafkaConsumerFactory(kafkaConfig);

        var serverService = new FMSHTTPServer(9040);
        var service = new MainService(
                consumerFactory, producerFactory,
                serverService
        );
        service.start();
    }
}
