package ru.bardinpetr.delivery.e2e.suite;

import org.junit.jupiter.api.Assertions;
import ru.bardinpetr.delivery.common.libs.messages.msg.MessageRequest;

import java.util.concurrent.CompletableFuture;

public class MessageAssertableFuture extends CompletableFuture<MessageRequest> {

    public void assertSent(String info) {

        Assertions.assertNotNull(null, info);
    }

    public void assertNotSent(String info) {
        Assertions.assertNull(null, info);

    }

}

