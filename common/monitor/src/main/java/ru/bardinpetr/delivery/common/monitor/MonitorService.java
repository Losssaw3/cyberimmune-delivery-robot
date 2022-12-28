package ru.bardinpetr.delivery.common.monitor;

import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import ru.bardinpetr.delivery.common.libs.messages.kafka.consumers.MonitoredKafkaConsumerFactory;
import ru.bardinpetr.delivery.common.libs.messages.kafka.deserializers.MonitoredDeserializer;
import ru.bardinpetr.delivery.common.libs.messages.kafka.producers.MonitoredKafkaProducerFactory;
import ru.bardinpetr.delivery.common.libs.messages.msg.MessageRequest;
import ru.bardinpetr.delivery.common.monitor.kafka.MonitorConsumerService;
import ru.bardinpetr.delivery.common.monitor.kafka.MonitorProducerService;
import ru.bardinpetr.delivery.common.monitor.validator.IValidator;

import java.util.Map;

@Slf4j
public class MonitorService {

    private final MonitorProducerService producer;
    private final MonitorConsumerService consumer;

    private final IValidator[] validators;

    public MonitorService(Map<String, Object> kafkaConfig, IValidator[] validators) {
        this.validators = validators;

        producer = new MonitorProducerService(
                new MonitoredKafkaProducerFactory(kafkaConfig)
        );

        var deserializer = new MonitoredDeserializer(this::onDeserializeError);

        var consumerFactory = new MonitoredKafkaConsumerFactory(
                kafkaConfig, deserializer
        );
        consumer = new MonitorConsumerService(consumerFactory, this::onMessage);
    }

    private MessageRequest onDeserializeError(String topic, Exception ex) {
        log.debug("[MON-ERR] invalid message on {} : {}", topic, ex);
        log.warn("[MON-ERR] invalid message on topic {}", topic, ex);
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

        log.debug("[MON-ALLOW] from {} to {}: {}", data.getSender(), data.getRecipient(), data);
        log.info("[MON-ALLOW] {}->{} : {}", data.getSender(), data.getRecipient(), data.getActionType());
        producer.sendMessage(data);
    }

    private void processInvalid(MessageRequest data) {
        log.debug("[MON-DENY] from {} to {}: {}", data.getSender(), data.getRecipient(), data);
        log.info("[MON-DENY] {}->{} : {}", data.getSender(), data.getRecipient(), data.getActionType());
    }

    public void start() {
        consumer.start();
        log.debug("[MON] started");
    }
}


