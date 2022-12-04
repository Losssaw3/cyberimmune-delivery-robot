package ru.bardinpetr.delivery.backend.authentication;


import ru.bardinpetr.delivery.backend.authentication.services.PinGeneratorService;
import ru.bardinpetr.delivery.backend.authentication.services.messaging.SenderService;
import ru.bardinpetr.delivery.libs.crypto.CryptoService;
import ru.bardinpetr.delivery.libs.crypto.KeyGenService;

import java.util.Arrays;

public class Main {
    public static void main(String[] args) {
        if (args.length > 0) {
            System.out.println(Arrays.toString(args));
            new KeyGenService().generateKeyToFile(args[0], args[1]);
        }

        var kg = new KeyGenService();
        var key = kg.loadKeyFromFile("ks0", "ks0p");

        var ping = new PinGeneratorService();
        var crypt = new CryptoService();
        var snd = new SenderService();

        var ms = new AuthenticationService(key, ping, crypt, snd);

        var enc = ms.createPin("asdf");
        System.out.println(enc);
        System.out.println(crypt.decrypt(key, enc));
    }
}
