package ru.bardinpetr.delivery.libs.messages.models.motion;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import ru.bardinpetr.delivery.libs.messages.MessageRequest;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class GetMotionDataReply extends MessageRequest {
    private double currentSpeed;
    private double currentAngle;
    private double[] odometerPosition;
}

