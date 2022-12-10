package ru.bardinpetr.delivery.libs.messages.kafka.consumers;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.listener.ConcurrentMessageListenerContainer;
import org.springframework.kafka.listener.ContainerProperties;
import org.springframework.kafka.listener.MessageListener;
import ru.bardinpetr.delivery.libs.messages.MessageRequest;
import ru.bardinpetr.delivery.libs.messages.kafka.interfaces.ITopicListener;

import java.util.Map;

public class MonitoredKafkaConsumerService { //extends Thread {

    private final Map<String, ITopicListener> listenerMap;
    private final ConcurrentMessageListenerContainer<String, MessageRequest> container;


    protected MonitoredKafkaConsumerService(MonitoredKafkaConsumerFactory consumerFactory, Map<String, ITopicListener> listenerMap) {
        this.listenerMap = listenerMap;

        var topics = new String[listenerMap.size()];
        listenerMap.keySet().toArray(topics);

        var containerProperties = new ContainerProperties(topics);
        containerProperties.setMessageListener((MessageListener<String, MessageRequest>) this::onMessage);

        container = new ConcurrentMessageListenerContainer<>(consumerFactory, containerProperties);
    }

    //    @Override
    public void start() {
        container.start();
    }

    private <T extends MessageRequest> void onMessage(ConsumerRecord<String, T> data) {
        var topic = data.topic();
        var message = data.value();
        if (!message.isValid()) return;

        System.out.printf("[RECV] from %s msg: %s", message.getSender(), message);

        listenerMap.get(topic).onMessage(message);
    }
}
