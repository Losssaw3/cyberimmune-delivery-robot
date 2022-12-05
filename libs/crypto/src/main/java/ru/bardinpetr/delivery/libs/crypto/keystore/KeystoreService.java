package ru.bardinpetr.delivery.libs.crypto.keystore;

import ru.bardinpetr.delivery.libs.crypto.utils.RandomUtil;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableEntryException;
import java.security.cert.CertificateException;

public class KeystoreService {
    public static final String PIN_KEY_ALIAS = "pinSecret";
    public static final String SIGN_PUB_KEY_ALIAS = "taskSign";
    public static final String SIGN_PRIV_KEY_ALIAS = "taskSign";

    private final int counter = 0;

    private KeyStore createKeystore(char[] password) {
        KeyStore keyStore;
        try {
            keyStore = KeyStore.getInstance(KeyStore.getDefaultType());
            keyStore.load(null, password);
            return keyStore;
        } catch (KeyStoreException | IOException | NoSuchAlgorithmException | CertificateException e) {
            throw new RuntimeException(e);
        }
    }

    protected KeyStore loadKeystore(String path, char[] password) {
        try (var stream = new FileInputStream(path)) {
            var keyStore = KeyStore.getInstance("PKCS12");
            keyStore.load(stream, password);
            return keyStore;
        } catch (KeyStoreException | IOException | NoSuchAlgorithmException | CertificateException e) {
            throw new RuntimeException(e);
        }
    }

    private void saveKeystore(KeyStore store, String path, char[] password) {
        try (var stream = new FileOutputStream(path)) {
            store.store(stream, password);
        } catch (IOException | KeyStoreException | NoSuchAlgorithmException | CertificateException e) {
            throw new RuntimeException(e);
        }
    }

    protected String generateKeystoreWithEntry(String path, String type, KeyStore.Entry entry) {
        String passwordText = RandomUtil.genString(32);
        char[] password = passwordText.toCharArray();

        var keyStore = createKeystore(password);
        try {
            keyStore.setEntry(
                    type,
                    entry,
                    new KeyStore.PasswordProtection(password)
            );
        } catch (KeyStoreException e) {
            throw new RuntimeException(e);
        }

        saveKeystore(keyStore, path, password);
        return passwordText;
    }

    public KeyStore.Entry loadEntryFromKeystore(String path, String passwordStr, String type) {
        char[] password = passwordStr.toCharArray();

        KeyStore keyStore = loadKeystore(path, password);

        try {
            return keyStore.getEntry(
                    type,
                    new KeyStore.PasswordProtection(password)
            );
        } catch (KeyStoreException | NoSuchAlgorithmException | UnrecoverableEntryException e) {
            throw new RuntimeException(e);
        }
    }
}
