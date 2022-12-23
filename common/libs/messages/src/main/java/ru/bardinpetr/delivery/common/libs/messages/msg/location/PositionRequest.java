package ru.bardinpetr.delivery.common.libs.messages.msg.location;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import ru.bardinpetr.delivery.common.libs.messages.msg.ReplyableMessageRequest;

@Data
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
public class PositionRequest extends ReplyableMessageRequest {

}
