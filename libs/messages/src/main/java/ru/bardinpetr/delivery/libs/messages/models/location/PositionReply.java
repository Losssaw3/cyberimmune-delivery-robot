package ru.bardinpetr.delivery.libs.messages.models.location;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import ru.bardinpetr.delivery.libs.messages.MessageRequest;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PositionReply extends MessageRequest {
    private Position position;
    private int accuracy;
}
