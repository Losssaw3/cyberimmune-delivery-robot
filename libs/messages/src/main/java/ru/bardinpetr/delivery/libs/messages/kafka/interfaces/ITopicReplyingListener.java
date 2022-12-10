package ru.bardinpetr.delivery.libs.messages.kafka.interfaces;

public interface ITopicReplyingListener {
    <T, S> S process(T data);
}
