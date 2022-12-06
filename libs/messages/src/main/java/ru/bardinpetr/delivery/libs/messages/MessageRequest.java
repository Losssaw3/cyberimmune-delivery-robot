package ru.bardinpetr.delivery.libs.messages;


import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * Naming convention:
 * Any message consists of request and reply - separate classes extending MessageRequest
 * Request and response classes should have the following naming: %ActionName1%Request and %ActionName1%Reply
 * Sending action1 from srv1 to srv2 will result in sending message to topic srv2_action1request from srv2 and
 * then srv2 will reply to topic srv1_action1reply. Topics are generated with lowercase naming of message class name
 */

@JsonIgnoreProperties({"messageIdentifier", "targetTopic", "valid"})
public class MessageRequest {
    protected boolean isValid = true;

    private boolean isVerified = false;
    private boolean isReply;
    private String sender = "";
    private String recipient = "";

    public MessageRequest() {
        this.isReply = false;
    }

    public MessageRequest(String sender, String recipient) {
        this();
        this.sender = sender;
        this.recipient = recipient;
    }

    public MessageRequest(String sender, String recipient, boolean isReply) {
        this.sender = sender;
        this.recipient = recipient;
        this.isReply = isReply;
    }

    public static String getTargetTopic(Class<? extends MessageRequest> cls, String target) {
        return "%s_%s".formatted(target, cls.getSimpleName().toLowerCase());
    }

    public final String getTargetTopic() {
        return getTargetTopic(getClass(), recipient);
    }

    public boolean isReply() {
        return isReply;
    }

    public void setReply(boolean reply) {
        isReply = reply;
    }

    public boolean isVerified() {
        return isVerified;
    }

    public void setVerified(boolean verified) {
        isVerified = verified;
    }

    public String getSender() {
        return sender;
    }

    public void setSender(String sender) {
        this.sender = sender;
    }

    public String getRecipient() {
        return recipient;
    }

    public void setRecipient(String recipient) {
        this.recipient = recipient;
    }

    public boolean isValid() {
        return isValid;
    }

    @Override
    public String toString() {
        return "MessageRequest{" +
                "sender='" + sender + '\'' +
                ", recipient='" + recipient + '\'' +
                '}';
    }

    public static class Reply extends MessageRequest {
        public Reply(MessageRequest base) {
            super(base.getRecipient(), base.getSender(), true);
        }
    }
}
