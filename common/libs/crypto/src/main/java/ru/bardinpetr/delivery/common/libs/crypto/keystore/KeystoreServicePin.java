package ru.bardinpetr.delivery.common.libs.crypto.keystore;

import javax.crypto.SecretKey;
import java.security.KeyStore;

public class KeystoreServicePin extends KeystoreService {

    /**
     * Generates keystore for single secret key record
     *
     * @param path file path for storing keystore
     * @return generated keystore password
     */
    public String generateKeystore(String path, SecretKey key) {
        return generateKeystoreWithEntry(path,
                KeystoreServicePin.PIN_KEY_ALIAS,
                new KeyStore.SecretKeyEntry(key));
    }

    public SecretKey getFromKeystore(String path, String pass) {
        var entry = loadEntryFromKeystore(path, pass, KeystoreServicePin.PIN_KEY_ALIAS);
        return ((KeyStore.SecretKeyEntry) entry).getSecretKey();
    }
}
