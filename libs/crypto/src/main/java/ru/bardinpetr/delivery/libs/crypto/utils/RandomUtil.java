package ru.bardinpetr.delivery.libs.crypto.utils;

import java.nio.charset.StandardCharsets;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;

public class RandomUtil {
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

    public static byte[] genBytesInRange(int cnt, int low, int high) {
        var data = new byte[cnt];
        var rng = getRng();
        for (int i = 0; i < cnt; i++) data[i] = (byte) (rng.nextInt(high - low + 1) + low);
        return data;
    }

    public static String genString(int len) {
        return new String(genBytesInRange(len, 97, 122), StandardCharsets.UTF_8);
    }
}
