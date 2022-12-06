package ru.bardinpetr.delivery.monitor.kafka;

import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;
import org.springframework.kafka.support.SendResult;
import org.springframework.util.concurrent.ListenableFuture;
import ru.bardinpetr.delivery.libs.messages.MessageRequest;

public class MonitorProducerService {
    private final KafkaTemplate<String, MessageRequest> kafkaTemplate;

    public MonitorProducerService(ProducerFactory<String, MessageRequest> producerFactory) {
        this.kafkaTemplate = new KafkaTemplate<>(producerFactory);
    }

    public ListenableFuture<SendResult<String, MessageRequest>> sendMessage(MessageRequest request) {
        request.setVerified(true);
        return kafkaTemplate.send(request.getTargetTopic(), request);
    }
}

