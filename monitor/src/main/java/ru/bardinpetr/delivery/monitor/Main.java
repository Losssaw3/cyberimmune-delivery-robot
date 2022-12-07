package ru.bardinpetr.delivery.monitor;

import ru.bardinpetr.delivery.libs.messages.fms.Action1Request;
import ru.bardinpetr.delivery.monitor.validator.IValidator;
import ru.bardinpetr.delivery.monitor.validator.models.ActionRulesBuilder;
import ru.bardinpetr.delivery.monitor.validator.models.AllowMode;
import ru.bardinpetr.delivery.monitor.validator.models.RuleValidatorBuilder;

public class Main {

    public static void main(String[] args) {
        var configs = Configuration.getKafkaGlobalParams();

        var validators = new IValidator[]{
                new RuleValidatorBuilder()
                        .addRule(
                                Action1Request.class,
                                new ActionRulesBuilder()
                                        .setDefaultMode(AllowMode.ALLOW)
//                                        .allow(new RequestActors("test", "smth"))
                                        .build()
                        )
                        .build()
        };

        var consumer = new MonitorService(configs, validators);
        consumer.start();
    }
}
