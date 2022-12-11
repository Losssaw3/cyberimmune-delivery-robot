package ru.bardinpetr.delivery.monitor.validator;

import ru.bardinpetr.delivery.libs.messages.msg.MessageRequest;

public interface IValidator {
    boolean verify(MessageRequest request);
}
