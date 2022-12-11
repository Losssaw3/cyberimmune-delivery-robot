package ru.bardinpetr.delivery.libs.messages.msg;


import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Data
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
public class ReplyableMessageRequest extends MessageRequest {
}
