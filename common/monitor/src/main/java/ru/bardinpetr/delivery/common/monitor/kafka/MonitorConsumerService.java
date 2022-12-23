package ru.bardinpetr.delivery.common.monitor.kafka;

import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.listener.ConcurrentMessageListenerContainer;
import org.springframework.kafka.listener.ContainerProperties;
import org.springframework.kafka.listener.MessageListener;
import ru.bardinpetr.delivery.common.libs.messages.msg.MessageRequest;

public class MonitorConsumerService {

    private final ConcurrentMessageListenerContainer<String, MessageRequest> container;

    public MonitorConsumerService(DefaultKafkaConsumerFactory<String, MessageRequest> consumerFactory,
                                  MessageListener<String, MessageRequest> listener) {
        var containerProperties = new ContainerProperties("monitor");
        containerProperties.setMessageListener(listener);

        container = new ConcurrentMessageListenerContainer<>(consumerFactory, containerProperties);
    }

    public void start() {
        container.start();
    }
}
