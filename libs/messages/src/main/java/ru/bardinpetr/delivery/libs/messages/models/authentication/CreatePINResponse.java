package ru.bardinpetr.delivery.libs.messages.models.authentication;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import ru.bardinpetr.delivery.libs.messages.MessageRequest;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CreatePINResponse extends MessageRequest {
    private String AESEncryptedPIN;

}
