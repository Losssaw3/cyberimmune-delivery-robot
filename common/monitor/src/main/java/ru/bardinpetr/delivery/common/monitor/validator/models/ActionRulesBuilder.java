package ru.bardinpetr.delivery.common.monitor.validator.models;

import ru.bardinpetr.delivery.common.libs.messages.msg.Unit;

import java.util.HashMap;
import java.util.Map;

public class ActionRulesBuilder {

    private final Map<RequestActors, AllowMode> actorsAllowance = new HashMap<>();
    private AllowMode defaultMode = AllowMode.DENY;

    public ActionRulesBuilder setDefaultMode(AllowMode mode) {
        this.defaultMode = mode;
        return this;
    }

    public ActionRulesBuilder allow(RequestActors actors) {
        actorsAllowance.put(actors, AllowMode.ALLOW);
        return this;
    }

    public ActionRulesBuilder allow(Unit from, Unit to) {
        return allow(RequestActors.of(from.toString(), to.toString()));
    }

    public ActionRulesBuilder deny(RequestActors actors) {
        actorsAllowance.put(actors, AllowMode.DENY);
        return this;
    }

    public ActionRulesBuilder deny(Unit from, Unit to) {
        return deny(RequestActors.of(from.toString(), to.toString()));
    }

    public ActionRules build() {
        return new ActionRules(actorsAllowance, defaultMode);
    }
}