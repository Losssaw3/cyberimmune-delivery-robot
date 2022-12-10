package ru.bardinpetr.delivery.robot.motion.hardware;


import lombok.Data;
import ru.bardinpetr.delivery.robot.motion.hardware.models.MotorParams;

@Data
public class MotorRestrictions {

    private final int maxSpeed;

    public MotorParams apply(MotorParams params) {
        params.setSpeed(Math.min(params.getSpeed(), maxSpeed));
        return params;
    }
}
