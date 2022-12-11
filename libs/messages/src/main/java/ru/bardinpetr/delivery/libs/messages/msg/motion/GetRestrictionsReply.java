package ru.bardinpetr.delivery.libs.messages.msg.motion;

import lombok.*;
import ru.bardinpetr.delivery.libs.messages.msg.MessageRequest;

@Data
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
public class GetRestrictionsReply extends MessageRequest {
    private double maxSpeed;
}

