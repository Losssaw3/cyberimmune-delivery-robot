package ru.bardinpetr.delivery.messages.kafka;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.listener.ConcurrentMessageListenerContainer;
import org.springframework.kafka.listener.ContainerProperties;
import org.springframework.kafka.listener.MessageListener;
import ru.bardinpetr.delivery.messages.MessageRequest;
import ru.bardinpetr.delivery.messages.kafka.interfaces.ITopicListener;

import java.util.Map;

public class MonitoredKafkaConsumerService {

    private final Map<String, ITopicListener> listenerMap;
    private final ConcurrentMessageListenerContainer<String, MessageRequest> container;


    protected MonitoredKafkaConsumerService(DefaultKafkaConsumerFactory<String, MessageRequest> consumerFactory, Map<String, ITopicListener> listenerMap) {
        this.listenerMap = listenerMap;

        var topics = new String[listenerMap.size()];
        listenerMap.keySet().toArray(topics);

        var containerProperties = new ContainerProperties(topics);
        containerProperties.setMessageListener((MessageListener<String, MessageRequest>) this::onMessage);

        container = new ConcurrentMessageListenerContainer<>(consumerFactory, containerProperties);
    }

    public void start() {
        container.start();
    }

    private void onMessage(ConsumerRecord<String, MessageRequest> data) {
        var topic = data.topic();
        var message = data.value();
        if (!message.isValid()) return;

        listenerMap.get(topic).onMessage(message);
    }
}
