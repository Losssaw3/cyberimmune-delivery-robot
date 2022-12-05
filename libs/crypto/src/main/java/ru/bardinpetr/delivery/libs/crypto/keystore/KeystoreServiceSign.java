package ru.bardinpetr.delivery.libs.crypto.keystore;

import java.security.*;

public class KeystoreServiceSign extends KeystoreService {

    public PublicKey getPublicFromKeystore(String path, String pass) {
        var charPass = pass.toCharArray();
        var ks = loadKeystore(path, charPass);
        try {
            return ks.getCertificate(SIGN_PUB_KEY_ALIAS).getPublicKey();
        } catch (KeyStoreException e) {
            throw new RuntimeException(e);
        }
    }

    public PrivateKey getPrivateFromKeystore(String path, String pass) {
        var charPass = pass.toCharArray();
        var ks = loadKeystore(path, charPass);
        try {
            return (PrivateKey) ks.getKey(SIGN_PRIV_KEY_ALIAS, charPass);
        } catch (KeyStoreException | NoSuchAlgorithmException | UnrecoverableKeyException e) {
            throw new RuntimeException(e);
        }
    }
}
