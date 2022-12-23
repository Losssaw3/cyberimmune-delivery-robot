package ru.bardinpetr.delivery.common.monitor.validator;

import ru.bardinpetr.delivery.common.libs.messages.msg.MessageRequest;

public interface IValidator {
    boolean verify(MessageRequest request);
}
