package ru.bardinpetr.delivery.robot.sensors.hardware;

import lombok.extern.slf4j.Slf4j;
import ru.bardinpetr.delivery.common.libs.messages.kafka.consumers.MonitoredKafkaConsumerFactory;
import ru.bardinpetr.delivery.common.libs.messages.kafka.consumers.MonitoredKafkaRequesterService;
import ru.bardinpetr.delivery.common.libs.messages.kafka.producers.MonitoredKafkaProducerFactory;
import ru.bardinpetr.delivery.common.libs.messages.msg.Unit;
import ru.bardinpetr.delivery.common.libs.messages.msg.location.Position;
import ru.bardinpetr.delivery.common.libs.messages.msg.location.PositionReply;
import ru.bardinpetr.delivery.common.libs.messages.msg.location.PositionRequest;
import ru.bardinpetr.delivery.common.libs.messages.msg.sensors.HumanDetectionConfigRequest;

import java.util.List;
import java.util.concurrent.*;

@Slf4j
public class PositioningHumanDetector extends Thread implements IHumanDetector {

    private final ScheduledExecutorService executorService;
    private final MonitoredKafkaRequesterService kafka;
    private final int checkInterval;
    private ScheduledFuture checkExecutor;
    private Position targetLocation;
    private double accuracy;

    private Runnable callback;

    public PositioningHumanDetector(MonitoredKafkaConsumerFactory consumerFactory, MonitoredKafkaProducerFactory producerFactory, int checkInterval) {
        this.checkInterval = checkInterval;

        kafka = new MonitoredKafkaRequesterService(Unit.SENSORS.toString(), List.of(PositionReply.class), producerFactory, consumerFactory);

        executorService = Executors.newSingleThreadScheduledExecutor();
    }

    @Override
    public void config(HumanDetectionConfigRequest config) {
        if (checkExecutor != null) checkExecutor.cancel(true);
        targetLocation = config.getLocation();
        accuracy = config.getAccuracy();

        checkExecutor = executorService.scheduleWithFixedDelay(this::check, 10, checkInterval, TimeUnit.SECONDS);
    }

    @Override
    public void setCallback(Runnable runnable) {
        this.callback = runnable;
    }

    private void check() {
        if (callback == null) return;

        log.info("Checking location...");
        try {
            var reply =
                    (PositionReply) kafka
                            .request(Unit.LOC.toString(), new PositionRequest())
                            .get(60, TimeUnit.SECONDS);
            var dist = reply.getPosition().distance(targetLocation);
            log.info("Got distance {}", dist);
            if (dist < accuracy)
                callback.run();
        } catch (InterruptedException | ExecutionException | TimeoutException ignored) {
        }
    }

    @Override
    public void run() {
        kafka.start();
    }
}
