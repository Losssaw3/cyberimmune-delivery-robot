package ru.bardinpetr.delivery.common.monitor.validator.models;

import lombok.Data;

import java.util.Map;

@Data
public class ActionRules {

    private final AllowMode defaultMode;
    private final Map<RequestActors, AllowMode> actorsAllowance;

    public ActionRules(Map<RequestActors, AllowMode> actorsAllowance, AllowMode defaultMode) {
        this.defaultMode = defaultMode;
        this.actorsAllowance = actorsAllowance;
    }

    public boolean validate(RequestActors actors) {
        return actorsAllowance.getOrDefault(actors, defaultMode) == AllowMode.ALLOW;
    }
}
