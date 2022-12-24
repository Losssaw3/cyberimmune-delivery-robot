package ru.bardinpetr.delivery.backend.fms;

import lombok.extern.slf4j.Slf4j;
import ru.bardinpetr.delivery.backend.fms.robots.Robot;
import ru.bardinpetr.delivery.backend.fms.server.FMSHTTPServer;
import ru.bardinpetr.delivery.common.libs.messages.kafka.consumers.MonitoredKafkaConsumerFactory;
import ru.bardinpetr.delivery.common.libs.messages.kafka.consumers.MonitoredKafkaConsumerService;
import ru.bardinpetr.delivery.common.libs.messages.kafka.consumers.MonitoredKafkaConsumerServiceBuilder;
import ru.bardinpetr.delivery.common.libs.messages.kafka.consumers.MonitoredKafkaRequesterService;
import ru.bardinpetr.delivery.common.libs.messages.kafka.producers.MonitoredKafkaProducerFactory;
import ru.bardinpetr.delivery.common.libs.messages.kafka.producers.MonitoredKafkaProducerService;
import ru.bardinpetr.delivery.common.libs.messages.msg.Unit;
import ru.bardinpetr.delivery.common.libs.messages.msg.authentication.CreatePINRequest;
import ru.bardinpetr.delivery.common.libs.messages.msg.authentication.CreatePINResponse;
import ru.bardinpetr.delivery.common.libs.messages.msg.ccu.*;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * Receives new task via HTTP and sends it to the robot
 */
@Slf4j
public class MainService {

    public static final String SERVICE_NAME = Unit.FMS.toString();

    private final MonitoredKafkaConsumerService consumerService;
    private final MonitoredKafkaProducerService producerService;
    private final MonitoredKafkaRequesterService requesterService;

    private final FMSHTTPServer serverService;

    private final Map<String, Robot> robots;

    public MainService(MonitoredKafkaConsumerFactory consumerFactory,
                       MonitoredKafkaProducerFactory producerFactory,
                       FMSHTTPServer serverService) {
        consumerService = new MonitoredKafkaConsumerServiceBuilder(SERVICE_NAME)
                .setConsumerFactory(consumerFactory)
                .subscribe(DeliveryStatusRequest.class, this::onMessage)
                .build();

        producerService = new MonitoredKafkaProducerService(
                SERVICE_NAME,
                producerFactory
        );

        requesterService = new MonitoredKafkaRequesterService(
                SERVICE_NAME,
                List.of(CreatePINResponse.class),
                producerFactory,
                consumerFactory
        );

        robots = new HashMap<>();

        this.serverService = serverService;
        this.serverService.setOnNewRobot(this::newRobot);
        this.serverService.setOnStart(this::newTask);
    }

    private String newTask(InputDeliveryTask task) {
        log.debug("New task arrived via rest: {}", task);
        log.info("New task for {} at {}", task.getTask().getUserId(), task.getTask().getPosition());

        var available =
                robots
                        .values().stream()
                        .filter(r -> r.getStatus() == DeliveryStatus.IDLE)
                        .findFirst();

        if (available.isEmpty()) {
            log.warn("Could not start task because all robots now working");
            return "No robots are available at the moment";
        }

        var url = available.get().getUrl();

        log.info("Generating PIN for task");

        CreatePINResponse pinResponse;
        try {
            pinResponse =
                    (CreatePINResponse) requesterService
                            .request(Unit.AUTH.toString(), new CreatePINRequest(task.getTask().getUserId()))
                            .get(30, TimeUnit.SECONDS);
            log.info("Got PIN info: {}", pinResponse);
        } catch (InterruptedException | ExecutionException | TimeoutException e) {
            log.error("Error while getting pin: ", e);
            return "Could not communicate";
        }

        task.setTask(new DeliveryTask(
                "",
                task.getTask().getPosition(),
                pinResponse.getEncryptedPin()
        ));

        log.info("Sending task to {} robot: {}", url, task);
        producerService.sendVia(
                Unit.COMM.toString(),
                url,
                Unit.CCU.toString(),
                new NewTaskRequest(task)
        );

        log.info("Task sent");

        return "OK";
    }

    private void onMessage(DeliveryStatusRequest request) {
        var senderIp = request.getSenderBridgeURL();
        log.info("Got msg from robot: {}", senderIp);

        var robot =
                robots
                        .values().stream()
                        .filter(i -> i.getRealIP().equals(senderIp))
                        .findFirst();
        if (robot.isEmpty()) {
            log.warn("Got message for unknown robot: {}. Having {}", senderIp, robots);
            return;
        }

        var id = robot.get().getUrl();
        robots
                .get(id)
                .setStatus(request.getStatus());

        log.info("Status for {} updated to {}", id, request.getStatus());
    }

    private String newRobot(String url) {
        log.info("New robot at {}", url);
        if (robots.containsKey(url)) {
            return "exists";
        }

        var real = url;
        try {
            real = InetAddress
                    .getByName(url.replaceAll(":\\d+", ""))
                    .getHostAddress();
        } catch (UnknownHostException ignored) {
        }

        robots.put(url, new Robot(url, real));

        log.info("Updated robots: {}", robots.values());
        return "OK";
    }

    public void start() {
        requesterService.start();
        consumerService.start();
        serverService.start();
        log.info("Started");
    }
}
