package ru.bardinpetr.delivery.monitor;

import org.apache.kafka.clients.consumer.ConsumerConfig;
import ru.bardinpetr.delivery.monitor.kafka.MonitorTopicConsumerService;

import java.util.HashMap;
import java.util.Map;

public class Main {

    public static void main(String[] args) {
        Map<String, Object> configs = new HashMap<>();
        configs.put("bootstrap.servers", "localhost:9092");
        configs.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "latest");
        configs.put(ConsumerConfig.GROUP_ID_CONFIG, "monitor-%d".formatted(Math.round(Math.random() * 10e6)));

        var consumer = new MonitorTopicConsumerService(configs);
        consumer.start();
    }
}
