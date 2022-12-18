package ru.bardinpetr.delivery.robot.communication;

import ru.bardinpetr.delivery.libs.messages.kafka.CommonKafkaConfiguration;
import ru.bardinpetr.delivery.libs.messages.kafka.consumers.MonitoredKafkaConsumerFactory;
import ru.bardinpetr.delivery.libs.messages.kafka.producers.MonitoredKafkaProducerFactory;
import ru.bardinpetr.delivery.libs.messages.msg.MessageRequest;
import ru.bardinpetr.delivery.robot.communication.client.CommHTTPClientService;
import ru.bardinpetr.delivery.robot.communication.server.CommHTTPServerService;

import java.util.ArrayList;

import static ru.bardinpetr.delivery.robot.communication.MainService.SERVICE_NAME;

public class Main {

    @SuppressWarnings("unchecked")
    public static void main(String[] args) {
        var kafkaConfig = CommonKafkaConfiguration.getKafkaGlobalParams(
                Configuration.getKafkaURI(),
                SERVICE_NAME
        );
        var producerFactory = new MonitoredKafkaProducerFactory(kafkaConfig);
        var consumerFactory = new MonitoredKafkaConsumerFactory(kafkaConfig);

        var serverService = new CommHTTPServerService(9990);
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
