package ru.bardinpetr.delivery.libs.messages.kafka.producers;

import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.support.serializer.JsonSerializer;
import ru.bardinpetr.delivery.libs.messages.MessageRequest;

import java.util.Map;

public class MonitoredKafkaProducerFactory extends DefaultKafkaProducerFactory<String, MessageRequest> {

    public MonitoredKafkaProducerFactory(Map<String, Object> configs) {
        super(configs,
                new StringSerializer(),
                new JsonSerializer<>()
        );
    }
}
