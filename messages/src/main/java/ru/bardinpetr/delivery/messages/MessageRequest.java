package ru.bardinpetr.delivery.messages;


/**
 * Naming convention:
 * Any message consists of request and reply - separate classes extending MessageRequest
 * Request and response classes should have the following naming: %ActionName1%Request and %ActionName1%Reply
 * Sending action1 from srv1 to srv2 will result in sending message to topic srv2_Action1Request from srv2 and
 * then srv2 will reply to topic srv1_Action1Reply
 */

public class MessageRequest {

    private final boolean isReply;
    private String sender;
    private String recipient;

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

    public MessageRequest() {
        this.isReply = false;
    }

    public static String getAction() {
        return "";
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

    public boolean isReply() {
        return isReply;
    }

    public Reply createReply() {
        return new Reply(this);
    }

    public String getIncomingTopic() {
        return "%s_%s".formatted(recipient, getClass().getSimpleName());
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
