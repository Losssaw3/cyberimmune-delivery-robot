package ru.bardinpetr.delivery.common.libs.messages.kafka.interfaces;

public interface ITopicReplyingListener {
    <T, S> S process(T data);
}
