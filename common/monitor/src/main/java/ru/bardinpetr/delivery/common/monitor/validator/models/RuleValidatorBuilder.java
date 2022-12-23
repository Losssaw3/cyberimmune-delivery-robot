package ru.bardinpetr.delivery.common.monitor.validator.models;

import ru.bardinpetr.delivery.common.libs.messages.msg.MessageRequest;
import ru.bardinpetr.delivery.common.monitor.validator.RuleValidator;

import java.util.HashMap;
import java.util.Map;

public class RuleValidatorBuilder {
    private final Map<Class<? extends MessageRequest>, ActionRules> rules = new HashMap<>();
    private AllowMode defaultMode = AllowMode.ALLOW;

    public RuleValidatorBuilder addRule(Class<? extends MessageRequest> msg, ActionRules rules) {
        this.rules.put(msg, rules);
        return this;
    }

    public RuleValidatorBuilder setDefaultMode(AllowMode defaultMode) {
        this.defaultMode = defaultMode;
        return this;
    }

    public RuleValidator build() {
        return new RuleValidator(rules, defaultMode);
    }
}