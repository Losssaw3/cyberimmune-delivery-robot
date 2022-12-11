package ru.bardinpetr.delivery.monitor.validator;

import ru.bardinpetr.delivery.libs.messages.msg.MessageRequest;
import ru.bardinpetr.delivery.monitor.validator.models.ActionRules;
import ru.bardinpetr.delivery.monitor.validator.models.AllowMode;
import ru.bardinpetr.delivery.monitor.validator.models.RequestActors;

import java.util.Map;

public class RuleValidator implements IValidator {
    private final Map<Class<? extends MessageRequest>, ActionRules> rules;
    private final AllowMode defaultMode;

    public RuleValidator(Map<Class<? extends MessageRequest>, ActionRules> rules) {
        this.rules = rules;
        this.defaultMode = AllowMode.ALLOW;
    }

    public RuleValidator(Map<Class<? extends MessageRequest>, ActionRules> rules,
                         AllowMode defaultMode) {
        this.defaultMode = defaultMode;
        this.rules = rules;
    }

    @Override
    public boolean verify(MessageRequest request) {
        var rule = rules.get(request.getClass());
        if (rule == null)
            return defaultMode == AllowMode.ALLOW;

        return rule.validate(new RequestActors(request.getSender(), request.getRecipient()));
    }
}
