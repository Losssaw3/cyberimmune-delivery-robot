package ru.bardinpetr.delivery.libs.crypto;

import ru.bardinpetr.delivery.libs.crypto.errors.CryptoAlgoException;

import java.security.*;
import java.util.Base64;

import static ru.bardinpetr.delivery.libs.crypto.utils.RandomUtil.getRng;

public class SignatureCryptoService {

    public static final int KEY_SIZE = 2048;
    private static final String ALGO = "SHA512withRSA";
    private final PrivateKey privateKey;
    private final PublicKey publicKey;

    public SignatureCryptoService(PrivateKey key) {
        privateKey = key;
        publicKey = null;
    }

    public SignatureCryptoService(PublicKey key) {
        privateKey = null;
        publicKey = key;
    }

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

    public static byte[] decodeSign(String sign) {
        return Base64.getDecoder().decode(sign);
    }

    private Signature getSignatureAlgo() {
        try {
            return Signature.getInstance(ALGO);
        } catch (NoSuchAlgorithmException e) {
            throw new CryptoAlgoException();
        }
    }

    public String sign(String data) {
        if (privateKey == null)
            throw new RuntimeException("No key");

        var algo = getSignatureAlgo();
        byte[] sign;
        try {
            algo.initSign(privateKey);
            algo.update(data.getBytes());
            sign = algo.sign();
        } catch (SignatureException | InvalidKeyException e) {
            throw new RuntimeException(e);
        }
        return Base64.getEncoder().encodeToString(sign);
    }

    public boolean verify(String data, String signature) {
        return verify(data, decodeSign(signature));
    }

    public boolean verify(String data, byte[] signature) {
        if (publicKey == null)
            throw new RuntimeException("No key");

        var algo = getSignatureAlgo();
        try {
            algo.initVerify(publicKey);
            algo.update(data.getBytes());
            return algo.verify(signature);
        } catch (SignatureException | InvalidKeyException e) {
            throw new RuntimeException(e);
        }
    }
}
