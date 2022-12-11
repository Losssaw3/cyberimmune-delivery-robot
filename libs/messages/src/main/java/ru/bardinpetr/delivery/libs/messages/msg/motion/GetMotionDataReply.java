package ru.bardinpetr.delivery.libs.messages.msg.motion;

import lombok.*;
import ru.bardinpetr.delivery.libs.messages.msg.MessageRequest;
import ru.bardinpetr.delivery.libs.messages.msg.location.Position;

@Data
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
public class GetMotionDataReply extends MessageRequest {
    private double currentSpeed;
    private double currentAngle;
    private Position odometerPosition;
}

