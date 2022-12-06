package ru.bardinpetr.delivery.messages;

import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.support.serializer.JsonDeserializer;
import org.springframework.kafka.support.serializer.JsonSerializer;
import ru.bardinpetr.delivery.messages.fms.Action1Request;
import ru.bardinpetr.delivery.messages.kafka.MonitoredKafkaConsumerFactory;
import ru.bardinpetr.delivery.messages.kafka.MonitoredKafkaConsumerServiceBuilder;
import ru.bardinpetr.delivery.messages.kafka.MonitoredKafkaProducerService;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class Main {


    public static void main(String[] args) throws ExecutionException, InterruptedException {
        Map<String, Object> configs = new HashMap<>();
        configs.put("bootstrap.servers", "localhost:9092");

        configs.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        configs.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, JsonSerializer.class);

        configs.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        configs.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, JsonDeserializer.class);
        configs.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "latest");
        configs.put(ConsumerConfig.GROUP_ID_CONFIG, "cg-unit-%d".formatted(Math.round(Math.random() * 10e6)));

        var producerFactory = new DefaultKafkaProducerFactory<String, MessageRequest>(configs);
        var producer = new MonitoredKafkaProducerService("test", producerFactory);

        var sched = Executors.newSingleThreadScheduledExecutor();
        sched.scheduleWithFixedDelay(
                () -> producer.sendMessage("smth", new Action1Request("qwertyi")),
                5, 3, TimeUnit.SECONDS
        );

        var kafkaConsumerFactory = MonitoredKafkaConsumerFactory.getConsumerFactory(configs);

        var consumer = new MonitoredKafkaConsumerServiceBuilder("srv0")
                .setConsumerFactory(kafkaConsumerFactory)
                .subscribe(Action1Request.class, System.out::println)
                .build();
        consumer.start();
    }
}
