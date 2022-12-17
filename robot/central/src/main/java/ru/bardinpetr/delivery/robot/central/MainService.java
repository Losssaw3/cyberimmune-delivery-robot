package ru.bardinpetr.delivery.robot.central;

import lombok.extern.slf4j.Slf4j;
import ru.bardinpetr.delivery.libs.messages.kafka.consumers.MonitoredKafkaConsumerFactory;
import ru.bardinpetr.delivery.libs.messages.kafka.consumers.MonitoredKafkaConsumerService;
import ru.bardinpetr.delivery.libs.messages.kafka.consumers.MonitoredKafkaConsumerServiceBuilder;
import ru.bardinpetr.delivery.libs.messages.kafka.producers.MonitoredKafkaProducerFactory;
import ru.bardinpetr.delivery.libs.messages.kafka.producers.MonitoredKafkaProducerService;
import ru.bardinpetr.delivery.libs.messages.msg.Units;
import ru.bardinpetr.delivery.libs.messages.msg.ccu.DeliveryStatus;
import ru.bardinpetr.delivery.libs.messages.msg.ccu.DeliveryStatusRequest;
import ru.bardinpetr.delivery.libs.messages.msg.ccu.NewTaskRequest;
import ru.bardinpetr.delivery.robot.central.services.NavService;
import ru.bardinpetr.delivery.robot.central.services.crypto.CoreCryptoService;

@Slf4j
public class MainService {

    public static final String SERVICE_NAME = "central";

    private final MonitoredKafkaConsumerService consumerService;
    private final MonitoredKafkaProducerService producerService;

    private final CoreCryptoService cryptoService;
    private final NavService navService;

    private DeliveryStatus currentStatus = DeliveryStatus.IDLE;

    public MainService(MonitoredKafkaConsumerFactory consumerFactory,
                       MonitoredKafkaProducerFactory producerFactory,
                       CoreCryptoService cryptoService, NavService navService) {
        this.cryptoService = cryptoService;
        this.navService = navService;

        consumerService = new MonitoredKafkaConsumerServiceBuilder(SERVICE_NAME)
                .setConsumerFactory(consumerFactory)
                .subscribe(NewTaskRequest.class, this::onNewTask)
                .build();

        producerService = new MonitoredKafkaProducerService(
                SERVICE_NAME,
                producerFactory
        );
    }

    private void onNewTask(NewTaskRequest request) {
        log.info("New task: {}; current status: {}", request, currentStatus);
        if (currentStatus != DeliveryStatus.IDLE) {
            replyStatus("Already running", DeliveryStatus.ENDED_ERR);
            return;
        }

        var task = cryptoService.decodeTask(request.getDeliveryTask());
        if (task == null) {
            replyStatus("Could not verify task crypto", DeliveryStatus.ENDED_ERR);
            log.error("Failed loading task due to crypto");
            return;
        }

        currentStatus = DeliveryStatus.RUNNING;
        replyStatus("OK", DeliveryStatus.RUNNING);

        log.info("Initiating navigation to {}", task.getPosition());
        navService.setTarget(task.getPosition());
        navService.run(this::onArrived);
    }

    private void onArrived() {
        currentStatus = DeliveryStatus.ARRIVED_TO_CUSTOMER;
        log.info("Arrived at destination");

        
    }


    private void replyStatus(String text, DeliveryStatus status) {
        producerService.sendMessage(
                Units.COMM,
                new DeliveryStatusRequest(text, status)
        );
    }

    public void start() {
        consumerService.start();
        navService.start();
        log.info("Started");
    }
}
