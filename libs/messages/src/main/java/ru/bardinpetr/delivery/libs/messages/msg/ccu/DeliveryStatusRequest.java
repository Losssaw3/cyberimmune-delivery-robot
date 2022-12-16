package ru.bardinpetr.delivery.libs.messages.msg.ccu;

import lombok.*;
import ru.bardinpetr.delivery.libs.messages.msg.MessageRequest;

@Data
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
public class DeliveryStatusRequest extends MessageRequest {
    private String text;
    private DeliveryStatus status;
}
