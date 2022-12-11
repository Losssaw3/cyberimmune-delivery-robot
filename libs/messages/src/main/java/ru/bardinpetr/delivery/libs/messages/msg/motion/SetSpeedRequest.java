package ru.bardinpetr.delivery.libs.messages.msg.motion;

import lombok.*;
import ru.bardinpetr.delivery.libs.messages.msg.MessageRequest;

@Data
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
public class SetSpeedRequest extends MessageRequest {
    private double speed;
    private double angle;
}
