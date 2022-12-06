package ru.bardinpetr.delivery.monitor;

import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.streams.StreamsConfig;

import java.util.Map;
import java.util.Properties;

public class Configuration {

    private static final Map<String, String> environ = System.getenv();

    public static String getKafkaURI() {
        return environ.getOrDefault("KAFKA_BOOTSTRAP_SERVER", "localhost:9092");
    }

    public static Properties getKafkaConsumerParams() {
        Properties props = new Properties();

        props.put(StreamsConfig.APPLICATION_ID_CONFIG, "monitor-main");
        props.put(StreamsConfig.CLIENT_ID_CONFIG, "monitor-%s".formatted(Math.round(Math.random() * 10e6)));

        props.put(StreamsConfig.BOOTSTRAP_SERVERS_CONFIG, Configuration.getKafkaURI());

        props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
        props.put(StreamsConfig.DEFAULT_KEY_SERDE_CLASS_CONFIG, Serdes.String().getClass());
        props.put(StreamsConfig.DEFAULT_VALUE_SERDE_CLASS_CONFIG, Serdes.String().getClass());

        return props;
    }

}
