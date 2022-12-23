package ru.bardinpetr.delivery.common.communication;

import java.util.List;
import java.util.Map;

public class Configuration {

    private static final Map<String, String> environ = System.getenv();

    public static String getKafkaURI() {
        return environ.getOrDefault("KAFKA_BOOTSTRAP_SERVER", "localhost:9092");
    }

    public static String getServerURI() {
        return environ.getOrDefault("SERVER_URI", "http://server-com:9010");
    }

    public static List<String> getAllowedMessages() {
        return List.of(environ.getOrDefault("MESSAGES", "").split(","));
    }

    public static int getPort() {
        return Integer.parseInt(environ.getOrDefault("PORT", "9010"));
    }
}

