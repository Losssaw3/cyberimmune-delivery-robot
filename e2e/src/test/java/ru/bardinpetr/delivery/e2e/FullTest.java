package ru.bardinpetr.delivery.e2e;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.*;
import ru.bardinpetr.delivery.common.libs.messages.kafka.CommonKafkaConfiguration;
import ru.bardinpetr.delivery.common.libs.messages.kafka.consumers.MonitoredKafkaConsumerFactory;
import ru.bardinpetr.delivery.common.libs.messages.kafka.producers.MonitoredKafkaProducerFactory;
import ru.bardinpetr.delivery.common.libs.messages.msg.Unit;
import ru.bardinpetr.delivery.common.libs.messages.msg.authentication.PINTestRequest;
import ru.bardinpetr.delivery.common.libs.messages.msg.ccu.DeliveryStatus;
import ru.bardinpetr.delivery.common.libs.messages.msg.ccu.DeliveryStatusRequest;
import ru.bardinpetr.delivery.common.libs.messages.msg.ccu.NewTaskRequest;
import ru.bardinpetr.delivery.common.libs.messages.msg.location.Position;
import ru.bardinpetr.delivery.e2e.interactors.APIRequests;
import ru.bardinpetr.delivery.e2e.suite.BusTestSuite;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

@Slf4j
public class FullTest {

    private static final String ROBOT_BROKER_URI = "localhost:9031";
    private static final String ROBOT_COMM_URI = "robot-com:9011";
    private static final String SERVER_BROKER_URI = "localhost:9030";
    private static final String SERVER_STORE_URI = "http://localhost:9044";

    private final BusTestSuite robotBusSuite;
    private final BusTestSuite serverBusSuite;
    private Position requestedPosition;


    public FullTest() throws InterruptedException {
        log.warn("Setup started. This takes time for kafka to initialize");

        var robotParams = CommonKafkaConfiguration.getKafkaGlobalParams(ROBOT_BROKER_URI, "TEST-MB-R");
        robotBusSuite = new BusTestSuite(
                new MonitoredKafkaConsumerFactory(robotParams),
                new MonitoredKafkaProducerFactory(robotParams)
        );
        robotBusSuite.start();

        var serverParams = CommonKafkaConfiguration.getKafkaGlobalParams(SERVER_BROKER_URI, "TEST-MB-S");
        serverBusSuite = new BusTestSuite(
                new MonitoredKafkaConsumerFactory(serverParams),
                new MonitoredKafkaProducerFactory(serverParams)
        );
        serverBusSuite.start();
    }

    @BeforeAll
    static void setup() throws InterruptedException {
        log.warn("Docker-compose for robot and server should be started manually!");
        Thread.sleep(20000);
    }

    @DisplayName("Normal actions according to business model should result in successful delivery completion")
    @Test
    @Order(0)
    void startDelivery() throws ExecutionException, InterruptedException {
        log.info("Sending new task");

        // Create robot
        APIRequests.createRobot(SERVER_STORE_URI, ROBOT_COMM_URI);

        // Send task to web market
        requestedPosition = new Position(20, 30);
        APIRequests.createTask(SERVER_STORE_URI, requestedPosition.getX(), requestedPosition.getY());

        log.info("Waiting for task to be processed");

        // Wait for task to be processed by FMS and sent to robot
        var isPin = serverBusSuite.awaitMessage(
                PINTestRequest.class,
                Unit.AUTH.toString(),
                30, TimeUnit.SECONDS
        );
        var isSent = serverBusSuite.awaitMessage(
                NewTaskRequest.class,
                Unit.COMM.toString(),
                30, TimeUnit.SECONDS
        );

        var pinResp = isPin.get();
        Assertions.assertNotNull(pinResp, "PIN should be generated for task");
        var pin = ((PINTestRequest) pinResp).getPin();
        log.info("Got pin: {}", pin);

        Assertions.assertNotNull(isSent.get(), "FMS should trigger delivery start via S-CS for valid tasks");
        log.info("Task sent successfully");

        // Check if task is received by CCU and acknowledged
        var isReceived = robotBusSuite.awaitMessage(
                NewTaskRequest.class,
                Unit.CCU.toString(),
                60, TimeUnit.SECONDS
        );
        var isNotifiedStart = serverBusSuite.awaitMessage(
                DeliveryStatusRequest.class,
                Unit.FMS.toString(),
                60, TimeUnit.SECONDS
        );

        Assertions.assertNotNull(isReceived.get(), "Robot should receive valid task via R-CS and forward to CCU");
        log.info("CCU received task successfully");

        var notification0 = isNotifiedStart.get();
        Assertions.assertNotNull(notification0, "FMS should have received notification of delivery start for valid tasks");
        Assertions.assertEquals(DeliveryStatus.RUNNING, ((DeliveryStatusRequest) notification0).getStatus(),
                "FMS should have received notification of delivery start for valid tasks");
        log.info("Robot replied to server delivery started");
    }

}
