package ru.bardinpetr.delivery.backend.authentication.services.crypto.errors;

public class CryptoAlgoException extends RuntimeException {
    public CryptoAlgoException() {
        super("Crypto algorithm is not present. As it server-client system, we could not predict what could be used now");
    }
}
