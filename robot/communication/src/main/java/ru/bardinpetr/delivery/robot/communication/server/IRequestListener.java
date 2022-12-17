package ru.bardinpetr.delivery.robot.communication.server;

import ru.bardinpetr.delivery.libs.messages.msg.MessageRequest;

public interface IRequestListener {
    void onMessage(MessageRequest request);
}
