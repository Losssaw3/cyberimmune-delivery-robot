package ru.bardinpetr.delivery.robot.odometer.hardware;

import lombok.extern.slf4j.Slf4j;
import ru.bardinpetr.delivery.libs.messages.kafka.consumers.MonitoredKafkaConsumerFactory;
import ru.bardinpetr.delivery.libs.messages.kafka.consumers.MonitoredKafkaRequesterService;
import ru.bardinpetr.delivery.libs.messages.kafka.producers.MonitoredKafkaProducerFactory;
import ru.bardinpetr.delivery.libs.messages.msg.Units;
import ru.bardinpetr.delivery.libs.messages.msg.location.Position;
import ru.bardinpetr.delivery.libs.messages.msg.motion.GetMotionDataReply;
import ru.bardinpetr.delivery.libs.messages.msg.motion.GetMotionDataRequest;
import ru.bardinpetr.delivery.robot.positioning_driver.IPositionService;

import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

@Slf4j
public class MockHardwareOdometer extends Thread implements IPositionService {

    private final MonitoredKafkaRequesterService kafka;

    public MockHardwareOdometer(MonitoredKafkaConsumerFactory consumerFactory,
                                MonitoredKafkaProducerFactory producerFactory) {

        kafka = new MonitoredKafkaRequesterService(
                Units.POS_ODOM.toString(),
                List.of(GetMotionDataReply.class),
                producerFactory,
                consumerFactory
        );
    }


    @Override
    public Position getCurrentPosition() {
        try {
            log.debug("Starting update");
            var reply =
                    (GetMotionDataReply) kafka
                            .request(Units.MOTION.toString(), new GetMotionDataRequest())
                            .get(30, TimeUnit.SECONDS);

            log.debug("New data from motion unit: {}", reply);

            return reply.getOdometerPosition();
        } catch (InterruptedException | ExecutionException | TimeoutException e) {
            log.warn("Failed to update position", e);
        }
        return null;
    }

    @Override
    public void run() {
        kafka.start();
        log.info("Started");
    }
}
