package ru.bardinpetr.delivery.robot.motion.hardware.models;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class MotorParams {
    private double speed = 0;
    private double direction = 0;
}
