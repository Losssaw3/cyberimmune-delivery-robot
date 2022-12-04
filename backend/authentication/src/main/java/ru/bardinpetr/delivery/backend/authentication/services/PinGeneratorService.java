package ru.bardinpetr.delivery.backend.authentication.services;

import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;

public class PinGeneratorService {
    private SecureRandom secureRandom;

    public PinGeneratorService() {
        try {
            secureRandom = SecureRandom.getInstance("NativePRNG");
        } catch (NoSuchAlgorithmException e) {
            secureRandom = new SecureRandom();
        }
    }

    public String createPin(int digits) {
        int min = (int) Math.pow(10, digits - 1);
        int max = (int) (Math.pow(10, digits) - 1);
        return String.valueOf(secureRandom.nextInt(max - min) + min);
    }

    public String createPin() {
        return createPin(6);
    }
}
