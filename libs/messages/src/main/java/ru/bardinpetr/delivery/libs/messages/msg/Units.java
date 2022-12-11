package ru.bardinpetr.delivery.libs.messages.msg;

public enum Units {
    CCU("central"),
    FMS("fleet_manager"),
    AUTH("authentication"),
    COMM("robot_communication"),
    HMI("hmi"),
    LOCKER("locker"),
    LOC("location"),
    MOTION("motion"),
    SENSORS("sensors"),
    POS_ODOM("odometer_driver");

    private final String name;

    Units(String name) {
        this.name = name;
    }

    @Override
    public String toString() {
        return name;
    }
}
