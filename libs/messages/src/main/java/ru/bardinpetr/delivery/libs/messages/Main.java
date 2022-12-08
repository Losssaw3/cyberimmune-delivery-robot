package ru.bardinpetr.delivery.libs.messages;

import org.apache.kafka.clients.consumer.ConsumerConfig;
import ru.bardinpetr.delivery.libs.messages.kafka.producers.MonitoredKafkaProducerFactory;
import ru.bardinpetr.delivery.libs.messages.kafka.producers.MonitoredKafkaProducerService;
import ru.bardinpetr.delivery.libs.messages.models.authentication.CreatePINRequest;

import java.util.HashMap;
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
        var producer = new MonitoredKafkaProducerService("test", producerFactory);

        var sched = Executors.newSingleThreadScheduledExecutor();
        sched.scheduleWithFixedDelay(() -> producer.sendMessage(
                "authentication",
                new CreatePINRequest("test")
        ), 0, 10, TimeUnit.SECONDS);

//        producer.sendMessage("authentication", new CreatePINRequest("test"));

//        var kafkaConsumerFactory = new MonitoredKafkaConsumerFactory(configs);
//
//        var consumer = new MonitoredKafkaConsumerServiceBuilder("srv0")
//                .setConsumerFactory(kafkaConsumerFactory)
//                .subscribe(Action1Request.class, System.out::println)
//                .build();
//        consumer.start();
    }
}
