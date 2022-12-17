package ru.bardinpetr.delivery.robot.locker.hardware;

import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

@Slf4j
public class LockerController {
    volatile private boolean isOpened = false;
    private final ScheduledExecutorService executor = Executors.newSingleThreadScheduledExecutor();

    /**
     * Opens locker for specified time
     *
     * @param timeSec Duration in seconds for lock to be opened
     */
    public void openLocker(int timeSec, Runnable onDoorClosed) {
        if (isOpened) return;

        setLockerStatus(true);
        executor.schedule(() -> setLockerStatus(false), timeSec, TimeUnit.SECONDS);

        // This is only for demonstration. There should be real sensor
        executor.schedule(onDoorClosed, timeSec * 2L, TimeUnit.SECONDS);
    }

    /**
     * Send locker command to hardware
     *
     * @param locked status to set
     */
    private void setLockerStatus(boolean locked) {
        isOpened = locked;
        log.warn("Locker status set to {}", locked);

        // Hardware controlling goes here...
    }
}
