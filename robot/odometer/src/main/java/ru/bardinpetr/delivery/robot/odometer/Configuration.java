package ru.bardinpetr.delivery.robot.odometer;

import ru.bardinpetr.delivery.common.libs.messages.msg.Unit;

import java.util.Map;

public class Configuration {

    private static final Map<String, String> environ = System.getenv();

    public static String getKafkaURI() {
        return environ.getOrDefault("KAFKA_BOOTSTRAP_SERVER", "localhost:9092");
    }

    public static String getName() {
        return environ.getOrDefault("SERVICE_NAME", Unit.POS_ODOM.toString());
    }

}

