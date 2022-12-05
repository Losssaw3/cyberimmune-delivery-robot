package ru.bardinpetr.delivery.libs.crypto;

import ru.bardinpetr.delivery.libs.crypto.errors.CryptoAlgoException;

import java.security.*;

import static ru.bardinpetr.delivery.libs.crypto.utils.RandomUtil.getRng;

public class SignatureCryptoService {

    public static final int KEY_SIZE = 2048;
    private static final String ALGO = "SHA512withRSA";

    public static KeyPair generate() {
        KeyPairGenerator generator = null;
        try {
            generator = KeyPairGenerator.getInstance("RSA");
        } catch (NoSuchAlgorithmException e) {
            throw new CryptoAlgoException();
        }
        generator.initialize(KEY_SIZE, getRng());
        return generator.generateKeyPair();
    }

    private Signature getSignatureAlgo() {
        try {
            return Signature.getInstance(ALGO);
        } catch (NoSuchAlgorithmException e) {
            throw new CryptoAlgoException();
        }
    }

    public byte[] sign(PrivateKey key, String data) {
        var algo = getSignatureAlgo();
        try {
            algo.initSign(key);
            algo.update(data.getBytes());
            return algo.sign();
        } catch (SignatureException | InvalidKeyException e) {
            throw new RuntimeException(e);
        }
    }

    public boolean verify(PublicKey key, String data, byte[] signature) {
        var algo = getSignatureAlgo();
        try {
            algo.initVerify(key);
            algo.update(data.getBytes());
            return algo.verify(signature);
        } catch (SignatureException | InvalidKeyException e) {
            throw new RuntimeException(e);
        }
    }
}
