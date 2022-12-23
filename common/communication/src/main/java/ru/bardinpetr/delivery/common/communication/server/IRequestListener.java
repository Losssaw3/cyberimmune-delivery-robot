package ru.bardinpetr.delivery.common.communication.server;

import ru.bardinpetr.delivery.common.libs.messages.msg.MessageRequest;

public interface IRequestListener {
    void onMessage(String source, MessageRequest request);
}
