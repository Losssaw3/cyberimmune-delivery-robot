package ru.bardinpetr.delivery.libs.messages.kafka.consumers;

import ru.bardinpetr.delivery.libs.messages.MessageRequest;
import ru.bardinpetr.delivery.libs.messages.ReplyableMessageRequest;
import ru.bardinpetr.delivery.libs.messages.kafka.interfaces.ITopicListener;
import ru.bardinpetr.delivery.libs.messages.kafka.producers.MonitoredKafkaProducerFactory;
import ru.bardinpetr.delivery.libs.messages.kafka.producers.MonitoredKafkaProducerService;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.stream.Collectors;

public class MonitoredKafkaRequesterService extends Thread {

    private final ConcurrentMap<Integer, CompletableFuture<ReplyableMessageRequest>> futures;

    private final List<Class<? extends ReplyableMessageRequest>> consumedMessageTypes;

    private final MonitoredKafkaProducerService producer;
    private final MonitoredKafkaConsumerService consumer;

    private int messageId = 0;

    public MonitoredKafkaRequesterService(
            String selfServiceName,
            List<Class<? extends ReplyableMessageRequest>> consumedMessageTypes,
            MonitoredKafkaProducerFactory producerFactory,
            MonitoredKafkaConsumerFactory consumerFactory) {
        this.consumedMessageTypes = consumedMessageTypes;

        futures = new ConcurrentHashMap<>();

        final ITopicListener<? extends ReplyableMessageRequest> listener = this::onReplyMessage;
        var listeners =
                consumedMessageTypes.stream()
                        .collect(Collectors.toMap(
                                cls -> MessageRequest.getTargetTopic(cls, selfServiceName),
                                cls -> (ITopicListener) listener
                        ));

        consumer = new MonitoredKafkaConsumerService(
                consumerFactory,
                listeners
        );
        producer = new MonitoredKafkaProducerService(selfServiceName, producerFactory);
    }

    public void run() {
        consumer.run();
    }

    public CompletableFuture<ReplyableMessageRequest> request(String recipient, ReplyableMessageRequest request) {
        var id = messageId++;
        request.setRequestId(id);
        producer.sendMessage(recipient, request);

        var future = new CompletableFuture<ReplyableMessageRequest>();
        futures.put(id, future);
        return future;
    }

    private void onReplyMessage(ReplyableMessageRequest message) {
        var id = message.getRequestId();

        var future = futures.get(id);
        if (future == null) return;

        if (!message.isValid())
            future.completeExceptionally(new RuntimeException("message invalid"));
        else
            future.complete(message);
        futures.remove(id);
    }
}
