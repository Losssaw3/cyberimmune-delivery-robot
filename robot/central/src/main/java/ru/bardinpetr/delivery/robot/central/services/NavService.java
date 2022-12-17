package ru.bardinpetr.delivery.robot.central.services;

import lombok.extern.slf4j.Slf4j;
import ru.bardinpetr.delivery.libs.messages.kafka.consumers.MonitoredKafkaConsumerFactory;
import ru.bardinpetr.delivery.libs.messages.kafka.consumers.MonitoredKafkaRequesterService;
import ru.bardinpetr.delivery.libs.messages.kafka.producers.MonitoredKafkaProducerFactory;
import ru.bardinpetr.delivery.libs.messages.kafka.producers.MonitoredKafkaProducerService;
import ru.bardinpetr.delivery.libs.messages.msg.Units;
import ru.bardinpetr.delivery.libs.messages.msg.location.Position;
import ru.bardinpetr.delivery.libs.messages.msg.location.PositionReply;
import ru.bardinpetr.delivery.libs.messages.msg.location.PositionRequest;
import ru.bardinpetr.delivery.libs.messages.msg.motion.SetSpeedRequest;

import java.util.List;
import java.util.concurrent.*;

import static ru.bardinpetr.delivery.robot.central.MainService.SERVICE_NAME;

@Slf4j
public class NavService {
    private static final int UPDATE_INTERVAL_SEC = 1;
    private static final double POSITION_COMPARISON_THRESHOLD = 10;

    private final MonitoredKafkaProducerService producerService;
    private final MonitoredKafkaRequesterService requesterService;

    private final ScheduledExecutorService executor;

    private Position target;
    private Runnable arrivedCallback;

    private boolean isRunning = false;

    public NavService(MonitoredKafkaConsumerFactory consumerFactory,
                      MonitoredKafkaProducerFactory producerFactory) {
        producerService = new MonitoredKafkaProducerService(
                SERVICE_NAME,
                producerFactory
        );

        requesterService = new MonitoredKafkaRequesterService(
                SERVICE_NAME,
                List.of(PositionReply.class),
                producerFactory, consumerFactory
        );

        executor = Executors.newScheduledThreadPool(2);
    }

    /**
     * Main controlling cycle. Executed after start each  UPDATE_INTERVAL_SEC;
     */
    private void update() {
        try {
            requesterService
                    .request(Units.LOC.toString(), new PositionRequest())
                    .thenApply(reply -> (PositionReply) reply)
                    .thenApply(reply -> (reply.getAccuracy() > 0 ? reply.getPosition() : null))
                    .thenAccept(position -> {
                        if (position == null) {
                            log.error("ALL POSITION PROVIDERS TAMPERED. STOPPING");
                            setMotors(0, 0);
                            return;
                        }
                        log.debug("Current: {}; target: {}", position, target);

                        var angle = position.directionTo(target);
                        var distance = position.distance(target);
                        log.debug("Distance to target {}; new direction: {}", distance, angle);

                        if (distance < POSITION_COMPARISON_THRESHOLD) {
                            onEnd(position);
                            return;
                        }

                        setMotors(5, angle);
                    })
                    .get(30, TimeUnit.SECONDS);
        } catch (InterruptedException | ExecutionException | TimeoutException ex) {
            log.error("Navigation cycle failed to update position", ex);
        }
    }

    private void onEnd(Position reachedPos) {
        log.warn("Finished at: {}", reachedPos);
        setMotors(0, 0);

        isRunning = false;
        target = null;

        executor.shutdownNow();
        arrivedCallback.run();
    }

    private void setMotors(double speed, double angle) {
        producerService.sendMessage(
                Units.MOTION,
                new SetSpeedRequest(speed, angle)
        );
    }

    public void setTarget(Position position) {
        if (!isRunning)
            target = position;
    }

    /**
     * starts navigation to position set with setTarget
     *
     * @param onArrived callback executed when arrived to the destination
     * @return boolean if start was successful
     */
    public boolean run(Runnable onArrived) {
        if (target == null || isRunning) return false;

        arrivedCallback = onArrived;
        executor.scheduleWithFixedDelay(
                this::update,
                UPDATE_INTERVAL_SEC, UPDATE_INTERVAL_SEC, TimeUnit.SECONDS
        );
        isRunning = true;
        return true;
    }

    public void start() {
//        consumerService.start();
        requesterService.start();
        log.info("Navigation started");
    }
}
