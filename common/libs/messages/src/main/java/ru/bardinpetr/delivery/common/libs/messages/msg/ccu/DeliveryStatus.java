package ru.bardinpetr.delivery.common.libs.messages.msg.ccu;

public enum DeliveryStatus {
    IDLE, STARTED, RUNNING, ENDED_OK, ARRIVED_TO_CUSTOMER, RETURNING, ENDED_ERR, LOCKER_OPENED, HUMAN_DETECTED, PIN_INVALID
}
