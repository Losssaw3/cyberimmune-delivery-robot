package ru.bardinpetr.delivery.libs.messages;

public class InvalidMessageRequest extends MessageRequest {
    public InvalidMessageRequest() {
        super();
        this.isValid = false;
    }
}
