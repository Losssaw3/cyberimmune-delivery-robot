package ru.bardinpetr.delivery.libs.messages.msg.ccu;

import lombok.*;
import ru.bardinpetr.delivery.libs.messages.msg.ForwardableMessageRequest;

@Data
@ToString(callSuper = false)
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
public class DeliveryStatusRequest extends ForwardableMessageRequest {
    private String text;
    private DeliveryStatus status;
}
