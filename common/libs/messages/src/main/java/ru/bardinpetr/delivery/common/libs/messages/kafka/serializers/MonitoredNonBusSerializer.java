package ru.bardinpetr.delivery.common.libs.messages.kafka.serializers;

import ru.bardinpetr.delivery.common.libs.messages.msg.MessageRequest;

public class MonitoredNonBusSerializer {

    private final MonitoredSerializer serializer = new MonitoredSerializer();

    public byte[] serialize(MessageRequest request) {
        return serializer.serialize("", request);
    }

}
