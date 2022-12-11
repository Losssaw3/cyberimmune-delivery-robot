package ru.bardinpetr.delivery.robot.location;

import ru.bardinpetr.delivery.libs.messages.models.Units;

import java.util.List;
import java.util.Map;

public class Configuration {

    private static final Map<String, String> environ = System.getenv();

    public static String getKafkaURI() {
        return environ.getOrDefault("KAFKA_BOOTSTRAP_SERVER", "localhost:9092");
    }

    private static double loadDouble(String name, double defaultValue) {
        return Double.parseDouble(environ.getOrDefault(name, String.valueOf(defaultValue)));
    }

    private static int loadInt(String name, int defaultValue) {
        return Integer.parseInt(environ.getOrDefault(name, String.valueOf(defaultValue)));
    }

    public static double getSpeedTrigger() {
        return loadDouble("SPEED_TAMPERED_TRIGGER", 5);
    }

    public static double getDistanceTrigger() {
        return loadDouble("DISTANCE_TAMPERED_TRIGGER", 50);
    }

    public static int getUpdateInterval() {
        return loadInt("UPDATE_INTERVAL_SEC", 10);
    }

    public static List<String> getServices() {
        var input = environ.getOrDefault("SERVICES", Units.POS_ODOM.toString());
        return List.of(input.split(","));
    }
}

