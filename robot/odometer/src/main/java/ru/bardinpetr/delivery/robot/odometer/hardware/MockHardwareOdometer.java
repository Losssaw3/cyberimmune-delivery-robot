package ru.bardinpetr.delivery.robot.odometer.hardware;

import ru.bardinpetr.delivery.libs.messages.kafka.consumers.MonitoredKafkaConsumerFactory;
import ru.bardinpetr.delivery.libs.messages.kafka.consumers.MonitoredKafkaRequesterService;
import ru.bardinpetr.delivery.libs.messages.kafka.producers.MonitoredKafkaProducerFactory;
import ru.bardinpetr.delivery.libs.messages.msg.Units;
import ru.bardinpetr.delivery.libs.messages.msg.location.Position;
import ru.bardinpetr.delivery.libs.messages.msg.motion.GetMotionDataReply;
import ru.bardinpetr.delivery.libs.messages.msg.motion.GetMotionDataRequest;
import ru.bardinpetr.delivery.robot.positioning_driver.IPositionService;

import java.util.List;
import java.util.concurrent.*;

public class MockHardwareOdometer implements IPositionService {

    private final MonitoredKafkaRequesterService kafka;
    private final ScheduledFuture updaterFuture;

    private Position currentPosition = new Position(0, 0);

    public MockHardwareOdometer(MonitoredKafkaConsumerFactory consumerFactory,
                                MonitoredKafkaProducerFactory producerFactory,
                                int checkInterval) {

        kafka = new MonitoredKafkaRequesterService(
                Units.POS_ODOM.toString(),
                List.of(GetMotionDataReply.class),
                producerFactory,
                consumerFactory
        );

        updaterFuture = Executors
                .newSingleThreadScheduledExecutor()
                .scheduleWithFixedDelay(
                        this::update,
                        60,
                        checkInterval,
                        TimeUnit.SECONDS
                );
    }

    private void update() {
        try {
            var reply =
                    (GetMotionDataReply) kafka
                            .request(Units.MOTION.toString(), new GetMotionDataRequest())
                            .exceptionally((ignored) -> null)
                            .get(30, TimeUnit.SECONDS);

            currentPosition = reply.getOdometerPosition();
        } catch (InterruptedException | ExecutionException | TimeoutException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public Position getCurrentPosition() {
        return currentPosition;
    }

    @Override
    public void run() {
        kafka.start();
    }

    @Override
    public void close() {
        updaterFuture.cancel(true);
    }
}
