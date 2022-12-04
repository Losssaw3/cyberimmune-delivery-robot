package ru.bardinpetr.delivery.backend.authentication.services.crypto;

import ru.bardinpetr.delivery.backend.authentication.services.crypto.errors.CryptoAlgoException;
import ru.bardinpetr.delivery.backend.authentication.services.crypto.errors.CryptoException;

import javax.crypto.*;
import javax.crypto.spec.IvParameterSpec;
import java.nio.charset.StandardCharsets;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.Key;
import java.security.NoSuchAlgorithmException;

public class CryptoService {

    private static final String ALGO = "AES";

    private int counter = 0;


    private Cipher getCipher() {
        Cipher cipher;
        try {
            cipher = Cipher.getInstance(ALGO + "/CBC/PKCS5Padding");
        } catch (NoSuchAlgorithmException | NoSuchPaddingException e) {
            throw new CryptoAlgoException();
        }
        return cipher;
    }

    public String encrypt(Key key, String data) {
        var cipher = getCipher();

        byte[] dataEncoded = data.getBytes(StandardCharsets.UTF_8);

        ++counter;

        var nonce = KeyGenService.genNonce(MsgCoder.NONCE_LENGTH);
        var spec = new IvParameterSpec(nonce);

        byte[] enc;
        try {
            cipher.init(Cipher.ENCRYPT_MODE, key, spec);
            enc = cipher.doFinal(dataEncoded);
        } catch (IllegalBlockSizeException | BadPaddingException | InvalidKeyException |
                 InvalidAlgorithmParameterException e) {
            throw new CryptoException(e);
        }
        return MsgCoder.encode(enc, nonce, counter);
    }

    public final String decrypt(SecretKey key, String msg) {
        var cipher = getCipher();

        var msgDecoded = MsgCoder.decode(msg);

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
