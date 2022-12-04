package ru.bardinpetr.delivery.backend.flightmanager.kafka;


import org.apache.kafka.clients.admin.AdminClientConfig;
import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;
import org.springframework.kafka.core.KafkaAdmin;

import java.util.HashMap;
import java.util.Map;

@Configuration
public class KafkaConfig {

    @Value(value = "${spring.kafka.bootstrap-servers}")
    private String bootstrapAddress;

    @Bean
    public Map<String, Object> globalKafkaConfig() {
        var conf = new HashMap<String, Object>();
        conf.put(AdminClientConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapAddress);
        return conf;
    }

    @Bean
    public KafkaAdmin kafkaAdmin() {
        var conf = globalKafkaConfig();
        return new KafkaAdmin(conf);
    }

    @Bean
    public NewTopic monitorTopic() {
        return new NewTopic("monitor", 1, (short) 1);
    }

    @Bean
    public NewTopic fmsAction1Reply() {
        return new NewTopic("fms_Action1Reply", 1, (short) 1);
    }

    @Bean
    public NewTopic fmsAction1Request() {
        return new NewTopic("fms_Action1Request", 1, (short) 1);
    }

    @Bean
    public String getBootstrapAddress() {
        return bootstrapAddress;
    }
}
