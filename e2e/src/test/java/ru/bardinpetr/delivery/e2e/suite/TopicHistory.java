package ru.bardinpetr.delivery.e2e.suite;

import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;

import java.util.ArrayDeque;
import java.util.Queue;
import java.util.concurrent.CompletableFuture;

@Slf4j
public class TopicHistory<T> {
    private final Queue<ConsumerRecord<String, T>> queue = new ArrayDeque<>();

    private boolean isConsumerWaiting = false;
    private CompletableFuture<T> listener;

    public void put(ConsumerRecord<String, T> msg) {
        if (isConsumerWaiting) {
            log.debug("directly sent to consumer: {}: {}", msg.topic(), msg.value());
            isConsumerWaiting = false;
            listener.complete(msg.value());
            return;
        }
        log.debug("added to queue: {}: {}", msg.topic(), msg.value());
        queue.add(msg);
    }

    public CompletableFuture<T> takeFirst() {
        if (isConsumerWaiting)
            throw new RuntimeException("It is not intended to watch TopicHistory changes from more than one point.");

        if (!queue.isEmpty())
            return CompletableFuture.completedFuture(queue.remove().value());

        isConsumerWaiting = true;
        listener = new CompletableFuture<>();
        return listener;
    }

    public void flush() {
        queue.clear();
        isConsumerWaiting = false;
        listener = null;
    }

    @Override
    public String toString() {
        return "TopicHistory{%s}".formatted(queue);
    }
}
