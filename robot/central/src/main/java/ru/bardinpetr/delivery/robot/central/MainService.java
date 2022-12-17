package ru.bardinpetr.delivery.robot.central;

import lombok.extern.slf4j.Slf4j;
import ru.bardinpetr.delivery.libs.messages.kafka.consumers.MonitoredKafkaConsumerFactory;
import ru.bardinpetr.delivery.libs.messages.kafka.consumers.MonitoredKafkaConsumerService;
import ru.bardinpetr.delivery.libs.messages.kafka.consumers.MonitoredKafkaConsumerServiceBuilder;
import ru.bardinpetr.delivery.libs.messages.kafka.producers.MonitoredKafkaProducerFactory;
import ru.bardinpetr.delivery.libs.messages.kafka.producers.MonitoredKafkaProducerService;
import ru.bardinpetr.delivery.libs.messages.msg.Units;
import ru.bardinpetr.delivery.libs.messages.msg.ccu.*;
import ru.bardinpetr.delivery.libs.messages.msg.hmi.PINEnterRequest;
import ru.bardinpetr.delivery.libs.messages.msg.location.Position;
import ru.bardinpetr.delivery.libs.messages.msg.locker.LockerDoorClosedRequest;
import ru.bardinpetr.delivery.libs.messages.msg.locker.LockerOpenRequest;
import ru.bardinpetr.delivery.libs.messages.msg.sensors.HumanDetectedRequest;
import ru.bardinpetr.delivery.libs.messages.msg.sensors.HumanDetectionConfigRequest;
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
    private boolean isHumanDetected = false;
    private DeliveryTask currentTask;

    private final Position home;


    public MainService(MonitoredKafkaConsumerFactory consumerFactory,
                       MonitoredKafkaProducerFactory producerFactory,
                       CoreCryptoService cryptoService, NavService navService) {
        this.cryptoService = cryptoService;
        this.navService = navService;

        consumerService = new MonitoredKafkaConsumerServiceBuilder(SERVICE_NAME)
                .setConsumerFactory(consumerFactory)
                .subscribe(NewTaskRequest.class, this::onNewTask)
                .subscribe(PINEnterRequest.class, this::onPinEntered)
                .subscribe(LockerDoorClosedRequest.class, this::onStartReturnHome)
                .subscribe(
                        HumanDetectedRequest.class,
                        msg -> setHumanDetected(currentStatus == DeliveryStatus.ARRIVED_TO_CUSTOMER || isHumanDetected)
                )
                .build();

        producerService = new MonitoredKafkaProducerService(
                SERVICE_NAME,
                producerFactory
        );

        home = new Position(0, 0);

        var t = new InputDeliveryTask("XPwjzZNxX9wlQ0hChx4vgDNkM8Bf3As/Nkr40Y/pG3ZEy15dkc+vxikUZ9c/E8lxk4jFiR986MPR8xmvHYPnD4JQFaTmuUP2HF6PIEyOzI1kEjcvP4RRRBc6Tqhtw/h/ATRk0IzuuMEvwiSzhpuOMkhh6o9gK9Ri+6dCcqApXDMmEZ2rIfnH3UMGjxBdretUUI7lK/kuMY96gOwyOanxS6ydY+36lwp4JYTngKGG+7jhdcn/neFlxZ/FttroPuH2d0vXW8s8Jf/K4boB1zgD7mC6MNgLfCj+vhJUx6XfYHBlvt9NcoMK0mATDo3LG4ms//pfzmlIX+Ozl4hRRrznOA==",
                new DeliveryTask(new Position(100.0, 200.0), "K7cnHXi1JYHebm7Taalf6wAAAAFJPp2UcDJtoa912uzIbAyD"));
        onNewTask(new NewTaskRequest(t));
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

        currentTask = task;

        log.info("Initiating navigation to {}", task.getPosition());
        navService.setTarget(task.getPosition());
        navService.run(this::onArrived);

        producerService.sendMessage(
                Units.SENSORS,
                new HumanDetectionConfigRequest(task.getPosition(), 20)
        );
    }

    private void onArrived() {
        currentStatus = DeliveryStatus.ARRIVED_TO_CUSTOMER;
        log.info("Arrived at destination");
    }

    private void onPinEntered(PINEnterRequest request) {
        if (currentStatus != DeliveryStatus.ARRIVED_TO_CUSTOMER || !isHumanDetected)
            return;

        var pin = request.getPin();
        log.info("Got PIN: {}", pin);

        if (!pin.equals(currentTask.getPin())) {
            log.error("Invalid PIN");
        }

        log.info("Opening locker...");
        producerService.sendMessage(
                Units.LOCKER,
                new LockerOpenRequest()
        );
    }

    private void onStartReturnHome(LockerDoorClosedRequest request) {
        log.info("Locker closed. Returning home");
        currentStatus = DeliveryStatus.RETURNING;

        navService.setTarget(home);
        navService.run(() -> {
            log.info("Returned successfully");

            currentStatus = DeliveryStatus.IDLE;
            replyStatus("Finished", DeliveryStatus.ENDED_OK);
        });
    }

    private void replyStatus(String text, DeliveryStatus status) {
        producerService.sendMessage(
                Units.COMM,
                new DeliveryStatusRequest(text, status)
        );
    }

    private void setHumanDetected(boolean state) {
        if (isHumanDetected == state) return;
        log.info("Human detection state changed to {}", state);
        isHumanDetected = state;
    }

    public void start() {
        log.info("Started");
        consumerService.start();
        navService.start();
    }
}
