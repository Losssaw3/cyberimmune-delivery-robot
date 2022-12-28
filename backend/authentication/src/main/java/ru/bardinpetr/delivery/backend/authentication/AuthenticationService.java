package ru.bardinpetr.delivery.backend.authentication;

import lombok.extern.slf4j.Slf4j;
import ru.bardinpetr.delivery.backend.authentication.services.PinGeneratorService;
import ru.bardinpetr.delivery.backend.authentication.services.messaging.SenderService;
import ru.bardinpetr.delivery.common.libs.crypto.AESCryptoService;
import ru.bardinpetr.delivery.common.libs.crypto.PINService;
import ru.bardinpetr.delivery.common.libs.messages.kafka.consumers.MonitoredKafkaConsumerFactory;
import ru.bardinpetr.delivery.common.libs.messages.kafka.consumers.MonitoredKafkaConsumerService;
import ru.bardinpetr.delivery.common.libs.messages.kafka.consumers.MonitoredKafkaConsumerServiceBuilder;
import ru.bardinpetr.delivery.common.libs.messages.kafka.producers.MonitoredKafkaProducerFactory;
import ru.bardinpetr.delivery.common.libs.messages.kafka.producers.MonitoredKafkaProducerService;
import ru.bardinpetr.delivery.common.libs.messages.msg.Unit;
import ru.bardinpetr.delivery.common.libs.messages.msg.authentication.CreatePINRequest;
import ru.bardinpetr.delivery.common.libs.messages.msg.authentication.CreatePINResponse;
import ru.bardinpetr.delivery.common.libs.messages.msg.authentication.PINTestRequest;

import javax.crypto.SecretKey;
import java.util.Map;

@Slf4j
public class AuthenticationService {
    public static final String SERVICE_NAME = Unit.AUTH.toString();

    private final PinGeneratorService pinService = new PinGeneratorService();
    private final SenderService senderService = new SenderService();
    private final MonitoredKafkaConsumerService consumerService;
    private final MonitoredKafkaProducerService producerService;
    private final AESCryptoService cryptoService;

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
        log.info("New pin creation request for {}", req.getUserIdentifier());
        var result = createPin(req.getUserIdentifier());
        producerService.sendReply(
                req,
                new CreatePINResponse(result)
        );
    }

    private String createPin(String mobileDestination) {
        var pin = pinService.createPin();
        this.senderService.send(mobileDestination, pin);

        this.producerService.sendMessage(
                Unit.AUTH,
                new PINTestRequest(pin)
        ); // This is only for complete-system unit testing.

        return cryptoService.encrypt(PINService.hashPin(pin));
    }

    public void start() {
        consumerService.start();
    }
}
