package ru.bardinpetr.delivery.common.libs.messages.kafka.interfaces;

import ru.bardinpetr.delivery.common.libs.messages.msg.MessageRequest;

public interface ITopicListener<T extends MessageRequest> {
    void onMessage(T data);
}
