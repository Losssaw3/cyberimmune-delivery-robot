package ru.bardinpetr.delivery.libs.messages.msg.authentication;

import lombok.*;
import ru.bardinpetr.delivery.libs.messages.msg.ReplyableMessageRequest;

@Data
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
public class CreatePINRequest extends ReplyableMessageRequest {
    private String userIdentifier;
}
