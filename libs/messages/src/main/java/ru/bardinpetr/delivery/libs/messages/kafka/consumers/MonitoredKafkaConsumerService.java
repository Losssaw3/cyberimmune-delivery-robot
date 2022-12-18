package ru.bardinpetr.delivery.libs.messages.kafka.consumers;

import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.listener.ConcurrentMessageListenerContainer;
import org.springframework.kafka.listener.ContainerProperties;
import org.springframework.kafka.listener.MessageListener;
import org.springframework.kafka.support.TopicPartitionOffset;
import ru.bardinpetr.delivery.libs.messages.kafka.interfaces.ITopicListener;
import ru.bardinpetr.delivery.libs.messages.msg.MessageRequest;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

@Slf4j
public class MonitoredKafkaConsumerService extends Thread {

    private final Set<String> receivedMessageIds;
    private final Map<String, ITopicListener<?>> listenerMap;
    private final ConcurrentMessageListenerContainer<String, MessageRequest> container;


    public MonitoredKafkaConsumerService(MonitoredKafkaConsumerFactory consumerFactory,
                                         Map<String, ITopicListener<?>> listenerMap) {
        this.listenerMap = listenerMap;
        receivedMessageIds = new HashSet<>();

        var topics =
                listenerMap
                        .keySet().stream()
                        .map(i -> new TopicPartitionOffset(i, 0, TopicPartitionOffset.SeekPosition.END))
                        .toArray(TopicPartitionOffset[]::new);

        var containerProperties = new ContainerProperties(topics);
        containerProperties.setMessageListener((MessageListener<String, MessageRequest>) this::onMessage);

        container = new ConcurrentMessageListenerContainer<>(consumerFactory, containerProperties);
    }

    @Override
    public void run() {
        container.start();
    }

    @SuppressWarnings("unchecked")
    private <T extends MessageRequest> void onMessage(ConsumerRecord<String, T> data) {
        var topic = data.topic();
        var message = data.value();
        if (!message.isValid()) return;

        if (!receivedMessageIds.add(message.getRequestId())) {
            log.debug("[RECV] got copy of ID{}", message.getRequestId());
            return;
        }

        log.debug("[RECV] from {} msg: {}", message.getSender(), message);

        ((ITopicListener<T>) listenerMap.get(topic)).onMessage(message);
    }
}
