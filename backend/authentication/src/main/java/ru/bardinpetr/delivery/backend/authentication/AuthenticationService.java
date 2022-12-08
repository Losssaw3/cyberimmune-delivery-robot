package ru.bardinpetr.delivery.backend.authentication;

import ru.bardinpetr.delivery.backend.authentication.services.PinGeneratorService;
import ru.bardinpetr.delivery.backend.authentication.services.messaging.SenderService;
import ru.bardinpetr.delivery.libs.crypto.AESCryptoService;
import ru.bardinpetr.delivery.libs.messages.kafka.consumers.MonitoredKafkaConsumerFactory;
import ru.bardinpetr.delivery.libs.messages.kafka.consumers.MonitoredKafkaConsumerService;
import ru.bardinpetr.delivery.libs.messages.kafka.consumers.MonitoredKafkaConsumerServiceBuilder;
import ru.bardinpetr.delivery.libs.messages.kafka.producers.MonitoredKafkaProducerFactory;
import ru.bardinpetr.delivery.libs.messages.kafka.producers.MonitoredKafkaProducerService;
import ru.bardinpetr.delivery.libs.messages.models.authentication.CreatePINRequest;
import ru.bardinpetr.delivery.libs.messages.models.authentication.CreatePINResponse;

import javax.crypto.SecretKey;
import java.util.Map;

public class AuthenticationService {
    public static final String SERVICE_NAME = "authentication";


    private final PinGeneratorService pinService = new PinGeneratorService();
    private final SenderService senderService = new SenderService();
    private final AESCryptoService cryptoService;

    private final MonitoredKafkaConsumerService consumerService;
    private final MonitoredKafkaProducerService producerService;

    public AuthenticationService(Map<String, Object> kafkaConfig, SecretKey secretKey) {
        cryptoService = new AESCryptoService(secretKey);

        consumerService = new MonitoredKafkaConsumerServiceBuilder(SERVICE_NAME)
                .setConsumerFactory(new MonitoredKafkaConsumerFactory(kafkaConfig))
                .subscribe(CreatePINRequest.class, this::onRequest)
                .build();

        producerService = new MonitoredKafkaProducerService(
                SERVICE_NAME,
                new MonitoredKafkaProducerFactory(kafkaConfig)
        );
    }

    private void onRequest(CreatePINRequest req) {
        var result = createPin(req.getUserIdentifier());
        producerService.sendMessage(
                req.getSender(),
                new CreatePINResponse(result)
        );
    }

    private String createPin(String mobileDestination) {
        var pin = pinService.createPin();
        var enc = cryptoService.encrypt(pin);
        this.senderService.send(mobileDestination, pin);
        return enc;
    }

    public void start() {
        consumerService.start();
    }
}
