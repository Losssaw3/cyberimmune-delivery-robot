package ru.bardinpetr.delivery.robot.location.aggregator;

import ru.bardinpetr.delivery.libs.messages.msg.location.Position;

import java.util.HashMap;
import java.util.List;
import java.util.Map;


/**
 * Service to aggregate positions from different services.
 * On update checks is service could be tampered by comparing with average position from valid services
 * and by checking average speed from last known point
 */
public class PositionAggregator {


    private final Map<String, PositionProviderDescription> providers = new HashMap<>();
    private final double maxSpeedTrigger;
    private final double positionDifferenceTamperedTrigger;

    public PositionAggregator(double maxSpeedTamperedTrigger, double positionDifferenceTamperedTrigger) {
        this.maxSpeedTrigger = maxSpeedTamperedTrigger;
        this.positionDifferenceTamperedTrigger = positionDifferenceTamperedTrigger;
    }

    public void update(String name, Position newPosition, int accuracy) {
        if (!providers.containsKey(name))
            providers.put(name, new PositionProviderDescription(name));
        var service = providers.get(name);
        var speed = service.addPosition(newPosition);
        var distanceFromValid = getAveragePosition().distance(newPosition);
        if (speed > maxSpeedTrigger || distanceFromValid > positionDifferenceTamperedTrigger)
            service.setTampered();
    }

    public Position getAveragePosition() {
        var valid = getValidProviders();
        var result = valid
                .stream()
                .map(i -> providers.get(i).getLastPosition())
                .reduce(new Position(0, 0), Position::plus);
        return result.divide(valid.size());
    }

    public List<String> getValidProviders() {
        return providers
                .values().stream()
                .filter(provider -> !provider.isTampered())
                .map(PositionProviderDescription::getName)
                .toList();
    }
}
