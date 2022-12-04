package ru.bardinpetr.delivery.robot.hmi;

import ru.bardinpetr.delivery.robot.hmi.interactors.IUserInteractor;

import java.time.LocalTime;

public class MainService {

    public static final int MAX_TRY_COUNT = 3;
    public static final int RETRY_DELAY_SEC = 5;
    private final IUserInteractor userInterface;

    private int tries = 0;
    private LocalTime lastTryTime;

    public MainService(IUserInteractor userInterface) {
        this.userInterface = userInterface;
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
        return pin.equals("123456");
    }

    private String handler(String pin) {
        if (!checkCount())
            return String.format("You have %s tries each %s seconds", MAX_TRY_COUNT, RETRY_DELAY_SEC);

        boolean res = verify(pin);
        if (res) {
            tries = 0;
            return "ok";
        } else {
            return "invalid pin";
        }
    }
}
