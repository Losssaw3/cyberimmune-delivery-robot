package ru.bardinpetr.delivery.libs.messages;


import lombok.Data;
import lombok.ToString;

@Data
@ToString(callSuper = true)
public class ReplyableMessageRequest extends MessageRequest {

    public MessageRequest makeReply(MessageRequest src) {
        return new MessageRequest(true,
                src.getRequestId(),
                src.getSender(), src.getRecipient(),
                false, true
        );
    }


}
