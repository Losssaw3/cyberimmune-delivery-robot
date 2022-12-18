package ru.bardinpetr.delivery.libs.messages.kafka.producers;

import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import ru.bardinpetr.delivery.libs.messages.kafka.serializers.MonitoredSerializer;
import ru.bardinpetr.delivery.libs.messages.msg.MessageRequest;

import java.util.Map;

public class MonitoredKafkaProducerFactory extends DefaultKafkaProducerFactory<String, MessageRequest> {

    public MonitoredKafkaProducerFactory(Map<String, Object> configs) {
        super(configs);

        setKeySerializer(new StringSerializer());
        setValueSerializer(new MonitoredSerializer());
    }
}
