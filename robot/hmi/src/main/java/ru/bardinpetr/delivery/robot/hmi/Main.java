package ru.bardinpetr.delivery.robot.hmi;

import ru.bardinpetr.delivery.robot.hmi.interactors.http.HTTPUserInteractor;

public class Main {
    public static void main(String[] args) {
        var server = new HTTPUserInteractor();
        var service = new MainService(server);
        service.start();
    }
}
