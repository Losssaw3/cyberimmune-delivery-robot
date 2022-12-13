package ru.bardinpetr.delivery.robot.locker.hardware;

import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

@Slf4j
public class LockerController {
    volatile private boolean isOpened = false;

    /**
     * Opens locker for specified time
     *
     * @param timeSec Duration in seconds for lock to be opened
     */
    public void openLocker(int timeSec) {
        if (isOpened) return;

        setLockerStatus(true);
        Executors
                .newSingleThreadScheduledExecutor()
                .schedule(() -> setLockerStatus(false), timeSec, TimeUnit.SECONDS);
    }

    /**
     * Send locker command to hardware
     *
     * @param locked status to set
     */
    private void setLockerStatus(boolean locked) {
        isOpened = locked;
        log.warn("Locker status set to {}", locked);
    }

}
