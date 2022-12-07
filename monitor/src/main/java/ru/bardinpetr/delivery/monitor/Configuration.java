package ru.bardinpetr.delivery.monitor;

import org.apache.kafka.clients.consumer.ConsumerConfig;

import java.util.HashMap;
import java.util.Map;

public class Configuration {

    private static final Map<String, String> environ = System.getenv();

    public static String getKafkaURI() {
        return environ.getOrDefault("KAFKA_BOOTSTRAP_SERVER", "localhost:9092");
    }

    public static Map<String, Object> getKafkaGlobalParams() {
        var props = new HashMap<String, Object>();
        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, getKafkaURI());
        props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "latest");
        props.put(ConsumerConfig.GROUP_ID_CONFIG, "monitor-%d".formatted(Math.round(Math.random() * 10e6)));
        return props;
    }

}
