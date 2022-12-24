package ru.bardinpetr.delivery.common.libs.messages.msg;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class ForwardableMessageRequest extends MessageRequest {
    private String forwardTo;
    private boolean isForwarded = false;
    private String recipientBridgeURL = null;
    private String senderBridgeURL = null;

    @Override
    public String toString() {
        return "FMR{%s->%s->%s}".formatted(getSender(), getRecipient(), getForwardTo());
    }
}
