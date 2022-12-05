package ru.bardinpetr.delivery.backend.authentication.services;

import java.security.SecureRandom;

import static ru.bardinpetr.delivery.libs.crypto.utils.RandomUtil.getRng;

public class PinGeneratorService {
    private final SecureRandom secureRandom = getRng();

    public String createPin(int digits) {
        int min = (int) Math.pow(10, digits - 1);
        int max = (int) (Math.pow(10, digits) - 1);
        return String.valueOf(secureRandom.nextInt(max - min) + min);
    }

    public String createPin() {
        return createPin(6);
    }
}
