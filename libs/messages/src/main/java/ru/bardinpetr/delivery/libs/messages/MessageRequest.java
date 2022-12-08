package ru.bardinpetr.delivery.libs.messages;


import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

/**
 * Naming convention:
 * Any message consists of request and reply - separate classes extending MessageRequest
 * Request and response classes should have the following naming: %ActionName1%Request and %ActionName1%Reply
 * Sending action1 from srv1 to srv2 will result in sending message to topic srv2_action1request from srv2 and
 * then srv2 will reply to topic srv1_action1reply. Topics are generated with lowercase naming of message class name
 */

@JsonIgnoreProperties({"messageIdentifier", "targetTopic", "valid"})
@Data
public class MessageRequest {
    protected boolean isValid = true;
    private String recipient = "";
    private String sender = "";
    private boolean isVerified = false;
    private boolean isReply = false;

    public MessageRequest(String recipient, String sender) {
        this.recipient = recipient;
        this.sender = sender;
    }

    public MessageRequest() {
    }

    public static String getTargetTopic(Class<? extends MessageRequest> cls, String target) {
        return "%s_%s".formatted(target, cls.getSimpleName().toLowerCase());
    }

    public final String getTargetTopic() {
        return getTargetTopic(getClass(), recipient);
    }

    @Override
    public String toString() {
        return "MessageRequest{" +
                "sender='" + sender + '\'' +
                ", recipient='" + recipient + '\'' +
                '}';
    }
}
