package ru.bardinpetr.delivery.libs.messages.models.motion;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import ru.bardinpetr.delivery.libs.messages.MessageRequest;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SetSpeedRequest extends MessageRequest {
    private double speed;
    private double angle;
    private boolean brake;
}
