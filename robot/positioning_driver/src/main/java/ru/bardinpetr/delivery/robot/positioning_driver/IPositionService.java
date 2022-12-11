package ru.bardinpetr.delivery.robot.positioning_driver;

import ru.bardinpetr.delivery.libs.messages.msg.location.Position;

public interface IPositionService extends Runnable, AutoCloseable {

    Position getCurrentPosition();

}
