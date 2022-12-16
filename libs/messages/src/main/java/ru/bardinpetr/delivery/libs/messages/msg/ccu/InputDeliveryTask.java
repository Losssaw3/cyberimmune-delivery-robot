package ru.bardinpetr.delivery.libs.messages.msg.ccu;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class InputDeliveryTask {
    private String signature;
    private DeliveryTask task;
}
