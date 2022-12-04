package ru.bardinpetr.delivery.backend.flightmanager.kafka;

import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.support.serializer.JsonDeserializer;
import ru.bardinpetr.delivery.messages.MessageRequest;

import java.util.HashMap;

@Configuration
public class KafkaConsumerConfig {
    @Value(value = "${spring.kafka.bootstrap-servers}")
    private String bootstrapAddress;

    @Bean
    public ConsumerFactory<String, MessageRequest> consumerFactory() {
        var conf = new HashMap<String, Object>();
        conf.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapAddress);
        conf.put(ConsumerConfig.GROUP_ID_CONFIG, "main");

        var deserializer =
                new JsonDeserializer<>(MessageRequest.class)
                        .typeResolver(KafkaTopics::incomingTypeMapper);

        return new DefaultKafkaConsumerFactory<>(
                conf,
                new StringDeserializer(),
                deserializer
        );
    }
}
