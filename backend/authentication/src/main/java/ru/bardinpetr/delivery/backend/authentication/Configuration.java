package ru.bardinpetr.delivery.backend.authentication;

import java.util.Map;

public class Configuration {

    private static final Map<String, String> environ = System.getenv();

    public static String getKafkaURI() {
        return environ.getOrDefault("KAFKA_BOOTSTRAP_SERVER", "localhost:9092");
    }

    public static String getKeyPath() {
        return environ.getOrDefault("KEYSTORE_PATH", "secret_keystore.p12");
    }

    public static String getKeyPassword() {
        return environ.get("KEYSTORE_PASSWORD");
    }
}
