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
import ru.bardinpetr.delivery.libs.messages.msg.ccu.DeliveryTask;
import ru.bardinpetr.delivery.libs.messages.msg.ccu.NewTaskRequest;
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

    public static final String SERVICE_NAME = Units.CCU.toString();

    private final MonitoredKafkaConsumerService consumerService;
    private final MonitoredKafkaProducerService producerService;

    private final CoreCryptoService cryptoService;
    private final NavService navService;
    private final Position home;
    private DeliveryStatus currentStatus = DeliveryStatus.IDLE;
    private boolean isHumanDetected = false;
    private DeliveryTask currentTask;

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
    }

    private void onNewTask(NewTaskRequest request) {
        log.info("New task: {}; current status: {}", request, currentStatus);
        if (currentStatus != DeliveryStatus.IDLE) {
            sendFMSStatus(DeliveryStatus.ENDED_ERR, "Already running");
            return;
        }

        var task = cryptoService.decodeTask(request.getDeliveryTask());
        if (task == null) {
            sendFMSStatus(DeliveryStatus.ENDED_ERR, "Could not verify task crypto");
            log.error("Failed loading task due to crypto");
            return;
        }

        setStatus(DeliveryStatus.RUNNING);

        currentTask = task;

        log.info("Initiating navigation to {}", task.getPosition());
        navService.setTarget(task.getPosition());
        navService.run(this::onArrived);

        configHumanDetection(task.getPosition());
    }

    private void configHumanDetection(Position position) {
        producerService.sendMessage(
                Units.SENSORS,
                new HumanDetectionConfigRequest(position, 20)
        );
    }

    private void onArrived() {
        setStatus(DeliveryStatus.ARRIVED_TO_CUSTOMER);
        log.info("Arrived at destination");
    }

    private void onPinEntered(PINEnterRequest request) {
        if (currentStatus != DeliveryStatus.ARRIVED_TO_CUSTOMER || !isHumanDetected)
            return;

        var pin = request.getPin();
        log.info("Got PIN: {}", pin);

        if (!pin.equals(currentTask.getPin())) {
            log.error("Invalid PIN");
            sendFMSStatus(DeliveryStatus.PIN_INVALID);
        }

        sendFMSStatus(DeliveryStatus.LOCKER_OPENED);

        log.info("Opening locker...");
        producerService.sendMessage(
                Units.LOCKER,
                new LockerOpenRequest()
        );
    }

    private void onStartReturnHome(LockerDoorClosedRequest request) {
        log.info("Locker closed. Returning home");
        setStatus(DeliveryStatus.RETURNING);

        navService.setTarget(home);
        navService.run(() -> {
            log.info("Returned successfully");

            sendFMSStatus(DeliveryStatus.ENDED_OK);
            setStatus(DeliveryStatus.IDLE);
        });
    }

    private void setStatus(DeliveryStatus status) {
        currentStatus = status;
        sendFMSStatus(status);
    }

    private void sendFMSStatus(DeliveryStatus status) {
        sendFMSStatus(status, status.toString());
    }

    private void sendFMSStatus(DeliveryStatus status, String text) {
        producerService.sendVia(
                Units.COMM.toString(),
                Units.FMS.toString(),
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
