package ru.bardinpetr.delivery.backend.authentication;

import ru.bardinpetr.delivery.backend.authentication.services.PinGeneratorService;
import ru.bardinpetr.delivery.backend.authentication.services.messaging.SenderService;
import ru.bardinpetr.delivery.libs.crypto.CryptoService;

import javax.crypto.SecretKey;

public class AuthenticationService {
    private final CryptoService cryptoService;
    private final PinGeneratorService pinService;
    private final SenderService senderService;
    private final SecretKey key;


    public AuthenticationService(SecretKey key,
                                 PinGeneratorService pinService,
                                 CryptoService cryptoService,
                                 SenderService senderService) {
        this.senderService = senderService;
        this.cryptoService = cryptoService;
        this.pinService = pinService;
        this.key = key;
    }

    public final String createPin(String mobileDestination) {
        var pin = pinService.createPin();
        var enc = cryptoService.encrypt(key, pin);
        this.senderService.send(mobileDestination, pin);
        return enc;
    }
}
