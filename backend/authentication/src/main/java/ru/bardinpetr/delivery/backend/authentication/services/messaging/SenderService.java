package ru.bardinpetr.delivery.backend.authentication.services.messaging;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class SenderService {

    public void send(String target, String data) {
        log.warn("sending PIN code to user: {}", data);
        // here goes GMS/FMS/...
    }
}
