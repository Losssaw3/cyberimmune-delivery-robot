package ru.bardinpetr.delivery.backend.authentication;


import ru.bardinpetr.delivery.common.libs.crypto.keystore.KeystoreServicePin;
import ru.bardinpetr.delivery.common.libs.messages.kafka.CommonKafkaConfiguration;

import static ru.bardinpetr.delivery.backend.authentication.AuthenticationService.SERVICE_NAME;


public class Main {
    public static void main(String[] args) {
        var kafkaConfig = CommonKafkaConfiguration.getKafkaGlobalParams(
                Configuration.getKafkaURI(),
                SERVICE_NAME
        );

        var secret = new KeystoreServicePin()
                .getFromKeystore(Configuration.getKeyPath(), Configuration.getKeyPassword());


        var main = new AuthenticationService(kafkaConfig, secret);
        main.start();
    }
}
