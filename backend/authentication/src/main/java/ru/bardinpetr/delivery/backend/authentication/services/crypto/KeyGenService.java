package ru.bardinpetr.delivery.backend.authentication.services.crypto;

import ru.bardinpetr.delivery.backend.authentication.services.crypto.errors.CryptoAlgoException;

import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.security.*;
import java.security.cert.CertificateException;

public class KeyGenService {
    public static final String KS_PIN_KEY_ENTRY = "pin";

    public static final int KEY_SIZE = 256;
    private final int counter = 0;


    public static SecureRandom getRng() {
        SecureRandom random;
        try {
            random = SecureRandom.getInstanceStrong();
        } catch (NoSuchAlgorithmException e) {
            random = new SecureRandom();
        }
        return random;
    }

    public static byte[] genNonce(int size) {
        byte[] nonce = new byte[size];
        getRng().nextBytes(nonce);
        return nonce;
    }

    public SecretKey generate() {
        return generate("AES");
    }

    public SecretKey generate(String algo) {
        try {
            var gen = KeyGenerator.getInstance(algo);
            gen.init(KEY_SIZE, getRng());
            return gen.generateKey();
        } catch (NoSuchAlgorithmException e) {
            throw new CryptoAlgoException();
        }
    }

    public void generateKeyToFile(String path, String passwordStr) {
        char[] password = passwordStr.toCharArray();

        KeyStore keyStore;
        try {
            keyStore = KeyStore.getInstance(KeyStore.getDefaultType());
            keyStore.load(null, password);
        } catch (KeyStoreException | IOException | NoSuchAlgorithmException | CertificateException e) {
            throw new RuntimeException(e);
        }

        var key = generate();
        var entry = new KeyStore.SecretKeyEntry(key);
        try {
            keyStore.setEntry(
                    KS_PIN_KEY_ENTRY,
                    entry,
                    new KeyStore.PasswordProtection(password)
            );
        } catch (KeyStoreException e) {
            throw new RuntimeException(e);
        }

        try (var stream = new FileOutputStream(path)) {
            keyStore.store(stream, password);
        } catch (IOException | KeyStoreException | NoSuchAlgorithmException | CertificateException e) {
            throw new RuntimeException(e);
        }
    }

    public SecretKey loadKeyFromFile(String path, String passwordStr) {
        char[] password = passwordStr.toCharArray();

        KeyStore keyStore;
        try (var stream = new FileInputStream(path)) {
            keyStore = KeyStore.getInstance(KeyStore.getDefaultType());
            keyStore.load(stream, password);
        } catch (KeyStoreException | IOException | NoSuchAlgorithmException | CertificateException e) {
            throw new RuntimeException(e);
        }

        KeyStore.SecretKeyEntry entry;
        try {
            entry = (KeyStore.SecretKeyEntry) keyStore.getEntry(
                    KS_PIN_KEY_ENTRY,
                    new KeyStore.PasswordProtection(password)
            );
        } catch (KeyStoreException | NoSuchAlgorithmException | UnrecoverableEntryException e) {
            throw new RuntimeException(e);
        }

        return entry.getSecretKey();
    }
}
