package ru.bardinpetr.delivery.robot.central;

import java.util.Map;

public class Configuration {

    private static final Map<String, String> environ = System.getenv();

    public static String getKafkaURI() {
        return environ.getOrDefault("KAFKA_BOOTSTRAP_SERVER", "localhost:9092");
    }

    public static String getSignKeystorePath() {
        return environ.getOrDefault("SIGN_KS_PATH", "certs/client_sign_keystore.p12");
    }

    public static String getSignKeystorePass() {
        return environ.getOrDefault("SIGN_KS_PASS", "");
    }
}

