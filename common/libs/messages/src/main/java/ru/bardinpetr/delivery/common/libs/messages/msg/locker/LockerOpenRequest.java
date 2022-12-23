package ru.bardinpetr.delivery.common.libs.messages.msg.locker;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import ru.bardinpetr.delivery.common.libs.messages.msg.MessageRequest;


@Data
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
public class LockerOpenRequest extends MessageRequest {
}
