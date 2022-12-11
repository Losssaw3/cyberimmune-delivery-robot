package ru.bardinpetr.delivery.libs.messages.msg;


import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * Naming convention:
 * Any message consists of request and reply - separate classes extending MessageRequest
 * Request and response classes should have the following naming: %ActionName1%Request and %ActionName1%Reply
 * Sending action1 from srv1 to srv2 will result in sending message to topic srv2_action1request from srv2 and
 * then srv2 will reply to topic srv1_action1reply. Topics are generated with lowercase naming of message class name
 */

@JsonIgnoreProperties({"targetTopic", "valid"})
@Data
@AllArgsConstructor
public class MessageRequest {

    private boolean isValid = true;
    private String requestId = "";
    private String recipient = "";
    private String sender = "";

    public MessageRequest(String recipient, String sender) {
        this.recipient = recipient;
        this.sender = sender;
    }

    public MessageRequest(boolean isValid) {
        this.isValid = isValid;
    }

    public MessageRequest() {
    }

    public static String getTargetTopic(Class<? extends MessageRequest> cls, String target) {
        return "%s_%s".formatted(target, cls.getSimpleName().toLowerCase());
    }

    /**
     * Generate ID for message as reply to the given one.
     */
    public static String getReplyMessageID(String original) {
        return "reply-%s".formatted(original);
    }

    /**
     * Join actionType with base messages package and try te get class for it
     *
     * @param actionType field of message to deserialize
     * @return class for this handler
     */
    public static String getClassNameFromActionType(String actionType) {
        return "%s.%s".formatted(MessageRequest.class.getPackageName(), actionType);
    }

    public final String getTargetTopic() {
        return getTargetTopic(getClass(), recipient);
    }

    /**
     * This field is used for action identification and deserialization.
     * Method works in pair with getClassFromActionType
     *
     * @return canonical class name without
     */
    public String getActionType() {
        String full = getClass().getCanonicalName();
        String pkgRegex = "%s.".formatted(MessageRequest.class.getPackageName())
                .replace(".", "\\.");
        return full.replaceAll(pkgRegex, "");
    }

    public void asReplyTo(MessageRequest request) {
        sender = request.recipient;
        recipient = request.sender;
        requestId = getReplyMessageID(request.requestId);
    }
}
