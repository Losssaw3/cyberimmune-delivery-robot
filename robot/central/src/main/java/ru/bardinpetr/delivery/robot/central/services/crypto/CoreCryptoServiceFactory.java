package ru.bardinpetr.delivery.robot.central.services.crypto;

import ru.bardinpetr.delivery.common.libs.crypto.AESCryptoService;
import ru.bardinpetr.delivery.common.libs.crypto.SignatureCryptoService;
import ru.bardinpetr.delivery.common.libs.crypto.keystore.KeystoreServicePin;
import ru.bardinpetr.delivery.common.libs.crypto.keystore.KeystoreServiceSign;

public class CoreCryptoServiceFactory {
    public static CoreCryptoService getService(String aesKeystorePath, String aesKeystorePass,
                                               String signKeystorePath, String signKeystorePass) {
        var ksSign = new KeystoreServiceSign();
        var ksPin = new KeystoreServicePin();

        return new CoreCryptoService(
                new SignatureCryptoService(ksSign.getPublicFromKeystore(signKeystorePath, signKeystorePass)),
                new AESCryptoService(ksPin.getFromKeystore(aesKeystorePath, aesKeystorePass))
        );
    }
}
