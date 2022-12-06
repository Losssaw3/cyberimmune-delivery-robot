package ru.bardinpetr.delivery.messages.kafka.interfaces;

import ru.bardinpetr.delivery.messages.MessageRequest;

public interface ITopicListener {
    <T extends MessageRequest> void onMessage(T data);
}
