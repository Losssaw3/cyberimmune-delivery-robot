package ru.bardinpetr.delivery.libs.messages.msg.locker;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import ru.bardinpetr.delivery.libs.messages.msg.MessageRequest;


@Data
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
public class LockerDoorClosedRequest extends MessageRequest {
}
