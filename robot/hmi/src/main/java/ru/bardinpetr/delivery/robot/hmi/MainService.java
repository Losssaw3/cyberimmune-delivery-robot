package ru.bardinpetr.delivery.robot.hmi;

import lombok.extern.slf4j.Slf4j;
import ru.bardinpetr.delivery.libs.crypto.PINService;
import ru.bardinpetr.delivery.libs.messages.kafka.consumers.MonitoredKafkaConsumerFactory;
import ru.bardinpetr.delivery.libs.messages.kafka.consumers.MonitoredKafkaRequesterService;
import ru.bardinpetr.delivery.libs.messages.kafka.producers.MonitoredKafkaProducerFactory;
import ru.bardinpetr.delivery.libs.messages.msg.Unit;
import ru.bardinpetr.delivery.libs.messages.msg.hmi.PINEnterRequest;
import ru.bardinpetr.delivery.libs.messages.msg.hmi.PINValidationResponse;
import ru.bardinpetr.delivery.robot.hmi.interactors.IUserInteractor;

import java.time.LocalTime;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * Provides a user interface to enter PIN code. Sends PIN code to central control unit.
 */
@Slf4j
public class MainService extends Thread {
    public static final String SERVICE_NAME = Unit.HMI.toString();

    public static final int MAX_TRY_COUNT = 3;
    public static final int RETRY_DELAY_SEC = 5;
    private final IUserInteractor userInterface;
    private final MonitoredKafkaRequesterService consumerService;

    private int tries = 0;
    private LocalTime lastTryTime;

    public MainService(MonitoredKafkaConsumerFactory consumerFactory,
                       MonitoredKafkaProducerFactory producerFactory,
                       IUserInteractor userInterface) {
        this.userInterface = userInterface;

        consumerService = new MonitoredKafkaRequesterService(
                SERVICE_NAME,
                List.of(PINValidationResponse.class),
                producerFactory, consumerFactory
        );
    }

    @Override
    public void run() {
        consumerService.start();

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
        PINValidationResponse response;
        try {
            response = (PINValidationResponse)
                    consumerService
                            .request(Unit.CCU.toString(), new PINEnterRequest(pin))
                            .get(5, TimeUnit.SECONDS);
        } catch (InterruptedException | ExecutionException | TimeoutException e) {
            log.error("Could not communicate with CCU to check pin");
            return false;
        }
        return response.isOk();
    }

    private String handler(String pin) {
        log.info("Got pin from user: {}; attempts count: {}; last at: {}", pin, tries, lastTryTime);
        if (!checkCount())
            return String.format("You have %s tries each %s seconds", MAX_TRY_COUNT, RETRY_DELAY_SEC);

        var hash = PINService.hashPin(pin);
        var res = verify(hash);

        log.info("PIN validated. Status={}", res);
        return res ? "OK" : "INVALID";
    }
}
