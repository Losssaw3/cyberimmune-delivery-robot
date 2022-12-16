package ru.bardinpetr.delivery.robot.central.services;

import lombok.extern.slf4j.Slf4j;
import ru.bardinpetr.delivery.libs.messages.kafka.consumers.MonitoredKafkaConsumerFactory;
import ru.bardinpetr.delivery.libs.messages.kafka.consumers.MonitoredKafkaConsumerService;
import ru.bardinpetr.delivery.libs.messages.kafka.consumers.MonitoredKafkaConsumerServiceBuilder;
import ru.bardinpetr.delivery.libs.messages.kafka.producers.MonitoredKafkaProducerFactory;
import ru.bardinpetr.delivery.libs.messages.kafka.producers.MonitoredKafkaProducerService;
import ru.bardinpetr.delivery.libs.messages.msg.location.Position;

import static ru.bardinpetr.delivery.robot.central.MainService.SERVICE_NAME;

@Slf4j
public class NavService {

    private final MonitoredKafkaConsumerService consumerService;
    private final MonitoredKafkaProducerService producerService;

    public NavService(MonitoredKafkaConsumerFactory consumerFactory,
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
        log.info("Navigation started");
    }

    public void setTarget(Position position) {

    }

    public void run(Runnable onArrived) {

    }
}
