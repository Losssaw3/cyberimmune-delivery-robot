package ru.bardinpetr.delivery.e2e.suite;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Assertions;
import ru.bardinpetr.delivery.common.libs.messages.msg.MessageRequest;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import java.util.function.Function;

@Slf4j
public class HistoryListener<T> {
    private final TopicHistory<T> hist;
    private final TimeUnit timeUnit;
    private final int timeout;
    private boolean isTakeEarliest = true;

    public HistoryListener(TopicHistory<T> history, int timeout, TimeUnit timeUnit) {
        this.hist = history;
        this.timeout = timeout;
        this.timeUnit = timeUnit;
    }

    /**
     * Returns the earliest message arrived to history queue
     *
     * @return future resolving to message or null on timeout
     */
    private CompletableFuture<T> take() {
        return (isTakeEarliest ? hist.takeFirst() : hist.takeLast())
                .completeOnTimeout(null, timeout, timeUnit);
    }

    /**
     * Set all commands depending on take() to use last element in queue
     */
    public HistoryListener<T> doTakeLast() {
        isTakeEarliest = false;
        return this;
    }

    /**
     * (default) Set all commands depending on take() to use first element in queue
     */
    public HistoryListener<T> doTakeFirst() {
        isTakeEarliest = true;
        return this;
    }

    /**
     * Tries to do takeFirst() (or takeLast if used) and asserts that timeout was not reached
     *
     * @param msg assert log message
     * @return if exists, the message received
     */
    public T assertArrived(String msg) throws ExecutionException, InterruptedException {
        var res = take().get();
        Assertions.assertNotNull(res, msg);
        log.debug("took from queue: {} {} when {}", res, ((MessageRequest) res).getTargetTopic(), hist.toString());
        return res;
    }

    /**
     * Tries to do takeFirst() and asserts that no message arrived unitll timeout
     *
     * @param msg assert log message
     */
    public void assertNotArrived(String msg) throws ExecutionException, InterruptedException {
        Assertions.assertNull(take().get(), msg);
    }

    /**
     * Tries to do takeFirst() and asserts that message arrived and that validator return true when called with message
     *
     * @param validator    boolean function checking message received. not called if timeout
     * @param nonExistsMsg assertion message when no message arrive
     * @param invalidMsg   assertion message when validation failed
     * @return if exists, the message received
     */
    public T assertArrivedThat(Function<T, Boolean> validator, String nonExistsMsg, String invalidMsg) throws ExecutionException, InterruptedException {
        var res = assertArrived(nonExistsMsg);
        Assertions.assertTrue(validator.apply(res), invalidMsg);
        return res;
    }

    /**
     * Tries to do takeFirst() and asserts that message arrived and that validator not throwing when called with message
     *
     * @param validator    this function should throw anything if message passed is not valid. not called if timeout
     * @param nonExistsMsg assertion message when no message arrive
     * @param invalidMsg   assertion message when validation failed
     * @return if exists, the message received
     */
    public T assertArrivedValidated(Consumer<T> validator, String nonExistsMsg, String invalidMsg) throws ExecutionException, InterruptedException {
        var res = assertArrived(nonExistsMsg);
        Assertions.assertAll(invalidMsg, () -> validator.accept(res));
        return res;
    }
}