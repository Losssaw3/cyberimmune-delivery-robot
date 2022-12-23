package ru.bardinpetr.delivery.robot.central;

import ru.bardinpetr.delivery.common.libs.messages.kafka.CommonKafkaConfiguration;
import ru.bardinpetr.delivery.common.libs.messages.kafka.consumers.MonitoredKafkaConsumerFactory;
import ru.bardinpetr.delivery.common.libs.messages.kafka.producers.MonitoredKafkaProducerFactory;
import ru.bardinpetr.delivery.robot.central.services.NavService;
import ru.bardinpetr.delivery.robot.central.services.crypto.CoreCryptoServiceFactory;

import static ru.bardinpetr.delivery.robot.central.MainService.SERVICE_NAME;


public class Main {
    public static void main(String[] args) {
        var kafkaConfig = CommonKafkaConfiguration.getKafkaGlobalParams(
                Configuration.getKafkaURI(),
                SERVICE_NAME
        );
        var producerFactory = new MonitoredKafkaProducerFactory(kafkaConfig);
        var consumerFactory = new MonitoredKafkaConsumerFactory(kafkaConfig);

        var crypto = CoreCryptoServiceFactory.getService(
                Configuration.getSignKeystorePath(), Configuration.getSignKeystorePass()
        );

        var nav = new NavService(consumerFactory, producerFactory);

        var service = new MainService(
                consumerFactory, producerFactory,
                crypto,
                nav
        );
        service.start();
    }
}
