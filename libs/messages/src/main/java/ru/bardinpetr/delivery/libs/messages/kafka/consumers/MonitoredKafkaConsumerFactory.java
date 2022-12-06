package ru.bardinpetr.delivery.libs.messages.kafka.consumers;

import org.apache.kafka.common.serialization.StringDeserializer;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.support.serializer.ErrorHandlingDeserializer;
import ru.bardinpetr.delivery.libs.messages.MessageRequest;

import java.util.Map;

import static ru.bardinpetr.delivery.libs.messages.kafka.consumers.DeserializerFactory.getDeserializer;

public class MonitoredKafkaConsumerFactory {


    public static DefaultKafkaConsumerFactory<String, MessageRequest> getConsumerFactory(Map<String, Object> configs) {
        return new DefaultKafkaConsumerFactory<>(
                configs,
                new StringDeserializer(),
                getDeserializer()
        );
    }

    public static DefaultKafkaConsumerFactory<String, MessageRequest> getConsumerFactory(
            Map<String, Object> configs,
            ErrorHandlingDeserializer<MessageRequest> deserializer
    ) {
        return new DefaultKafkaConsumerFactory<>(
                configs,
                new StringDeserializer(),
                deserializer
        );
    }

}
