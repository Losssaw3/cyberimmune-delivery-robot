package ru.bardinpetr.delivery.robot.communication;

import lombok.extern.slf4j.Slf4j;
import ru.bardinpetr.delivery.libs.messages.kafka.consumers.MonitoredKafkaConsumerFactory;
import ru.bardinpetr.delivery.libs.messages.kafka.consumers.MonitoredKafkaConsumerService;
import ru.bardinpetr.delivery.libs.messages.kafka.consumers.MonitoredKafkaConsumerServiceBuilder;
import ru.bardinpetr.delivery.libs.messages.kafka.producers.MonitoredKafkaProducerFactory;
import ru.bardinpetr.delivery.libs.messages.kafka.producers.MonitoredKafkaProducerService;


@Slf4j
public class MainService {

    public static final String SERVICE_NAME = "central";

    private final MonitoredKafkaConsumerService consumerService;
    private final MonitoredKafkaProducerService producerService;

    public MainService(MonitoredKafkaConsumerFactory consumerFactory,
                       MonitoredKafkaProducerFactory producerFactory) {
        consumerService = new MonitoredKafkaConsumerServiceBuilder(SERVICE_NAME)
                .setConsumerFactory(consumerFactory)
                .build();

        producerService = new MonitoredKafkaProducerService(
                SERVICE_NAME,
                producerFactory
        );
    }

    public void start() {
        consumerService.start();
        log.info("Started");
    }
}
