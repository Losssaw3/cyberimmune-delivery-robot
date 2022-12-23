package ru.bardinpetr.delivery.backend.fms.robots;

import lombok.Data;
import lombok.NonNull;

@Data
public class Robot {
    @NonNull
    private String url;
    private boolean isIDLE = true;
}
