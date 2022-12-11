package ru.bardinpetr.delivery.libs.messages.msg.hmi;

import lombok.*;
import ru.bardinpetr.delivery.libs.messages.msg.MessageRequest;

@Data
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
public class PINEnterRequest extends MessageRequest {
    private String pin;
}
