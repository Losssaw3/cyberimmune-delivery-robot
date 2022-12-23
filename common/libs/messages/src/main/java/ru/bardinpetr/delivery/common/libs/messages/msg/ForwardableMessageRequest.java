package ru.bardinpetr.delivery.common.libs.messages.msg;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

@Data
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
public class ForwardableMessageRequest extends MessageRequest {
    private String forwardTo;
    private boolean isForwarded = false;
    private String recipientBridgeURL = null;
    private String senderBridgeURL = null;
}
