package ru.bardinpetr.delivery.libs.messages;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ReplyableMessageRequest extends MessageRequest {

    private int requestId = -1;

    public MessageRequest getReply() {
        return null;
    }


}
