package ru.bardinpetr.delivery.monitor.validator.models;

import java.util.HashMap;
import java.util.Map;

public class ActionRulesBuilder {

    private final Map<RequestActors, AllowMode> actorsAllowance = new HashMap<>();
    private AllowMode defaultMode = AllowMode.ALLOW;

    public ActionRulesBuilder setDefaultMode(AllowMode mode) {
        this.defaultMode = mode;
        return this;
    }

    public ActionRulesBuilder allow(RequestActors actors) {
        actorsAllowance.put(actors, AllowMode.ALLOW);
        return this;
    }

    public ActionRulesBuilder deny(RequestActors actors) {
        actorsAllowance.put(actors, AllowMode.DENY);
        return this;
    }

    public ActionRules build() {
        return new ActionRules(actorsAllowance, defaultMode);
    }
}