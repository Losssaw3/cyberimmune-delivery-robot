package ru.bardinpetr.delivery.common.libs.messages.msg.location;

import lombok.*;
import ru.bardinpetr.delivery.common.libs.messages.msg.MessageRequest;

@Data
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
public class PositionReply extends MessageRequest {
    private Position position;
    private int accuracy;
}
