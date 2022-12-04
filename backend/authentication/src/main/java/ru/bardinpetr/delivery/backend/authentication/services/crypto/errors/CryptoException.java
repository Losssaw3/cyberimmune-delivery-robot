package ru.bardinpetr.delivery.backend.authentication.services.crypto.errors;

public class CryptoException extends RuntimeException {
    public CryptoException(Throwable throwable) {
        super(throwable);
    }
}
