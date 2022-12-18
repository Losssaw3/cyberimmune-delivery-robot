package ru.bardinpetr.delivery.robot.communication;

import java.util.Map;

public class Configuration {

    private static final Map<String, String> environ = System.getenv();

    public static String getKafkaURI() {
        return environ.getOrDefault("KAFKA_BOOTSTRAP_SERVER", "localhost:9092");
    }

    public static String getServerURI() {
        return environ.getOrDefault("SERVER_URI", "http://0.0.0.0:9990");
    }
}

