package ru.bardinpetr.delivery.robot.motion.hardware;

import lombok.extern.slf4j.Slf4j;
import ru.bardinpetr.delivery.common.libs.messages.msg.location.Position;
import ru.bardinpetr.delivery.robot.motion.hardware.models.MotorParams;

@Slf4j
public class MotorController {
    private final InternalOdometryController odometryController;
    private final MotorRestrictions restrictions;
    private MotorParams currentTarget = new MotorParams();

    public MotorController(MotorRestrictions restrictions) {
        this.restrictions = restrictions;
        odometryController = new InternalOdometryController();
    }

    public void set(MotorParams params) {
        currentTarget = restrictions.apply(params);
        odometryController.update(currentTarget);
        // send commands to motors
        log.info("Sending to hardware: {}", params);
    }

    public MotorRestrictions getRestrictions() {
        return restrictions;
    }

    public MotorParams getCurrentTarget() {
        return currentTarget;
    }

    public Position getReferenceIdealPosition() {
        return odometryController.getPosition();
    }
}
