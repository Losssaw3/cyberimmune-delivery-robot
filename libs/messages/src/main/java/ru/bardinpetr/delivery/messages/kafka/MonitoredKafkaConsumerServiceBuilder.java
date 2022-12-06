package ru.bardinpetr.delivery.messages.kafka;

import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import ru.bardinpetr.delivery.messages.MessageRequest;
import ru.bardinpetr.delivery.messages.kafka.interfaces.ITopicListener;

import java.util.HashMap;
import java.util.Map;

public class MonitoredKafkaConsumerServiceBuilder {
    private final Map<String, ITopicListener> listenerMap = new HashMap<>();
    private final String selfTopicName;
    private DefaultKafkaConsumerFactory<String, MessageRequest> consumerFactory;

    public MonitoredKafkaConsumerServiceBuilder(String selfTopicName) {
        this.selfTopicName = selfTopicName;
    }

    public MonitoredKafkaConsumerServiceBuilder setConsumerFactory(DefaultKafkaConsumerFactory<String, MessageRequest> consumerFactory) {
        this.consumerFactory = consumerFactory;
        return this;
    }

    public MonitoredKafkaConsumerServiceBuilder subscribe(Class<? extends MessageRequest> topicType, ITopicListener listener) {
        var topic = MessageRequest.getTargetTopic(topicType, selfTopicName);
        this.listenerMap.put(topic, listener);
        return this;
    }

    public MonitoredKafkaConsumerService build() {
        return new MonitoredKafkaConsumerService(consumerFactory, listenerMap);
    }
}