package ru.bardinpetr.delivery.common.monitor;

import ru.bardinpetr.delivery.common.libs.messages.kafka.CommonKafkaConfiguration;
import ru.bardinpetr.delivery.common.libs.messages.msg.authentication.CreatePINRequest;
import ru.bardinpetr.delivery.common.libs.messages.msg.authentication.CreatePINResponse;
import ru.bardinpetr.delivery.common.libs.messages.msg.authentication.PINTestRequest;
import ru.bardinpetr.delivery.common.libs.messages.msg.ccu.DeliveryStatusRequest;
import ru.bardinpetr.delivery.common.libs.messages.msg.ccu.NewTaskRequest;
import ru.bardinpetr.delivery.common.libs.messages.msg.hmi.PINEnterRequest;
import ru.bardinpetr.delivery.common.libs.messages.msg.hmi.PINValidationResponse;
import ru.bardinpetr.delivery.common.libs.messages.msg.location.PositionReply;
import ru.bardinpetr.delivery.common.libs.messages.msg.location.PositionRequest;
import ru.bardinpetr.delivery.common.libs.messages.msg.locker.LockerDoorClosedRequest;
import ru.bardinpetr.delivery.common.libs.messages.msg.locker.LockerOpenRequest;
import ru.bardinpetr.delivery.common.libs.messages.msg.motion.*;
import ru.bardinpetr.delivery.common.libs.messages.msg.sensors.HumanDetectedRequest;
import ru.bardinpetr.delivery.common.libs.messages.msg.sensors.HumanDetectionConfigRequest;
import ru.bardinpetr.delivery.common.monitor.validator.IValidator;
import ru.bardinpetr.delivery.common.monitor.validator.models.ActionRulesBuilder;
import ru.bardinpetr.delivery.common.monitor.validator.models.AllowMode;
import ru.bardinpetr.delivery.common.monitor.validator.validators.RuleValidatorBuilder;
import ru.bardinpetr.delivery.common.monitor.validator.validators.TimeIntervalValidator;

import java.util.Map;

import static ru.bardinpetr.delivery.common.libs.messages.msg.Unit.*;

public class Main {

    public static void main(String[] args) {
        var configs = CommonKafkaConfiguration.getKafkaGlobalParams(
                Configuration.getKafkaURI(),
                "monitor"
        );

        var validators = new IValidator[]{
                createRuleValidator(),
                createTimeValidator()
        };

        var consumer = new MonitorService(configs, validators);
        consumer.start();
    }

    private static IValidator createTimeValidator() {
        return new TimeIntervalValidator(Map.of(
                PINEnterRequest.class, 3 * 1000L
        ));
    }

    private static IValidator createRuleValidator() {
        return new RuleValidatorBuilder()
                .setDefaultMode(AllowMode.DENY)
                .addRule(
                        CreatePINRequest.class,
                        new ActionRulesBuilder()
                                .allow(FMS, AUTH)
                                .build()
                )
                .addRule(
                        CreatePINResponse.class,
                        new ActionRulesBuilder()
                                .allow(AUTH, FMS)
                                .build()
                )
                .addRule(
                        PINTestRequest.class,
                        new ActionRulesBuilder()
                                .allow(AUTH, AUTH)
                                .build()
                )
                .addRule(
                        DeliveryStatusRequest.class,
                        new ActionRulesBuilder()
                                .allow(CCU, FMS)
                                .allow(CCU, COMM)
                                .allow(COMM, FMS)
                                .build()
                )
                .addRule(
                        NewTaskRequest.class,
                        new ActionRulesBuilder()
                                .allow(FMS, CCU)
                                .allow(COMM, CCU)
                                .allow(FMS, COMM)
                                .build()
                )
                .addRule(
                        PINEnterRequest.class,
                        new ActionRulesBuilder()
                                .allow(HMI, CCU)
                                .build()
                )
                .addRule(
                        PINValidationResponse.class,
                        new ActionRulesBuilder()
                                .allow(CCU, HMI)
                                .build()
                )
                .addRule(
                        PositionRequest.class,
                        new ActionRulesBuilder()
                                .allow(CCU, LOC)
                                .allow(LOC, POS_ODOM)
                                .allow(LOC, ODOM2)
                                .allow(POS_ODOM, MOTION)
                                .allow(ODOM2, MOTION)
                                .allow(SENSORS, LOC)
                                .build()
                )
                .addRule(
                        PositionReply.class,
                        new ActionRulesBuilder()
                                .allow(MOTION, POS_ODOM)
                                .allow(MOTION, ODOM2)
                                .allow(POS_ODOM, LOC)
                                .allow(ODOM2, LOC)
                                .allow(LOC, CCU)
                                .allow(LOC, SENSORS)
                                .build()
                )
                .addRule(
                        LockerOpenRequest.class,
                        new ActionRulesBuilder()
                                .allow(CCU, LOCKER)
                                .build()
                )
                .addRule(
                        LockerDoorClosedRequest.class,
                        new ActionRulesBuilder()
                                .allow(LOCKER, CCU)
                                .build()
                )
                .addRule(
                        GetMotionDataRequest.class,
                        new ActionRulesBuilder()
                                .allow(POS_ODOM, MOTION)
                                .allow(ODOM2, MOTION)
                                .build()
                )
                .addRule(
                        GetMotionDataReply.class,
                        new ActionRulesBuilder()
                                .allow(MOTION, POS_ODOM)
                                .allow(MOTION, ODOM2)
                                .build()
                )
                .addRule(
                        GetRestrictionsRequest.class,
                        new ActionRulesBuilder()
                                .allow(CCU, MOTION)
                                .allow(LOC, MOTION)
                                .build()
                )
                .addRule(
                        GetRestrictionsReply.class,
                        new ActionRulesBuilder()
                                .allow(MOTION, CCU)
                                .allow(MOTION, LOC)
                                .build()
                )
                .addRule(
                        SetSpeedRequest.class,
                        new ActionRulesBuilder()
                                .allow(CCU, MOTION)
                                .allow(LOC, MOTION)
                                .build()
                )
                .addRule(
                        HumanDetectionConfigRequest.class,
                        new ActionRulesBuilder()
                                .allow(CCU, SENSORS)
                                .build()
                )
                .addRule(
                        HumanDetectedRequest.class,
                        new ActionRulesBuilder()
                                .allow(SENSORS, CCU)
                                .build()
                )
                .build();
    }
}
