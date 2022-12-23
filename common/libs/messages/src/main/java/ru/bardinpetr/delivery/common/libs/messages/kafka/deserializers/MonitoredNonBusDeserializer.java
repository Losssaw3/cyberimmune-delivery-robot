package ru.bardinpetr.delivery.common.libs.messages.kafka.deserializers;

import ru.bardinpetr.delivery.common.libs.messages.msg.MessageRequest;

import java.nio.charset.StandardCharsets;

public class MonitoredNonBusDeserializer {

    private final MonitoredDeserializer deser = new MonitoredDeserializer();

    public MessageRequest deserialize(String data) {
        return deserialize(data.getBytes(StandardCharsets.UTF_8));
    }

    public MessageRequest deserialize(byte[] data) {
        return deser.deserialize("", data);
    }
}
