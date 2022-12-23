package ru.bardinpetr.delivery.common.communication;

import ru.bardinpetr.delivery.common.communication.client.CommHTTPClientService;
import ru.bardinpetr.delivery.common.communication.server.CommHTTPServerService;
import ru.bardinpetr.delivery.common.libs.messages.kafka.CommonKafkaConfiguration;
import ru.bardinpetr.delivery.common.libs.messages.kafka.consumers.MonitoredKafkaConsumerFactory;
import ru.bardinpetr.delivery.common.libs.messages.kafka.producers.MonitoredKafkaProducerFactory;
import ru.bardinpetr.delivery.common.libs.messages.msg.MessageRequest;

import java.util.ArrayList;

import static ru.bardinpetr.delivery.common.communication.MainService.SERVICE_NAME;

public class Main {

    @SuppressWarnings("unchecked")
    public static void main(String[] args) {
        var kafkaConfig = CommonKafkaConfiguration.getKafkaGlobalParams(
                Configuration.getKafkaURI(),
                SERVICE_NAME
        );
        var producerFactory = new MonitoredKafkaProducerFactory(kafkaConfig);
        var consumerFactory = new MonitoredKafkaConsumerFactory(kafkaConfig);

        var serverService = new CommHTTPServerService(Configuration.getPort());
        var clientService = new CommHTTPClientService(Configuration.getServerURI());

        var msgs = new ArrayList<Class<? extends MessageRequest>>();
        Configuration
                .getAllowedMessages()
                .forEach(i -> {
                    try {
                        var cls = Class.forName(MessageRequest.getClassNameFromActionType(i));
                        if (!MessageRequest.class.isAssignableFrom(cls))
                            throw new RuntimeException("Message is of invalid class");
                        msgs.add((Class<? extends MessageRequest>) cls);
                    } catch (ClassNotFoundException e) {
                        throw new RuntimeException("Not found message class");
                    }
                });

        var service = new MainService(
                consumerFactory, producerFactory,
                serverService, clientService,
                msgs
        );
        service.start();
    }
}
