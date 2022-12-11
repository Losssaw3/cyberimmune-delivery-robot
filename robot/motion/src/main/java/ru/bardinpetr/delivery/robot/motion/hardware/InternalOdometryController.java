package ru.bardinpetr.delivery.robot.motion.hardware;

import ru.bardinpetr.delivery.libs.messages.msg.location.Position;
import ru.bardinpetr.delivery.robot.motion.hardware.models.MotorParams;

import java.time.Duration;
import java.time.Instant;

/**
 * Used to predict current reference position knowing only when speed was changed.
 * Returns position calculated to current time.
 */
public class InternalOdometryController {

    private MotorParams motors = new MotorParams(0, 0);
    private Position position = new Position(0, 0);
    private Instant lastUpdateTime;

    public Position getPosition() {
        if (lastUpdateTime == null) return position;

        var delta = (double) Duration.between(lastUpdateTime, Instant.now()).toMillis() / 1000;
        var speedX = motors.getSpeed() * Math.cos(motors.getDirection());
        var speedY = motors.getSpeed() * Math.sin(motors.getDirection());
        System.err.printf("%s %s %s %s %s %s %s\n", lastUpdateTime, Instant.now(), delta, speedY, speedX, position.getX() + speedX * delta, position.getY() + speedY * delta);
        return new Position(position.getX() + speedX * delta, position.getY() + speedY * delta);
    }

    public void update(MotorParams currentTarget) {
        position = getPosition();
        motors = currentTarget;
        lastUpdateTime = Instant.now();
    }
}
