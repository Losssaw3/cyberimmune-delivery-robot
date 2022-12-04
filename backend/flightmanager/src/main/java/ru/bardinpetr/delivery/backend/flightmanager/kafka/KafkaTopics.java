package ru.bardinpetr.delivery.backend.flightmanager.kafka;

import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.type.TypeFactory;
import org.apache.kafka.common.header.Headers;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import ru.bardinpetr.delivery.messages.MessageRequest;
import ru.bardinpetr.delivery.messages.fms.Action1Request;

import java.util.List;
import java.util.Map;
import java.util.function.Predicate;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Configuration
public class KafkaTopics {

    private static final TypeFactory jsonTypeFactory = TypeFactory.defaultInstance();
    private static final List<Class<? extends MessageRequest>> incomingMessageTypes =
            List.of(Action1Request.Reply.class, Action1Request.class);
    @Value("ru.bardinpetr.delivery.kafka-base-topic")
    private static String baseTopic;
    private static final Map<Predicate<String>, JavaType> incomingTopicType =
            incomingMessageTypes
                    .stream()
                    .collect(Collectors.toMap(
                                    cls -> Pattern.compile("%s_%s_%s".formatted(
                                                    baseTopic,
                                                    "request",
                                                    cls.getSimpleName()))
                                            .asMatchPredicate(),
                                    jsonTypeFactory::constructType
                            )
                    );

    public static JavaType incomingTypeMapper(String topic, byte[] data, Headers headers) {
        return incomingTopicType
                .entrySet().stream()
                .filter(i -> i.getKey().test(topic))
                .map(Map.Entry::getValue)
                .findFirst()
                .orElseGet(() -> jsonTypeFactory.constructType(MessageRequest.class));
    }

    public static String getTopicForClass() {
        return " ";
    }

//    @Bean
    public String incomingTopicPrefix() {
        return "%s_reply".formatted(baseTopic);
    }
}
