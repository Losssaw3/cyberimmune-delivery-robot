package ru.bardinpetr.delivery.libs.messages.msg.authentication;

import lombok.*;
import ru.bardinpetr.delivery.libs.messages.msg.MessageRequest;

@Data
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
public class CreatePINRequest extends MessageRequest {
    private String userIdentifier;
}
