package ru.bardinpetr.delivery.libs.messages.msg.motion;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;
import ru.bardinpetr.delivery.libs.messages.msg.ReplyableMessageRequest;

@Data
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
public class GetMotionDataRequest extends ReplyableMessageRequest {
}
