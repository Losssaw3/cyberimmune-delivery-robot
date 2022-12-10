package ru.bardinpetr.delivery.libs.messages.models.motion;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import ru.bardinpetr.delivery.libs.messages.MessageRequest;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class GetRestrictionsReply extends MessageRequest {
    private double maxSpeed;
}

