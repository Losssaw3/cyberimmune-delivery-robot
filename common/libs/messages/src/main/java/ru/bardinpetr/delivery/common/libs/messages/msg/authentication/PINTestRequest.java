package ru.bardinpetr.delivery.common.libs.messages.msg.authentication;

import lombok.*;
import ru.bardinpetr.delivery.common.libs.messages.msg.MessageRequest;

@Data
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
public class PINTestRequest extends MessageRequest {
    private String pin;

}
