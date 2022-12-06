package ru.bardinpetr.delivery.monitor.kafka;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.support.serializer.FailedDeserializationInfo;
import ru.bardinpetr.delivery.libs.messages.MessageRequest;
import ru.bardinpetr.delivery.libs.messages.kafka.consumers.DeserializerFactory;
import ru.bardinpetr.delivery.libs.messages.kafka.consumers.MonitoredKafkaConsumerFactory;
import ru.bardinpetr.delivery.libs.messages.kafka.producers.MonitoredKafkaProducerFactory;

import java.util.Map;

public class MonitorTopicConsumerService {
    public static final String TOPIC = "monitor";
    private final MonitorProducerService producer;
    private final MonitorConsumerService consumer;

    public MonitorTopicConsumerService(Map<String, Object> kafkaConfig) {
        var producerFactory = MonitoredKafkaProducerFactory.getProducerFactory(kafkaConfig);
        producer = new MonitorProducerService(producerFactory);

        var deserializer = DeserializerFactory.getDeserializer(this::onDeserializeError);

        var consumerFactory = MonitoredKafkaConsumerFactory.getConsumerFactory(
                kafkaConfig, deserializer
        );
        consumer = new MonitorConsumerService(consumerFactory, this::onMessage);
    }

    private MessageRequest onDeserializeError(FailedDeserializationInfo info) {
        System.err.println(info);
        return null;
    }

    private void onMessage(ConsumerRecord<String, MessageRequest> msg) {
        var data = msg.value();

        if (!data.isValid()) {
            processInvalid(data);
            return;
        }

        System.out.println(data);
        producer.sendMessage(data);
    }

    private void processInvalid(MessageRequest data) {
        System.err.println(data);
    }

    public void start() {
        System.out.println("Started");
        consumer.start();
    }
}


