package ru.bardinpetr.delivery.robot.hmi;

import ru.bardinpetr.delivery.libs.messages.kafka.consumers.MonitoredKafkaConsumerFactory;
import ru.bardinpetr.delivery.libs.messages.kafka.consumers.MonitoredKafkaConsumerService;
import ru.bardinpetr.delivery.libs.messages.kafka.consumers.MonitoredKafkaConsumerServiceBuilder;
import ru.bardinpetr.delivery.libs.messages.kafka.producers.MonitoredKafkaProducerFactory;
import ru.bardinpetr.delivery.libs.messages.kafka.producers.MonitoredKafkaProducerService;
import ru.bardinpetr.delivery.libs.messages.msg.Units;
import ru.bardinpetr.delivery.libs.messages.msg.hmi.PINEnterRequest;
import ru.bardinpetr.delivery.libs.messages.msg.hmi.PINValidationResponse;
import ru.bardinpetr.delivery.robot.hmi.interactors.IUserInteractor;

import java.time.LocalTime;

/**
 * Provides a user interface to enter PIN code. Sends PIN code to central control unit.
 */
public class MainService {
    public static final String SERVICE_NAME = Units.HMI.toString();

    public static final int MAX_TRY_COUNT = 3;
    public static final int RETRY_DELAY_SEC = 5;
    private final IUserInteractor userInterface;
    private final MonitoredKafkaConsumerService consumerService;
    private final MonitoredKafkaProducerService producerService;

    private int tries = 0;
    private LocalTime lastTryTime;

    public MainService(MonitoredKafkaConsumerFactory consumerFactory,
                       MonitoredKafkaProducerFactory producerFactory,
                       IUserInteractor userInterface) {
        this.userInterface = userInterface;

        consumerService = new MonitoredKafkaConsumerServiceBuilder(SERVICE_NAME)
                .setConsumerFactory(consumerFactory)
                .subscribe(PINValidationResponse.class, this::onRequest)
                .build();

        producerService = new MonitoredKafkaProducerService(
                SERVICE_NAME,
                producerFactory
        );
    }

    private void onRequest(PINValidationResponse request) {
    }

    public void start() {
        userInterface.registerPINHandler(this::handler);
        userInterface.start();
    }

    private boolean checkCount() {
        var now = LocalTime.now();
        boolean res = true;
        if (++tries > MAX_TRY_COUNT) {
            if (now.minusSeconds(RETRY_DELAY_SEC).isAfter(lastTryTime))
                tries = 0;
            else
                res = false;
        }

        lastTryTime = now;
        return res;
    }

    private boolean verify(String pin) {
        producerService.sendMessage(Units.CCU, new PINEnterRequest(pin));
        return true;
    }

    private String handler(String pin) {
        if (!checkCount())
            return String.format("You have %s tries each %s seconds", MAX_TRY_COUNT, RETRY_DELAY_SEC);

        verify(pin);
        return "sent";
    }
}
