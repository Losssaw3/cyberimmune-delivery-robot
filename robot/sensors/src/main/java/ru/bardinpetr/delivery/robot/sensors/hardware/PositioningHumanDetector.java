package ru.bardinpetr.delivery.robot.sensors.hardware;

import ru.bardinpetr.delivery.libs.messages.kafka.consumers.MonitoredKafkaConsumerFactory;
import ru.bardinpetr.delivery.libs.messages.kafka.consumers.MonitoredKafkaRequesterService;
import ru.bardinpetr.delivery.libs.messages.kafka.producers.MonitoredKafkaProducerFactory;
import ru.bardinpetr.delivery.libs.messages.models.Units;
import ru.bardinpetr.delivery.libs.messages.models.location.Position;
import ru.bardinpetr.delivery.libs.messages.models.location.PositionReply;
import ru.bardinpetr.delivery.libs.messages.models.location.PositionRequest;
import ru.bardinpetr.delivery.libs.messages.models.sensors.HumanDetectionConfigRequest;

import java.util.List;
import java.util.concurrent.*;

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

        kafka = new MonitoredKafkaRequesterService(Units.SENSORS.toString(), List.of(PositionReply.class), producerFactory, consumerFactory);

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

        try {
            var reply =
                    (PositionReply) kafka
                            .request(Units.LOC.toString(), new PositionRequest())
                            .get(60, TimeUnit.SECONDS);
            if (reply.getPosition().distance(targetLocation) < accuracy)
                callback.run();
        } catch (InterruptedException | ExecutionException | TimeoutException ignored) {
        }
    }

    @Override
    public void run() {
        kafka.start();
    }
}
