package ru.bardinpetr.delivery.robot.central;

import lombok.extern.slf4j.Slf4j;
import ru.bardinpetr.delivery.common.libs.messages.kafka.consumers.MonitoredKafkaConsumerFactory;
import ru.bardinpetr.delivery.common.libs.messages.kafka.consumers.MonitoredKafkaConsumerService;
import ru.bardinpetr.delivery.common.libs.messages.kafka.consumers.MonitoredKafkaConsumerServiceBuilder;
import ru.bardinpetr.delivery.common.libs.messages.kafka.producers.MonitoredKafkaProducerFactory;
import ru.bardinpetr.delivery.common.libs.messages.kafka.producers.MonitoredKafkaProducerService;
import ru.bardinpetr.delivery.common.libs.messages.msg.MessageRequest;
import ru.bardinpetr.delivery.common.libs.messages.msg.Unit;
import ru.bardinpetr.delivery.common.libs.messages.msg.ccu.DeliveryStatus;
import ru.bardinpetr.delivery.common.libs.messages.msg.ccu.DeliveryStatusRequest;
import ru.bardinpetr.delivery.common.libs.messages.msg.ccu.DeliveryTask;
import ru.bardinpetr.delivery.common.libs.messages.msg.ccu.NewTaskRequest;
import ru.bardinpetr.delivery.common.libs.messages.msg.hmi.PINEnterRequest;
import ru.bardinpetr.delivery.common.libs.messages.msg.hmi.PINValidationResponse;
import ru.bardinpetr.delivery.common.libs.messages.msg.location.Position;
import ru.bardinpetr.delivery.common.libs.messages.msg.locker.LockerDoorClosedRequest;
import ru.bardinpetr.delivery.common.libs.messages.msg.locker.LockerOpenRequest;
import ru.bardinpetr.delivery.common.libs.messages.msg.sensors.HumanDetectedRequest;
import ru.bardinpetr.delivery.common.libs.messages.msg.sensors.HumanDetectionConfigRequest;
import ru.bardinpetr.delivery.robot.central.services.NavService;
import ru.bardinpetr.delivery.robot.central.services.crypto.CoreCryptoService;

@Slf4j
public class MainService {

    public static final String SERVICE_NAME = Unit.CCU.toString();
    private static final int PIN_MAX_ENTER_COUNT = 5;
    private final MonitoredKafkaConsumerService consumerService;
    private final MonitoredKafkaProducerService producerService;

    private final CoreCryptoService cryptoService;
    private final NavService navService;
    private final Position home;
    private DeliveryStatus currentStatus = DeliveryStatus.IDLE;
    private DeliveryTask currentTask;

    private int pinEnterCount = 0;

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
                .subscribe(HumanDetectedRequest.class, this::onHumanDetected)
                .build();

        producerService = new MonitoredKafkaProducerService(
                SERVICE_NAME,
                producerFactory
        );

        home = new Position(0, 0);
    }

    /**
     * Triggered by receiving new task from FMS.
     * Validates the task and if it can be taken now.
     * If ok - starts navigation to the point
     */
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
        pinEnterCount = 0;

        log.info("Initiating navigation to {}", task.getPosition());
        navService.setTarget(task.getPosition());
        navService.run(() -> {
            setStatus(DeliveryStatus.ARRIVED_TO_CUSTOMER);
            log.info("Arrived at destination");
        });

        configHumanDetection(task.getPosition());
    }

    /**
     * Triggered by HMI Unit with password to check
     */
    private void onPinEntered(PINEnterRequest request) {
        boolean res = validatePin(request.getPin());
        producerService.sendReply(
                request,
                new PINValidationResponse(res)
        );
    }

    /**
     * Function for pin code validation. Checks for current statuses.
     * Opens locker if pin is valid.
     * Triggered by HMI via onPinEntered.
     *
     * @return if pin is valid
     */
    private boolean validatePin(String pin) {
        if (currentStatus != DeliveryStatus.HUMAN_DETECTED)
            return false;

        log.info("Got PIN: {}", pin);

        boolean valid = pin.equals(currentTask.getPin());

        if (!valid) {
            log.error("Invalid PIN");
            pinEnterCount++;
            if (pinEnterCount > PIN_MAX_ENTER_COUNT) {
                setStatus(DeliveryStatus.PIN_INVALID);
                onStartReturnHome(null);
            } else {
                sendFMSStatus(DeliveryStatus.PIN_INVALID);
            }
            return false;
        }

        setStatus(DeliveryStatus.LOCKER_OPENED);

        log.info("Opening locker...");
        producerService.sendMessage(
                Unit.LOCKER,
                new LockerOpenRequest()
        );

        return true;
    }

    /**
     * Triggered when customer is detected with Sensors Unit.
     * If navigation is finished (robot arrived to customer),
     * then enable PIN code checking (and therefore locker opening)
     */
    private void onHumanDetected(MessageRequest request) {
        if (currentStatus == DeliveryStatus.ARRIVED_TO_CUSTOMER)
            setStatus(DeliveryStatus.HUMAN_DETECTED);
        log.info("Human detected");
    }


    /**
     * When door of robot locker is closed by the user, robot should return to the warehouse (starts nav service).
     * Triggered by Locker Unit.
     */
    private void onStartReturnHome(LockerDoorClosedRequest ignored) {
        if (currentStatus != DeliveryStatus.LOCKER_OPENED && currentStatus != DeliveryStatus.PIN_INVALID) return;

        log.info("Locker closed. Returning home");
        setStatus(DeliveryStatus.RETURNING);

        navService.setTarget(home);
        navService.run(() -> {
            log.info("Returned successfully");

            sendFMSStatus(DeliveryStatus.ENDED_OK);
            setStatus(DeliveryStatus.IDLE);
        });
    }

    /**
     * As human detection works by comparing locations, we need to send Sensors Unit a target position
     *
     * @param position destination position
     */
    private void configHumanDetection(Position position) {
        producerService.sendMessage(
                Unit.SENSORS,
                new HumanDetectionConfigRequest(position, 20)
        );
    }

    private void setStatus(DeliveryStatus status) {
        log.warn("Status changed to {}", status);
        currentStatus = status;
        sendFMSStatus(status);
    }

    private void sendFMSStatus(DeliveryStatus status) {
        sendFMSStatus(status, status.toString());
    }

    /**
     * Notify FMS of status change via CS
     *
     * @param status new status
     * @param text   optional textual description
     */
    private void sendFMSStatus(DeliveryStatus status, String text) {
        producerService.sendVia(
                Unit.COMM.toString(),
                Unit.FMS.toString(),
                new DeliveryStatusRequest(text, status)
        );
    }

    public void start() {
        log.info("Started");
        consumerService.start();
        navService.start();
    }
}
