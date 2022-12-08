package ru.bardinpetr.delivery.libs.messages.models.hmi;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import ru.bardinpetr.delivery.libs.messages.MessageRequest;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PINEnterRequest extends MessageRequest {
    private String pin;
}
