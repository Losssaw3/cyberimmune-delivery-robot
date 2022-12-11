package ru.bardinpetr.delivery.libs.messages.kafka.consumers;

import ru.bardinpetr.delivery.libs.messages.kafka.interfaces.ITopicListener;
import ru.bardinpetr.delivery.libs.messages.msg.MessageRequest;

import java.util.HashMap;
import java.util.Map;

public class MonitoredKafkaConsumerServiceBuilder {
    private final Map<String, ITopicListener<?>> listenerMap = new HashMap<>();
    private final String selfTopicName;
    private MonitoredKafkaConsumerFactory consumerFactory;

    public MonitoredKafkaConsumerServiceBuilder(String selfTopicName) {
        this.selfTopicName = selfTopicName;
    }

    public MonitoredKafkaConsumerServiceBuilder setConsumerFactory(MonitoredKafkaConsumerFactory consumerFactory) {
        this.consumerFactory = consumerFactory;
        return this;
    }

    public <T extends MessageRequest> MonitoredKafkaConsumerServiceBuilder subscribe(Class<T> topicType, ITopicListener<T> listener) {
        var topic = MessageRequest.getTargetTopic(topicType, selfTopicName);
        this.listenerMap.put(topic, listener);
        return this;
    }

    public MonitoredKafkaConsumerService build() {
        return new MonitoredKafkaConsumerService(consumerFactory, listenerMap);
    }
}