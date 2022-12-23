package ru.bardinpetr.delivery.common.libs.messages.kafka;

import org.apache.kafka.clients.consumer.ConsumerConfig;

import java.util.HashMap;
import java.util.Map;

public class CommonKafkaConfiguration {
    public static Map<String, Object> getKafkaGlobalParams(String brokerUri, String groupId) {
        var props = new HashMap<String, Object>();
        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, brokerUri);
        props.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, "true");
        props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "latest");
        props.put(ConsumerConfig.GROUP_ID_CONFIG, groupId);
        return props;
    }
}
