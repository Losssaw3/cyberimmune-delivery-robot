package ru.bardinpetr.delivery.backend.authentication;

import ru.bardinpetr.delivery.backend.authentication.services.PinGeneratorService;
import ru.bardinpetr.delivery.backend.authentication.services.messaging.SenderService;
import ru.bardinpetr.delivery.libs.crypto.AESCryptoService;

public class AuthenticationService {
    private final AESCryptoService cryptoService;
    private final PinGeneratorService pinService;
    private final SenderService senderService;


    public AuthenticationService(PinGeneratorService pinService,
                                 AESCryptoService cryptoService,
                                 SenderService senderService) {
        this.senderService = senderService;
        this.cryptoService = cryptoService;
        this.pinService = pinService;
    }

    public final String createPin(String mobileDestination) {
        var pin = pinService.createPin();
        var enc = cryptoService.encrypt(pin);
        this.senderService.send(mobileDestination, pin);
        return enc;
    }
}
