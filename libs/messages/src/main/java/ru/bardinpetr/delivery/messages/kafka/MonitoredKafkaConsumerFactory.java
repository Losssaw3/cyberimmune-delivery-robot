package ru.bardinpetr.delivery.messages.kafka;

import org.apache.kafka.common.serialization.StringDeserializer;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.support.serializer.ErrorHandlingDeserializer;
import org.springframework.kafka.support.serializer.JsonDeserializer;
import ru.bardinpetr.delivery.messages.InvalidMessageRequest;
import ru.bardinpetr.delivery.messages.MessageRequest;

import java.util.Map;

public class MonitoredKafkaConsumerFactory {

    public static DefaultKafkaConsumerFactory<String, MessageRequest> getConsumerFactory(Map<String, Object> configs) {
        return new DefaultKafkaConsumerFactory<>(
                configs,
                new StringDeserializer(),
                getDeserializer()
        );
    }

    public static ErrorHandlingDeserializer<MessageRequest> getDeserializer() {
        var jsonDeserializer = new JsonDeserializer<MessageRequest>();
        jsonDeserializer.addTrustedPackages("*");

        var errorDeserializer = new ErrorHandlingDeserializer<>(jsonDeserializer);
        errorDeserializer.setFailedDeserializationFunction(info -> {
            System.err.println(info);
            return new InvalidMessageRequest();
        });

        return errorDeserializer;
    }
}
