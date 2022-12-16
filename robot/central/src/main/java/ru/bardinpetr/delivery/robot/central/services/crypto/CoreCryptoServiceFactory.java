package ru.bardinpetr.delivery.robot.central.services.crypto;

import ru.bardinpetr.delivery.libs.crypto.AESCryptoService;
import ru.bardinpetr.delivery.libs.crypto.SignatureCryptoService;
import ru.bardinpetr.delivery.libs.crypto.keystore.KeystoreServicePin;
import ru.bardinpetr.delivery.libs.crypto.keystore.KeystoreServiceSign;

public class CoreCryptoServiceFactory {
    public static CoreCryptoService getService(String aesKeystorePath, String aesKeystorePass,
                                               String signKeystorePath, String signKeystorePass) {
        var ksPin = new KeystoreServicePin();
        var ksSign = new KeystoreServiceSign();

        return new CoreCryptoService(
                new SignatureCryptoService(ksSign.getPublicFromKeystore(signKeystorePath, signKeystorePass)),
                new AESCryptoService(ksPin.getFromKeystore(aesKeystorePath, aesKeystorePass))
        );
    }
}
