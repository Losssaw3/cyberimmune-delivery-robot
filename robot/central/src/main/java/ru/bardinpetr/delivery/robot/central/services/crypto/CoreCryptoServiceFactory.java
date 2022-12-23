package ru.bardinpetr.delivery.robot.central.services.crypto;

import ru.bardinpetr.delivery.libs.crypto.SignatureCryptoService;
import ru.bardinpetr.delivery.libs.crypto.keystore.KeystoreServiceSign;

public class CoreCryptoServiceFactory {
    public static CoreCryptoService getService(String signKeystorePath, String signKeystorePass) {
        var ksSign = new KeystoreServiceSign();

        return new CoreCryptoService(
                new SignatureCryptoService(ksSign.getPublicFromKeystore(signKeystorePath, signKeystorePass))
        );
    }
}
