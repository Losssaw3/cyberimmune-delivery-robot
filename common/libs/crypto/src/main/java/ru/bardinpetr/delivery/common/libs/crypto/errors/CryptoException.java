package ru.bardinpetr.delivery.common.libs.crypto.errors;

public class CryptoException extends RuntimeException {
    public CryptoException(Throwable throwable) {
        super(throwable);
    }
}
