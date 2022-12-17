package ru.bardinpetr.delivery.libs.messages.kafka.consumers;

import org.apache.kafka.common.serialization.StringDeserializer;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.support.serializer.ErrorHandlingDeserializer;
import ru.bardinpetr.delivery.libs.messages.kafka.deserializers.MonitoredDeserializer;
import ru.bardinpetr.delivery.libs.messages.msg.MessageRequest;

import java.util.Map;

public class MonitoredKafkaConsumerFactory extends DefaultKafkaConsumerFactory<String, MessageRequest> {


    public MonitoredKafkaConsumerFactory(Map<String, Object> configs, ErrorHandlingDeserializer<MessageRequest> deserializer) {
        super(
                configs,
                new StringDeserializer(),
                deserializer);
    }

    public MonitoredKafkaConsumerFactory(Map<String, Object> configs) {
        super(
                configs,
                new StringDeserializer(),
                new MonitoredDeserializer());
    }

}
