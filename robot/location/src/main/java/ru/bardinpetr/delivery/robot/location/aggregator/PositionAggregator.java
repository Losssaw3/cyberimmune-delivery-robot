package ru.bardinpetr.delivery.robot.location.aggregator;

import lombok.extern.slf4j.Slf4j;
import ru.bardinpetr.delivery.common.libs.messages.msg.location.Position;

import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


/**
 * Service to aggregate positions from different services.
 * On update checks is service could be tampered by comparing with average position from valid services
 * and by checking average speed from last known point
 */
@Slf4j
public class PositionAggregator {
    private final static double SPEED_TOLERANCE = 1 + 0.3;

    private final Map<String, PositionProviderDescription> providers = new HashMap<>();
    private Position lastBeforeAllTampered = new Position();
    private final double maxSpeedTrigger;
    private final double positionDifferenceTamperedTrigger;


    public PositionAggregator(double maxSpeedTamperedTrigger, double positionDifferenceTamperedTrigger) {
        this.maxSpeedTrigger = maxSpeedTamperedTrigger * SPEED_TOLERANCE;
        this.positionDifferenceTamperedTrigger = positionDifferenceTamperedTrigger;
    }

    public void update(String name, Position newPosition, int accuracy) {
        if (!providers.containsKey(name))
            providers.put(name, new PositionProviderDescription(name));
        var service = providers.get(name);

        newPosition.setTimestampSeconds(Instant.now().getEpochSecond());
        var speed = service.getLastSegmentSpeed(newPosition);

        log.info("Recorded position from {} -> {}", name, newPosition);

        service.addPosition(newPosition);

        if (getValidProviders().size() == 0) return;
        var distanceFromValid = getAveragePosition().distance(newPosition);

        if (speed > maxSpeedTrigger) {
            service.setTampered();
            log.warn("Set {} tampered because of speed limit {}", name, speed);
        }

        var speedCorrectedDistanceTrigger =
                positionDifferenceTamperedTrigger + maxSpeedTrigger * service.getAge(newPosition);
        if (distanceFromValid > speedCorrectedDistanceTrigger) {
            service.setTampered();
            log.warn("Set {} tampered because of position difference {}", name, distanceFromValid);
        }
    }

    public Position getAveragePosition() {
        var valid = getValidProviders();
        if (valid.size() == 0) {
            log.error("No providers valid now");
            return lastBeforeAllTampered;
        }

        var sum = valid
                .stream()
                .map(i -> providers.get(i).getLastPosition())
                .reduce(new Position(0, 0), Position::plus);
        var res = sum.divide(valid.size());

        lastBeforeAllTampered = res.clone();
        return res;
    }

    /**
     * @return name list of position-providing units not marked as tampered yet
     */
    public List<String> getValidProviders() {
        return providers
                .values().stream()
                .filter(provider -> provider.isActive() && !provider.isTampered())
                .map(PositionProviderDescription::getName)
                .toList();
    }

    /**
     * Get time difference from now to last updated position
     *
     * @return time in seconds
     */
    public long positionAge() {
        return Instant.now().getEpochSecond() - getAveragePosition().getTimestampSeconds();
    }
}
