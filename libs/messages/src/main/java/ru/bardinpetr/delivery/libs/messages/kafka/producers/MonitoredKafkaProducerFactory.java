package ru.bardinpetr.delivery.libs.messages.kafka.producers;

import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.support.serializer.JsonSerializer;
import ru.bardinpetr.delivery.libs.messages.msg.MessageRequest;

import java.util.Map;

public class MonitoredKafkaProducerFactory extends DefaultKafkaProducerFactory<String, MessageRequest> {

    public MonitoredKafkaProducerFactory(Map<String, Object> configs) {
        super(configs);

        setKeySerializer(new StringSerializer());

        var serializer = new JsonSerializer<MessageRequest>();
        serializer.noTypeInfo();
        setValueSerializer(serializer);
    }
}
