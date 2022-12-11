package ru.bardinpetr.delivery.libs.messages.models.sensors;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import ru.bardinpetr.delivery.libs.messages.MessageRequest;
import ru.bardinpetr.delivery.libs.messages.models.location.Position;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class HumanDetectionConfigRequest extends MessageRequest {
    private Position location;
    private double accuracy;
}
