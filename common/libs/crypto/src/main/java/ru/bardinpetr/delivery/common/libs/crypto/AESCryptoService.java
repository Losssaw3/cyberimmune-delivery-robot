package ru.bardinpetr.delivery.common.libs.crypto;

import ru.bardinpetr.delivery.common.libs.crypto.data.CryptMsgCoder;
import ru.bardinpetr.delivery.common.libs.crypto.errors.CryptoAlgoException;
import ru.bardinpetr.delivery.common.libs.crypto.errors.CryptoException;

import javax.crypto.*;
import javax.crypto.spec.IvParameterSpec;
import java.nio.charset.StandardCharsets;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

import static ru.bardinpetr.delivery.common.libs.crypto.utils.RandomUtil.genNonce;
import static ru.bardinpetr.delivery.common.libs.crypto.utils.RandomUtil.getRng;

public class AESCryptoService {
    public static final int KEY_SIZE = 256;
    private final SecretKey key;
    private int counter = 0;

    public AESCryptoService(SecretKey key) {
        this.key = key;
    }

    public static SecretKey generate() {
        try {
            var gen = KeyGenerator.getInstance("AES");
            gen.init(KEY_SIZE, getRng());
            return gen.generateKey();
        } catch (NoSuchAlgorithmException e) {
            throw new CryptoAlgoException();
        }
    }

    private Cipher getCipher() {
        Cipher cipher;
        try {
            cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
        } catch (NoSuchAlgorithmException | NoSuchPaddingException e) {
            throw new CryptoAlgoException();
        }
        return cipher;
    }

    public String encrypt(String data) {
        var cipher = getCipher();

        byte[] dataEncoded = data.getBytes(StandardCharsets.UTF_8);

        ++counter;

        var nonce = genNonce(CryptMsgCoder.NONCE_LENGTH);
        var spec = new IvParameterSpec(nonce);

        byte[] enc;
        try {
            cipher.init(Cipher.ENCRYPT_MODE, key, spec);
            enc = cipher.doFinal(dataEncoded);
        } catch (IllegalBlockSizeException | BadPaddingException | InvalidKeyException |
                 InvalidAlgorithmParameterException e) {
            throw new CryptoException(e);
        }
        return CryptMsgCoder.encode(enc, nonce, counter);
    }

    public final String decrypt(String msg) {
        var cipher = getCipher();

        var msgDecoded = CryptMsgCoder.decode(msg);

        var spec = new IvParameterSpec(msgDecoded.nonce());

        byte[] plain;
        try {
            cipher.init(Cipher.DECRYPT_MODE, key, spec);
            plain = cipher.doFinal(msgDecoded.data());
        } catch (IllegalBlockSizeException | BadPaddingException | InvalidKeyException |
                 InvalidAlgorithmParameterException e) {
            throw new CryptoException(e);
        }

        return new String(plain);
    }

}
