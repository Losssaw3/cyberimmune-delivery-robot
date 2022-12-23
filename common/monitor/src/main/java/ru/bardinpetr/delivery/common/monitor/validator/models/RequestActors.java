package ru.bardinpetr.delivery.common.monitor.validator.models;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

@Data
@RequiredArgsConstructor(staticName = "of")
@AllArgsConstructor
public class RequestActors {
    @NonNull
    private String from;
    @NonNull
    private String to;

    private boolean isBidirectional = false;
}
