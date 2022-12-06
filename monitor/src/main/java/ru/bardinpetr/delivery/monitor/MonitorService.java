package ru.bardinpetr.delivery.monitor;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.support.serializer.FailedDeserializationInfo;
import ru.bardinpetr.delivery.libs.messages.MessageRequest;
import ru.bardinpetr.delivery.libs.messages.kafka.consumers.DeserializerFactory;
import ru.bardinpetr.delivery.libs.messages.kafka.consumers.MonitoredKafkaConsumerFactory;
import ru.bardinpetr.delivery.libs.messages.kafka.producers.MonitoredKafkaProducerFactory;
import ru.bardinpetr.delivery.monitor.kafka.MonitorConsumerService;
import ru.bardinpetr.delivery.monitor.kafka.MonitorProducerService;
import ru.bardinpetr.delivery.monitor.validator.IValidator;

import java.util.Map;

public class MonitorService {
    public static final String TOPIC = "monitor";
    private final MonitorProducerService producer;
    private final MonitorConsumerService consumer;

    private final IValidator[] validators;

    public MonitorService(Map<String, Object> kafkaConfig, IValidator[] validators) {
        this.validators = validators;

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

    private boolean check(MessageRequest data) {
        if (!data.isValid()) return false;

        for (var validator : validators)
            if (!validator.verify(data)) return false;

        return true;
    }

    private void onMessage(ConsumerRecord<String, MessageRequest> msg) {
        var data = msg.value();

        if (!check(data)) {
            processInvalid(data);
            return;
        }

        System.out.printf("Allowed message: %s", data);
        producer.sendMessage(data);
    }

    private void processInvalid(MessageRequest data) {
        System.err.printf("Rejected message: %s", data);
    }

    public void start() {
        System.out.println("Started");
        consumer.start();
    }
}


