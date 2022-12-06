package ru.bardinpetr.delivery.monitor.validator.models;


import lombok.Data;

@Data
public class RequestActors {
    private String from;
    private String to;

    private boolean isBidirectional = false;

    public RequestActors(String from, String to) {
        this.from = from;
        this.to = to;
    }

    public RequestActors(String from, String to, boolean isBidirectional) {
        this.from = from;
        this.to = to;
        this.isBidirectional = isBidirectional;
    }
}
