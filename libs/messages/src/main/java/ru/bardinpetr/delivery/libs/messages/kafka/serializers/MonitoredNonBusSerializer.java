package ru.bardinpetr.delivery.libs.messages.kafka.serializers;

import ru.bardinpetr.delivery.libs.messages.msg.MessageRequest;

public class MonitoredNonBusSerializer {

    private final MonitoredSerializer serializer = new MonitoredSerializer();

    public byte[] serialize(MessageRequest request) {
        return serializer.serialize("", request);
    }

}
