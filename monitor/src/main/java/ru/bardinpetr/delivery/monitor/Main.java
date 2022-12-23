package ru.bardinpetr.delivery.monitor;

import ru.bardinpetr.delivery.libs.messages.kafka.CommonKafkaConfiguration;
import ru.bardinpetr.delivery.libs.messages.msg.authentication.CreatePINRequest;
import ru.bardinpetr.delivery.libs.messages.msg.authentication.CreatePINResponse;
import ru.bardinpetr.delivery.libs.messages.msg.ccu.DeliveryStatusRequest;
import ru.bardinpetr.delivery.libs.messages.msg.ccu.NewTaskRequest;
import ru.bardinpetr.delivery.libs.messages.msg.hmi.PINEnterRequest;
import ru.bardinpetr.delivery.libs.messages.msg.hmi.PINValidationResponse;
import ru.bardinpetr.delivery.libs.messages.msg.location.PositionReply;
import ru.bardinpetr.delivery.libs.messages.msg.location.PositionRequest;
import ru.bardinpetr.delivery.libs.messages.msg.locker.LockerDoorClosedRequest;
import ru.bardinpetr.delivery.libs.messages.msg.locker.LockerOpenRequest;
import ru.bardinpetr.delivery.libs.messages.msg.motion.*;
import ru.bardinpetr.delivery.libs.messages.msg.sensors.HumanDetectedRequest;
import ru.bardinpetr.delivery.libs.messages.msg.sensors.HumanDetectionConfigRequest;
import ru.bardinpetr.delivery.monitor.validator.IValidator;
import ru.bardinpetr.delivery.monitor.validator.models.ActionRulesBuilder;
import ru.bardinpetr.delivery.monitor.validator.models.AllowMode;
import ru.bardinpetr.delivery.monitor.validator.models.RuleValidatorBuilder;

import static ru.bardinpetr.delivery.libs.messages.msg.Unit.*;

public class Main {

    public static void main(String[] args) {
        var configs = CommonKafkaConfiguration.getKafkaGlobalParams(
                Configuration.getKafkaURI(),
                "monitor"
        );

        var validators = new IValidator[]{
                new RuleValidatorBuilder()
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
                                DeliveryStatusRequest.class,
                                new ActionRulesBuilder()
                                        .allow(CCU, FMS)
                                        .build()
                        )
                        .addRule(
                                NewTaskRequest.class,
                                new ActionRulesBuilder()
                                        .allow(FMS, CCU)
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
                                        .allow(POS_ODOM, MOTION)
                                        .build()
                        )
                        .addRule(
                                PositionReply.class,
                                new ActionRulesBuilder()
                                        .allow(MOTION, POS_ODOM)
                                        .allow(POS_ODOM, LOC)
                                        .allow(LOC, CCU)
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
                                        .build()
                        )
                        .addRule(
                                GetMotionDataReply.class,
                                new ActionRulesBuilder()
                                        .allow(MOTION, POS_ODOM)
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
                        .build()
        };

        var consumer = new MonitorService(configs, validators);
        consumer.start();
    }
}
