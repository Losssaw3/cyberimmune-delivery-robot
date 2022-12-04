package ru.bardinpetr.cyberimmune_delivery.monitor.kafka;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.kafka.streams.KafkaStreams;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.kstream.KStream;
import org.apache.kafka.streams.processor.RecordContext;
import ru.bardinpetr.cyberimmune_delivery.monitor.Configuration;

public class MonitorTopicConsumer implements AutoCloseable {
    public static final String TOPIC = "monitor";
    private final KafkaStreams streams;
    private final ObjectMapper jsonMapper;

    public MonitorTopicConsumer() {
        var params = Configuration.getKafkaConsumerParams();

        StreamsBuilder streamsBuilder = new StreamsBuilder();
        KStream<String, String> inputStream = streamsBuilder.stream(TOPIC);

        jsonMapper = new ObjectMapper();

        inputStream
                .filter(this::verify)
                .to(this::selectTopic);

        streams = new KafkaStreams(streamsBuilder.build(), params);
    }

    private boolean verify(String action, String data) {
        System.out.printf("%s: %s\n", action, data);
        return true;
    }

    private String selectTopic(String action, String data, RecordContext context) {
        return "out";
    }

    public void start() {
        streams.cleanUp();
        streams.start();
        System.out.println("Started");
    }

    @Override
    public void close() {
        streams.close();
    }
}


