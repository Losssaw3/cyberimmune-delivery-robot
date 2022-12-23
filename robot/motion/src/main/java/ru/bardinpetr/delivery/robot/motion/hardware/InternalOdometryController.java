package ru.bardinpetr.delivery.robot.motion.hardware;

import lombok.extern.slf4j.Slf4j;
import ru.bardinpetr.delivery.common.libs.messages.msg.location.Position;
import ru.bardinpetr.delivery.robot.motion.hardware.models.MotorParams;

import java.time.Duration;
import java.time.Instant;

/**
 * Used to predict current reference position knowing only when speed was changed.
 * Returns position calculated to current time.
 */
@Slf4j
public class InternalOdometryController {

    private MotorParams motors = new MotorParams(0, 0);
    private Position position = new Position(0, 0);
    private Instant lastUpdateTime;

    public Position getPosition() {
        if (lastUpdateTime == null) return position;

        var delta = (double) Duration.between(lastUpdateTime, Instant.now()).toMillis() / 1000;
        var speedX = motors.getSpeed() * Math.cos(motors.getDirection());
        var speedY = motors.getSpeed() * Math.sin(motors.getDirection());
        var deltaPos = new Position(speedX * delta, speedY * delta);
        var result = position.plus(deltaPos);

        log.debug("Calculated position {}: last {}, delta for time {} -> {},", result, position, delta, deltaPos);
        return result;
    }

    public void update(MotorParams currentTarget) {
        position = getPosition();
        motors = currentTarget;
        lastUpdateTime = Instant.now();
        log.debug("Calculated position before updating motor params: {}", position);
    }
}
