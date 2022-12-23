package ru.bardinpetr.delivery.common.libs.messages.msg.motion;

import lombok.*;
import ru.bardinpetr.delivery.common.libs.messages.msg.MessageRequest;

@Data
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
public class GetRestrictionsReply extends MessageRequest {
    private double maxSpeed;
}

