package ru.bardinpetr.delivery.libs.messages.kafka.consumers;

import org.springframework.kafka.support.serializer.ErrorHandlingDeserializer;
import org.springframework.kafka.support.serializer.FailedDeserializationInfo;
import org.springframework.kafka.support.serializer.JsonDeserializer;
import ru.bardinpetr.delivery.libs.messages.InvalidMessageRequest;
import ru.bardinpetr.delivery.libs.messages.MessageRequest;

import java.util.function.Function;

public class DeserializerFactory {
    public static ErrorHandlingDeserializer<MessageRequest> getDeserializer() {
        return getDeserializer(info -> {
            System.err.printf("Invalid message processed: %s", info);
            return new InvalidMessageRequest();
        });
    }

    public static ErrorHandlingDeserializer<MessageRequest> getDeserializer(
            Function<FailedDeserializationInfo, MessageRequest> errorHandler) {
        var jsonDeserializer = new JsonDeserializer<MessageRequest>();
        jsonDeserializer.addTrustedPackages("*");

        var errorDeserializer = new ErrorHandlingDeserializer<>(jsonDeserializer);
        errorDeserializer.setFailedDeserializationFunction(errorHandler);

        return errorDeserializer;
    }

}
