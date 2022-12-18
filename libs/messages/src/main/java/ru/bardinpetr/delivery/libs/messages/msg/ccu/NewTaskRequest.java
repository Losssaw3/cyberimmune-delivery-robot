package ru.bardinpetr.delivery.libs.messages.msg.ccu;

import lombok.*;
import ru.bardinpetr.delivery.libs.messages.msg.ForwardableMessageRequest;

@Data
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
public class NewTaskRequest extends ForwardableMessageRequest {
    private InputDeliveryTask deliveryTask;
}
