package ru.bardinpetr.delivery.libs.messages.models.authentication;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import ru.bardinpetr.delivery.libs.messages.MessageRequest;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreatePINRequest extends MessageRequest {
    private String userIdentifier;
}
