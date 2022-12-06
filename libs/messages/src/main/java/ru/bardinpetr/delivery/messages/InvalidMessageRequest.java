package ru.bardinpetr.delivery.messages;

public class InvalidMessageRequest extends MessageRequest {
    public InvalidMessageRequest() {
        super();
        this.isValid = false;
    }
}
