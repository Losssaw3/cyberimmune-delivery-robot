package ru.bardinpetr.delivery.backend.authentication;


import ru.bardinpetr.delivery.libs.messages.kafka.CommonKafkaConfiguration;

import static ru.bardinpetr.delivery.backend.authentication.AuthenticationService.SERVICE_NAME;


public class Main {
    public static void main(String[] args) {
        var kafkaConfig = CommonKafkaConfiguration.getKafkaGlobalParams(
                Configuration.getKafkaURI(),
                SERVICE_NAME
        );

        var main = new AuthenticationService(kafkaConfig);
        main.start();
    }
}
