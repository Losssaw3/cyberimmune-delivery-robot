package ru.bardinpetr.delivery.libs.messages.msg.ccu;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class InputDeliveryTask {
    private String signature;
    private DeliveryTask task;
}
