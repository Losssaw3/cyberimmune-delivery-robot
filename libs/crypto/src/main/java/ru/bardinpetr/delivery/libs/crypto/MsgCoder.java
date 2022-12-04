package ru.bardinpetr.delivery.libs.crypto;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.Base64;

public class MsgCoder {
    public static final int NONCE_LENGTH = 16;
    private static final int COUNT_LENGTH = 4;

    public static String encode(byte[] data, byte[] nonce, int count) {
        if (nonce.length != NONCE_LENGTH)
            throw new RuntimeException("invalid nonce");

        var countAsBytes = ByteBuffer.allocate(COUNT_LENGTH).putInt(count).array();
        var buffer = new ByteArrayOutputStream();
        try {
            buffer.write(nonce);
            buffer.write(countAsBytes);
            buffer.write(data);
        } catch (IOException ignored) {
        }

        byte[] arr = buffer.toByteArray();
        return Base64.getEncoder().encodeToString(arr);
    }

    public static CryptMsg decode(String msg) {
        byte[] decoded = Base64.getDecoder().decode(msg);
        int dataLen = decoded.length - COUNT_LENGTH - NONCE_LENGTH;
        if (dataLen <= 0) throw new RuntimeException("not enough bytes");

        byte[] ciphertext = new byte[dataLen];
        byte[] nonce = new byte[NONCE_LENGTH];
        byte[] counter = new byte[COUNT_LENGTH];

        System.arraycopy(decoded, 0, nonce, 0, NONCE_LENGTH);
        System.arraycopy(decoded, NONCE_LENGTH, counter, 0, COUNT_LENGTH);
        System.arraycopy(decoded, NONCE_LENGTH + COUNT_LENGTH, ciphertext, 0, dataLen);

        int counterInt = ByteBuffer.wrap(counter).getInt();

        return new CryptMsg(ciphertext, nonce, counterInt);
    }

    public record CryptMsg(byte[] data, byte[] nonce, int count) {
    }
}
