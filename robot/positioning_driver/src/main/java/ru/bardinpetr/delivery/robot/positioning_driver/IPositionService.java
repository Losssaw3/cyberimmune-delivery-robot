package ru.bardinpetr.delivery.robot.positioning_driver;

import ru.bardinpetr.delivery.common.libs.messages.msg.location.Position;

public interface IPositionService {

    Position getCurrentPosition();

    void start();
}
