package ru.bardinpetr.delivery.common.libs.messages.msg;

public enum Unit {
    CCU("central"),
    FMS("fms"),
    AUTH("auth"),
    COMM("comms"),
    HMI("hmi"),
    LOCKER("locker"),
    LOC("location"),
    MOTION("motion"),
    SENSORS("sensors"),
    POS_ODOM("odom");

    private final String name;

    Unit(String name) {
        this.name = name;
    }

    @Override
    public String toString() {
        return name;
    }
}
