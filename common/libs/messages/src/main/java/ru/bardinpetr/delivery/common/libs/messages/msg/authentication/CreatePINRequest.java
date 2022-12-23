package ru.bardinpetr.delivery.common.libs.messages.msg.authentication;

import lombok.*;
import ru.bardinpetr.delivery.common.libs.messages.msg.ReplyableMessageRequest;

@Data
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
public class CreatePINRequest extends ReplyableMessageRequest {
    private String userIdentifier;
}
