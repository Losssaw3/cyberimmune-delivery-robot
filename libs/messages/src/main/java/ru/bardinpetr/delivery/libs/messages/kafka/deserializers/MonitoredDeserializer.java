package ru.bardinpetr.delivery.libs.messages.kafka.deserializers;

import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.common.header.Headers;
import org.springframework.kafka.support.serializer.ErrorHandlingDeserializer;
import org.springframework.kafka.support.serializer.JsonDeserializer;
import ru.bardinpetr.delivery.libs.messages.msg.MessageRequest;


/**
 * Deserializer for messages on MB. Uses JSON root field actionType
 * for identification via MessageRequest.getClassNameFromActionType.
 *
 * @see ru.bardinpetr.delivery.libs.messages.msg.MessageRequest
 */
@Slf4j
public class MonitoredDeserializer extends ErrorHandlingDeserializer<MessageRequest> {

    public MonitoredDeserializer(IDeserializerErrorHandler errorHandler) {
        super(getDeserializer());
        setFailedDeserializationFunction(error -> errorHandler.onError(error.getTopic(), error.getException()));
    }

    public MonitoredDeserializer() {
        this((topic, ex) -> {
            log.error("Invalid message on {} processed: {}", topic, ex);
            return new MessageRequest(false);
        });
    }

    private static JsonDeserializer<MessageRequest> getDeserializer() {
        var jsonDeserializer = new JsonDeserializer<MessageRequest>();
        jsonDeserializer.addTrustedPackages(MessageRequest.getClassNameFromActionType("*"));
        jsonDeserializer.ignoreTypeHeaders();
        jsonDeserializer.typeResolver(MonitoredDeserializer::resolveType);
        return jsonDeserializer;
    }

    private static JavaType resolveType(String topic, byte[] data, Headers headers) {
        var mapper = new ObjectMapper();
        try {
            var action = mapper.readTree(data).get("actionType");
            if (action == null) return null;
            var javaClass = MessageRequest.getClassNameFromActionType(action.asText());
            return mapper.getTypeFactory().constructFromCanonical(javaClass);
        } catch (Exception ignored) {
        }
        return null;
    }

    public interface IDeserializerErrorHandler {
        MessageRequest onError(String topic, Exception ex);
    }
}
