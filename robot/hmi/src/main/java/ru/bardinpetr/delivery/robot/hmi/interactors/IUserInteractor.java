package ru.bardinpetr.delivery.robot.hmi.interactors;

public interface IUserInteractor {
    void start();

    void registerPINHandler(PinHandler handler);
}
