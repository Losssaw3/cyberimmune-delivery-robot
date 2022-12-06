package ru.bardinpetr.delivery.monitor.validator;

import ru.bardinpetr.delivery.libs.messages.MessageRequest;

public interface IValidator {
    boolean verify(MessageRequest request);
}
