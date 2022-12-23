package ru.bardinpetr.delivery.common.libs.messages.msg.motion;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;
import ru.bardinpetr.delivery.common.libs.messages.msg.ReplyableMessageRequest;

@Data
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
public class GetRestrictionsRequest extends ReplyableMessageRequest {
}
