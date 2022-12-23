package ru.bardinpetr.delivery.backend.fms.robots;

import lombok.Data;
import lombok.NonNull;
import ru.bardinpetr.delivery.common.libs.messages.msg.ccu.DeliveryStatus;

@Data
public class Robot {
    @NonNull
    private String url;
    @NonNull
    private String realIP;
    private DeliveryStatus status = DeliveryStatus.IDLE;
}
