package ru.bardinpetr.delivery.backend.authentication;


import ru.bardinpetr.delivery.backend.authentication.services.PinGeneratorService;
import ru.bardinpetr.delivery.backend.authentication.services.messaging.SenderService;
import ru.bardinpetr.delivery.libs.crypto.AESCryptoService;
import ru.bardinpetr.delivery.libs.crypto.keystore.KeystoreServicePin;


public class Main {
    public static void main(String[] args) {

        var keystorePIN = new KeystoreServicePin();
        var secret = keystorePIN.getFromKeystore("secret_keystore.p12", "kiuw2ka7ahSeeTh2wieb6ohy1Xu3haj4");

        var ping = new PinGeneratorService();
        var crypt = new AESCryptoService(secret);
        var snd = new SenderService();

        var ms = new AuthenticationService(ping, crypt, snd);

        var enc = ms.createPin("asdf");
        System.out.println(crypt.decrypt(enc));
    }
}
