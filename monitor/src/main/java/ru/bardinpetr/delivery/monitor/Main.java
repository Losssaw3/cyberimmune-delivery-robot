package ru.bardinpetr.delivery.monitor;

import org.apache.kafka.clients.consumer.ConsumerConfig;
import ru.bardinpetr.delivery.libs.messages.fms.Action1Request;
import ru.bardinpetr.delivery.monitor.validator.IValidator;
import ru.bardinpetr.delivery.monitor.validator.models.ActionRulesBuilder;
import ru.bardinpetr.delivery.monitor.validator.models.AllowMode;
import ru.bardinpetr.delivery.monitor.validator.models.RequestActors;
import ru.bardinpetr.delivery.monitor.validator.models.RuleValidatorBuilder;

import java.util.HashMap;
import java.util.Map;

public class Main {

    public static void main(String[] args) {
        Map<String, Object> configs = new HashMap<>();
        configs.put("bootstrap.servers", "localhost:9092");
        configs.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "latest");
        configs.put(ConsumerConfig.GROUP_ID_CONFIG, "monitor-%d".formatted(Math.round(Math.random() * 10e6)));

        var validators = new IValidator[]{
                new RuleValidatorBuilder()
                        .addRule(
                                Action1Request.class,
                                new ActionRulesBuilder()
                                        .setDefaultMode(AllowMode.DENY)
                                        .allow(new RequestActors("test", "smth"))
                                        .build()
                        )
                        .build()
        };

        var consumer = new MonitorService(configs, validators);
        consumer.start();
    }
}
