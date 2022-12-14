package ru.bardinpetr.delivery.robot.location;

import lombok.extern.slf4j.Slf4j;
import ru.bardinpetr.delivery.libs.messages.kafka.consumers.MonitoredKafkaConsumerFactory;
import ru.bardinpetr.delivery.libs.messages.kafka.consumers.MonitoredKafkaConsumerService;
import ru.bardinpetr.delivery.libs.messages.kafka.consumers.MonitoredKafkaConsumerServiceBuilder;
import ru.bardinpetr.delivery.libs.messages.kafka.consumers.MonitoredKafkaRequesterService;
import ru.bardinpetr.delivery.libs.messages.kafka.producers.MonitoredKafkaProducerFactory;
import ru.bardinpetr.delivery.libs.messages.kafka.producers.MonitoredKafkaProducerService;
import ru.bardinpetr.delivery.libs.messages.msg.Units;
import ru.bardinpetr.delivery.libs.messages.msg.location.PositionReply;
import ru.bardinpetr.delivery.libs.messages.msg.location.PositionRequest;
import ru.bardinpetr.delivery.robot.location.aggregator.PositionAggregator;

import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * An aggregator for multiple hardware positioning drivers.
 */
@Slf4j
public class MainService {

    private final MonitoredKafkaConsumerService consumerService;
    private final MonitoredKafkaProducerService producerService;

    private final MonitoredKafkaRequesterService requesterService;
    private final PositionAggregator aggregator;
    private final int updateIntervalSeconds;
    private final List<String> aggregatedServices;

    public MainService(MonitoredKafkaConsumerFactory consumerFactory,
                       MonitoredKafkaProducerFactory producerFactory,
                       List<String> aggregatedServices,
                       PositionAggregator aggregator,
                       int updateIntervalSeconds) {
        this.aggregator = aggregator;
        this.updateIntervalSeconds = updateIntervalSeconds;
        this.aggregatedServices = aggregatedServices;

        consumerService = new MonitoredKafkaConsumerServiceBuilder(Units.LOC.toString())
                .setConsumerFactory(consumerFactory)
                .subscribe(PositionRequest.class, this::positionRequest)
                .build();

        producerService = new MonitoredKafkaProducerService(
                Units.LOC.toString(),
                producerFactory
        );

        requesterService = new MonitoredKafkaRequesterService(
                Units.LOC.toString(),
                List.of(PositionReply.class),
                producerFactory,
                consumerFactory
        );

        var executor = Executors.newSingleThreadScheduledExecutor();
        executor.scheduleWithFixedDelay(
                this::updateAll,
                updateIntervalSeconds,
                updateIntervalSeconds,
                TimeUnit.SECONDS
        );
    }

    private void updateAll() {
        aggregatedServices.forEach(this::updateService);
    }

    private void updateService(String service) {
        try {
            log.debug("Updating {}...", service);
            var res = (PositionReply)
                    requesterService
                            .request(service, new PositionRequest())
                            .get(updateIntervalSeconds / 2, TimeUnit.SECONDS);
            log.debug("Got {} from {}", res.getPosition(), service);
            aggregator.update(service, res.getPosition(), res.getAccuracy());
            log.debug("Updating {} finished", service);
        } catch (InterruptedException | ExecutionException | TimeoutException e) {
            log.warn("Service {} is not responding {}", service, e);
        }
    }

    private void positionRequest(PositionRequest request) {
        log.debug("Got position request");
        var lastTime = aggregator.positionAge();
        if (lastTime > updateIntervalSeconds / 5) {
            log.info("Going to update position as it is by {} seconds in past", lastTime);
            updateAll();
        }

        var providers = aggregator.getValidProviders();
        log.debug("Calculated position via {}", providers);
        producerService.sendReply(
                request,
                new PositionReply(
                        aggregator.getAveragePosition(),
                        providers.size()
                )
        );
    }

    public void start() {
        requesterService.start();
        consumerService.start();
        log.info("Started");
    }
}
