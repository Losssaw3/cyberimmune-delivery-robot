package ru.bardinpetr.delivery.e2e;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.*;
import ru.bardinpetr.delivery.common.libs.messages.kafka.CommonKafkaConfiguration;
import ru.bardinpetr.delivery.common.libs.messages.kafka.consumers.MonitoredKafkaConsumerFactory;
import ru.bardinpetr.delivery.common.libs.messages.kafka.producers.MonitoredKafkaProducerFactory;
import ru.bardinpetr.delivery.common.libs.messages.msg.Unit;
import ru.bardinpetr.delivery.common.libs.messages.msg.authentication.PINTestRequest;
import ru.bardinpetr.delivery.common.libs.messages.msg.ccu.*;
import ru.bardinpetr.delivery.common.libs.messages.msg.location.Position;
import ru.bardinpetr.delivery.common.libs.messages.msg.location.PositionReply;
import ru.bardinpetr.delivery.common.libs.messages.msg.location.PositionRequest;
import ru.bardinpetr.delivery.e2e.interactors.APIRequests;
import ru.bardinpetr.delivery.e2e.suite.BusTestSuite;

import java.util.Random;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertEquals;

@Slf4j
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class FullTest {

    private static final double DIST_THRESH = 20;

    private static final String ROBOT_BROKER_URI = "localhost:9031";
    private static final String ROBOT_COMM_URI = "robot-com:9011";
    private static final String SERVER_BROKER_URI = "localhost:9030";
    private static final String SERVER_STORE_URI = "http://localhost:9044";

    private BusTestSuite robotBusSuite;
    private BusTestSuite serverBusSuite;
    private Position requestedPosition;

    @BeforeAll
    void setup() throws InterruptedException {
        log.warn("Docker-compose for robot and server should be started manually!");
        log.warn("Setup started. This takes time for kafka to initialize");

        var robotParams = CommonKafkaConfiguration.getKafkaGlobalParams(ROBOT_BROKER_URI, "TEST-MB-R");
        robotBusSuite = new BusTestSuite(
                new MonitoredKafkaConsumerFactory(robotParams),
                new MonitoredKafkaProducerFactory(robotParams),
                2000);
        robotBusSuite.start();

        var serverParams = CommonKafkaConfiguration.getKafkaGlobalParams(SERVER_BROKER_URI, "TEST-MB-S");
        serverBusSuite = new BusTestSuite(
                new MonitoredKafkaConsumerFactory(serverParams),
                new MonitoredKafkaProducerFactory(serverParams),
                2000);
        serverBusSuite.start();

        log.info("Waiting kafka");
        Thread.sleep(20000);
        log.info("Started");
    }

    private void flush() {
        robotBusSuite.flushHistory();
        serverBusSuite.flushHistory();
    }

    @DisplayName("Robot should not start delivery for invalid task")
    @Test
    @Order(0)
    void invalidTaskTest() throws ExecutionException, InterruptedException {
        log.info("[START] invalid task scenario");
        flush();

        var req = new NewTaskRequest(
                new InputDeliveryTask(
                        "SOME INVALID SIGNATURE",
                        new DeliveryTask("id0", new Position(0, 0), "nope")
                )
        );
        req.setRecipient(Unit.CCU.toString());
        robotBusSuite.produceUnmonitored(req);
        log.info("Sent task with invalid signature");

        serverBusSuite
                .awaitMessages(
                        DeliveryStatusRequest.class, Unit.FMS,
                        60, TimeUnit.SECONDS
                )
                .assertArrivedThat(i -> DeliveryStatus.ENDED_ERR == i.getStatus(),
                        "For invalid tasks CCU should send an notification",
                        "CCU Status notification for invalid task should be of ENDED_ERR"
                );

        log.info("Robot did not start invalid task - ok");
        log.info("SCENARIO 1,2 PASSED");
    }

    @DisplayName("For valid task delivery should start")
    @Test
    @Order(1)
    void startDeliveryTest() throws ExecutionException, InterruptedException {
        log.info("[START] classic scenario");
//        Thread.sleep(3000);
        log.info("Starting sending new task");

        flush();

        APIRequests.createRobot(SERVER_STORE_URI, ROBOT_COMM_URI);

        // Send task to web market
        var rnd = new Random().ints(2, 100, 500).toArray();
        requestedPosition = new Position(rnd[0], rnd[1]);
        APIRequests.createTask(SERVER_STORE_URI, requestedPosition.getX(), requestedPosition.getY());

        log.info("Waiting for task to be processed");

        // Check if pin was issued
        var pin = serverBusSuite
                .awaitMessages(
                        PINTestRequest.class, Unit.AUTH,
                        30, TimeUnit.SECONDS
                )
                .assertArrived("PIN should be generated for task")
                .getPin();
        log.info("Got pin: {}", pin);

        // Check if task was sent from server side
        serverBusSuite
                .awaitMessages(
                        NewTaskRequest.class, Unit.COMM,
                        30, TimeUnit.SECONDS
                )
                .assertArrived("FMS should trigger delivery start via CS for valid tasks");

        log.info("Task delivered successfully");

        // Check if task is received by CCU and acknowledged
        robotBusSuite
                .awaitMessages(
                        NewTaskRequest.class, Unit.CCU,
                        30, TimeUnit.SECONDS
                )
                .assertArrived("Robot should receive valid task via CS and forward to CCU");

        log.info("CCU received task successfully");

        // Check if status response arrived to fms
        serverBusSuite
                .awaitMessages(
                        DeliveryStatusRequest.class, Unit.FMS,
                        30, TimeUnit.SECONDS
                )
                .assertArrivedValidated(
                        i -> assertEquals(DeliveryStatus.RUNNING, i.getStatus()),
                        "FMS should receive notification of delivery start for valid tasks",
                        "For valid tasks robot should start execution and return RUNNING status"
                );

        log.info("Robot replied to server that delivery has started");
    }

    @DisplayName("CCU should not take new tasks when already performing one")
    @Order(2)
    @Test
    void taskCopyTest() throws ExecutionException, InterruptedException {
        log.info("[START] repeated task scenario");
        flush();

        // Here is a hardcoded valid task FOR DEMO KEYS, it would fail with any others. Please note.
        var req = new NewTaskRequest(new InputDeliveryTask(
                "KBOGY+cmnDD2n8fW3fX5RKS10pNMXHpqPxMx+pVKI7QsWR/6nRrJmPqslEvHlI3bQMyNkedf0Q2MoLqek4b9d/Rva5f3gjKQMmuBlZY7bLG01GweZpliFeWGT8x5I1HmKdxbX1zYyzP+VZR2srbdBQshHGWBoQJWeoa1OLALhV1NubFIGR2AugXMz3bxvVtKRfxndFgX8IwpqvNBokxvGU9lKjm5FxMW2XmrUQ5Ks9Tu85edjp8GyjJO33AesOMZJNr1YNKUMRYPOzIsYC9knNLT4LQ5FF66PiEQS0q0UVVvxA7gYT21ZaTyM6xrWU4fdc8utlexBgtIEfBv4FD4tA==",
                new DeliveryTask("", new Position(20, 30), "0Y+4mFGzeU/g1HS4++xSeU6GWhfY71hGAwvMAe4lpwP1frRylcxyHaSVIXd3n+5ks82VOeqt2qaSp/LvnZHQlQ==")
        ));
        req.setRecipient(Unit.CCU.toString());
        robotBusSuite.produceUnmonitored(req);
        log.info("Sent valid task when robot in delivery");

        serverBusSuite
                .awaitMessages(
                        DeliveryStatusRequest.class,
                        Unit.FMS,
                        60, TimeUnit.SECONDS
                )
                .assertArrivedValidated(
                        i -> assertEquals(DeliveryStatus.ENDED_ERR, i.getStatus()),
                        "For duplicated tasks CCU should send an notification",
                        "CCU Status notification for task when not idle should be of ENDED_ERR"
                );

        log.info("Robot did not start invalid task - ok");
        log.info("SCENARIO 5 PASSED");
    }

    @DisplayName("Once delivery started robot should arrive to the destination")
    @Order(3)
    @Test
    void deliveryProcessTest() throws ExecutionException, InterruptedException {
        log.info("[START] destination test");
        flush();

        // Simulate malfunctioning/tampering of one location driver
        // As all location is request-reply, the only way to do it from here
        // is to intercept the request, copy ID, and then send many replies
        // with hope that one of them would arrive faster than real one.
        log.info("starting [mitm] on position driver ODOM2");
        var realRequest = robotBusSuite
                .awaitMessages(
                        PositionRequest.class, Unit.ODOM2,
                        30, TimeUnit.SECONDS
                )
                .assertArrived("When in progress of delivery, all position providers should be frequently pooled");
        log.info("[mitm] Got original message to odom2: ID{}", realRequest.getRequestId());

        var invalidMsg = new PositionReply(new Position(12345, 12345), 1);
        invalidMsg.setSender("odom2");
        invalidMsg.setRecipient(Unit.LOC.toString());
        invalidMsg.setRequestId("reply-%s".formatted(realRequest.getRequestId()));

        for (int i = 0; i < 100; i++)
            robotBusSuite.produceUnmonitoredNoID(invalidMsg);

        log.info("[mitm] sent invalid replies to Location Aggregator and hope it would not die");

        log.info("Waiting for robot to arrive");
        robotBusSuite
                .awaitMessages(
                        DeliveryStatusRequest.class, Unit.COMM,
                        3, TimeUnit.MINUTES
                )
                .assertArrivedValidated(
                        i -> assertEquals(DeliveryStatus.ARRIVED_TO_CUSTOMER, i.getStatus()),
                        "Robot should be able to reach destination and notify of it",
                        "Robot should be able to reach destination and notify of it"
                );

        // To get the latest position we need to trigger request on behalf of some service and then intercept response for it
        log.info("Arrived. Trying to get real position");
        var req = new PositionRequest();
        req.setSender(Unit.CCU.toString());
        req.setRecipient(Unit.LOC.toString());
        robotBusSuite.produceUnmonitored(req);

        var posRequest = robotBusSuite
                .awaitMessages(
                        PositionReply.class, Unit.CCU,
                        10, TimeUnit.SECONDS
                )
                .doTakeLast()
                .assertArrivedThat(
                        i -> {
                            log.info("Arrived at: {}; Required: {}", i.getPosition(), requestedPosition);
                            return i.getPosition().distance(requestedPosition) < DIST_THRESH;
                        },
                        "Robot's location service do not reply with location on request",
                        "Robot arrived not at destination"
                );

        log.info("Robot arrived ok");
        log.info("Waiting for user to show up");
        robotBusSuite
                .awaitMessages(
                        DeliveryStatusRequest.class, Unit.COMM,
                        3, TimeUnit.MINUTES
                )
                .assertArrivedValidated(
                        i -> assertEquals(DeliveryStatus.HUMAN_DETECTED, i.getStatus()),
                        "Robot should notify of user presence",
                        "User presence wasn't established however state illegally changed"
                );
        log.info("User came to the robot");

        log.info("SCENARIO 11 PASSED");
    }


}
