package ru.bardinpetr.delivery.libs.messages.msg.sensors;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import ru.bardinpetr.delivery.libs.messages.msg.MessageRequest;

@Data
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
public class HumanDetectedRequest extends MessageRequest {
}
