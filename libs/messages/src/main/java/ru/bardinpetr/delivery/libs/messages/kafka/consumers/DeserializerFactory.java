package ru.bardinpetr.delivery.libs.messages.kafka.consumers;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.kafka.support.serializer.ErrorHandlingDeserializer;
import org.springframework.kafka.support.serializer.FailedDeserializationInfo;
import org.springframework.kafka.support.serializer.JsonDeserializer;
import ru.bardinpetr.delivery.libs.messages.msg.MessageRequest;

import java.util.function.Function;

public class DeserializerFactory {
    public static ErrorHandlingDeserializer<MessageRequest> getDeserializer() {
        return getDeserializer(info -> {
            System.err.printf("Invalid message processed: %s", info);
            return new MessageRequest(false);
        });
    }

    public static ErrorHandlingDeserializer<MessageRequest> getDeserializer(
            Function<FailedDeserializationInfo, MessageRequest> errorHandler) {
        var mapper = new ObjectMapper();

        var jsonDeserializer = new JsonDeserializer<MessageRequest>();
        jsonDeserializer.addTrustedPackages(MessageRequest.getClassNameFromActionType("*"));
        jsonDeserializer.ignoreTypeHeaders();
        jsonDeserializer.typeResolver((topic, data, headers) -> {
            try {
                var msg = mapper.readTree(data);
                var action = msg.get("actionType");
                if (action == null) return null;
                var javaClass = MessageRequest.getClassNameFromActionType(action.asText());
                return mapper.getTypeFactory().constructFromCanonical(javaClass);
            } catch (Exception ignored) {
            }
            return null;
        });

        var errorDeserializer = new ErrorHandlingDeserializer<>(jsonDeserializer);
        errorDeserializer.setFailedDeserializationFunction(errorHandler);

        return errorDeserializer;
    }

}
