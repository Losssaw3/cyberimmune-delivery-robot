package ru.bardinpetr.delivery.libs.messages.kafka.consumers;

import ru.bardinpetr.delivery.libs.messages.kafka.interfaces.ITopicListener;
import ru.bardinpetr.delivery.libs.messages.kafka.producers.MonitoredKafkaProducerFactory;
import ru.bardinpetr.delivery.libs.messages.kafka.producers.MonitoredKafkaProducerService;
import ru.bardinpetr.delivery.libs.messages.msg.MessageRequest;
import ru.bardinpetr.delivery.libs.messages.msg.ReplyableMessageRequest;

import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.stream.Collectors;

public class MonitoredKafkaRequesterService extends Thread {

    private final ConcurrentMap<String, CompletableFuture<MessageRequest>> futures;

    private final MonitoredKafkaProducerService producer;
    private final MonitoredKafkaConsumerService consumer;

    private final int messageId = 0;

    public MonitoredKafkaRequesterService(
            String selfServiceName,
            List<Class<? extends MessageRequest>> consumedMessageTypes,
            MonitoredKafkaProducerFactory producerFactory,
            MonitoredKafkaConsumerFactory consumerFactory) {

        futures = new ConcurrentHashMap<>();

        final ITopicListener<MessageRequest> listener = this::onReplyMessage;
        Map<String, ITopicListener<?>> listeners =
                consumedMessageTypes.stream()
                        .collect(Collectors.toMap(
                                cls -> MessageRequest.getTargetTopic(cls, selfServiceName),
                                cls -> listener
                        ));

        consumer = new MonitoredKafkaConsumerService(
                consumerFactory,
                listeners
        );
        producer = new MonitoredKafkaProducerService(selfServiceName, producerFactory);
    }

    @Override
    public void run() {
        consumer.start();
    }

    public CompletableFuture<MessageRequest> request(String recipient, ReplyableMessageRequest request) {
        var id = producer.sendMessage(recipient, request);

        var future = new CompletableFuture<MessageRequest>();
        futures.put(MessageRequest.getReplyMessageID(id), future);
        return future;
    }

    private void onReplyMessage(MessageRequest message) {
        var id = message.getRequestId();
        System.out.printf("[RS_RECV] ID%s msg %s\n", id, message);

        var future = futures.get(id);
        if (future == null)
            throw new RuntimeException("Got message with invalid identifier. Could not find listener");

        if (!message.isValid())
            future.completeExceptionally(new RuntimeException("message invalid"));
        else
            future.complete(message);
        futures.remove(id);
    }

}
