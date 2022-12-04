package ru.bardinpetr.delivery.backend.flightmanager.kafka;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.ProducerFactory;
import org.springframework.kafka.listener.ConcurrentMessageListenerContainer;
import org.springframework.kafka.requestreply.ReplyingKafkaTemplate;
import ru.bardinpetr.delivery.messages.MessageRequest;


@Configuration
public class KafkaReplyingConfig {

    @Bean
    public ReplyingKafkaTemplate<String, MessageRequest, MessageRequest> replyingTemplate(
            ProducerFactory<String, MessageRequest> pf,
            ConcurrentMessageListenerContainer<String, MessageRequest> repliesContainer) {

        return new ReplyingKafkaTemplate<>(pf, repliesContainer);
    }

    @Bean
    public ConcurrentMessageListenerContainer<String, MessageRequest> repliesContainer(
            ConcurrentKafkaListenerContainerFactory<String, MessageRequest> containerFactory) {

        ConcurrentMessageListenerContainer<String, MessageRequest> repliesContainer =
                containerFactory.createContainer("fms_Action1Reply");
        repliesContainer.getContainerProperties().setGroupId("main");
        repliesContainer.setAutoStartup(false);
        return repliesContainer;
    }
}
