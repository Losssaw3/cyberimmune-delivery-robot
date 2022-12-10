package ru.bardinpetr.delivery.libs.messages;

import org.apache.kafka.clients.consumer.ConsumerConfig;
import ru.bardinpetr.delivery.libs.messages.kafka.consumers.MonitoredKafkaConsumerFactory;
import ru.bardinpetr.delivery.libs.messages.kafka.consumers.MonitoredKafkaConsumerServiceBuilder;
import ru.bardinpetr.delivery.libs.messages.kafka.consumers.MonitoredKafkaRequesterService;
import ru.bardinpetr.delivery.libs.messages.kafka.producers.MonitoredKafkaProducerFactory;
import ru.bardinpetr.delivery.libs.messages.kafka.producers.MonitoredKafkaProducerService;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class Main {


    public static void main(String[] args) throws ExecutionException, InterruptedException {
        Map<String, Object> configs = new HashMap<>();
        configs.put("bootstrap.servers", "localhost:9092");
        configs.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "latest");
        configs.put(ConsumerConfig.GROUP_ID_CONFIG, "cg-unit-%d".formatted(Math.round(Math.random() * 10e6)));

        var producerFactory = new MonitoredKafkaProducerFactory(configs);
        var kafkaConsumerFactory = new MonitoredKafkaConsumerFactory(configs);

        var rep = new MonitoredKafkaRequesterService(
                "test",
                List.of(ReplyableMessageRequest.class),
                producerFactory,
                kafkaConsumerFactory
        );

        var producer = new MonitoredKafkaProducerService("test2", producerFactory);
        var consumer = new MonitoredKafkaConsumerServiceBuilder("test2")
                .setConsumerFactory(kafkaConsumerFactory)
                .subscribe(ReplyableMessageRequest.class, i -> {
                    System.out.printf("OK %s", i);
                    producer.sendMessage(i.getSender(), i);
                })
                .build();

        var sched = Executors.newSingleThreadScheduledExecutor();
        sched.scheduleWithFixedDelay(() -> {
                    try {
                        System.out.println(rep.request("test2", new ReplyableMessageRequest()).get());
                    } catch (InterruptedException | ExecutionException e) {
                        throw new RuntimeException(e);
                    }
                },
                10, 30, TimeUnit.SECONDS);

        consumer.start();
        rep.start();
//
//        var sched = Executors.newSingleThreadScheduledExecutor();
//        sched.scheduleWithFixedDelay(() -> producer.sendMessage(
//                "authentication",
//                new CreatePINRequest("test")
//        ), 0, 10, TimeUnit.SECONDS);

//        producer.sendMessage("authentication", new CreatePINRequest("test"));

//

    }
}
