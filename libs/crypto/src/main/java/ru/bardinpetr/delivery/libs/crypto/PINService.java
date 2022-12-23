package ru.bardinpetr.delivery.libs.crypto;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;

public class PINService {
    public static final String ALGO = "SHA-512";

    public static String hashPin(String pin) {
        MessageDigest digest = null;
        try {
            digest = MessageDigest.getInstance(ALGO);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
        var hash = digest.digest(pin.getBytes(StandardCharsets.UTF_8));
        return Base64.getEncoder().encodeToString(hash);
    }
}
