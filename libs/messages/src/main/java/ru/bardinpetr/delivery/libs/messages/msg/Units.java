package ru.bardinpetr.delivery.libs.messages.msg;

public enum Units {
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

    Units(String name) {
        this.name = name;
    }

    @Override
    public String toString() {
        return name;
    }
}
