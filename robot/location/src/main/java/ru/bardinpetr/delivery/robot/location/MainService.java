package ru.bardinpetr.delivery.robot.location;

import ru.bardinpetr.delivery.libs.messages.kafka.consumers.MonitoredKafkaConsumerFactory;
import ru.bardinpetr.delivery.libs.messages.kafka.consumers.MonitoredKafkaConsumerService;
import ru.bardinpetr.delivery.libs.messages.kafka.consumers.MonitoredKafkaConsumerServiceBuilder;
import ru.bardinpetr.delivery.libs.messages.kafka.consumers.MonitoredKafkaRequesterService;
import ru.bardinpetr.delivery.libs.messages.kafka.producers.MonitoredKafkaProducerFactory;
import ru.bardinpetr.delivery.libs.messages.kafka.producers.MonitoredKafkaProducerService;
import ru.bardinpetr.delivery.libs.messages.models.Units;
import ru.bardinpetr.delivery.libs.messages.models.location.PositionReply;
import ru.bardinpetr.delivery.libs.messages.models.location.PositionRequest;
import ru.bardinpetr.delivery.robot.location.aggregator.PositionAggregator;

import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * An aggregator for multiple hardware positioning drivers.
 */
public class MainService {

    private final MonitoredKafkaConsumerService consumerService;
    private final MonitoredKafkaProducerService producerService;

    private final MonitoredKafkaRequesterService requesterService;
    private final PositionAggregator aggregator;

    public MainService(MonitoredKafkaConsumerFactory consumerFactory,
                       MonitoredKafkaProducerFactory producerFactory,
                       List<String> aggregatedServices,
                       PositionAggregator aggregator,
                       int updateIntervalSeconds) {
        this.aggregator = aggregator;

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

        aggregatedServices.forEach(
                serviceName -> executor
                        .scheduleWithFixedDelay(
                                () -> updateService(serviceName),
                                60,
                                updateIntervalSeconds,
                                TimeUnit.SECONDS
                        )
        );
    }

    private void updateService(String service) {
        try {
            var res = (PositionReply)
                    requesterService
                            .request(service, new PositionRequest())
                            .get(30, TimeUnit.SECONDS);

            aggregator.update(service, res.getPosition(), res.getAccuracy());
        } catch (InterruptedException | ExecutionException | TimeoutException e) {
        }
    }

    private void positionRequest(PositionRequest request) {
        producerService.sendReply(
                request,
                new PositionReply(
                        aggregator.getAveragePosition(),
                        aggregator.getValidProviders().size()
                )
        );
    }

    public void start() {
        requesterService.start();
        consumerService.start();
    }
}
