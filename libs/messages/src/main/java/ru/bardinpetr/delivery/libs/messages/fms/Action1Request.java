package ru.bardinpetr.delivery.libs.messages.fms;

import ru.bardinpetr.delivery.libs.messages.MessageRequest;

public class Action1Request extends MessageRequest {
    private String query;

    public Action1Request() {
        super();
    }

    public Action1Request(String query) {
        this.query = query;
    }

    public static String getAction() {
        return "";
    }

    public static String getTopic(String recipient) {
        return "%s:%s".formatted(recipient, getAction());
    }

    public String getQuery() {
        return query;
    }

    public void setQuery(String query) {
        this.query = query;
    }

    @Override
    public String toString() {
        return "Action1Message{" +
                "query='" + query + '\'' +
                "} " + super.toString();
    }

    public static class Reply extends MessageRequest.Reply {
        private String result;

        public Reply(MessageRequest base, String result) {
            super(base);
            this.result = result;
        }

        public String getResult() {
            return result;
        }

        public void setResult(String result) {
            this.result = result;
        }
    }

}
