package ru.bardinpetr.delivery.robot.location.aggregator;

import lombok.Getter;
import lombok.ToString;
import ru.bardinpetr.delivery.libs.messages.models.location.Position;

import java.util.ArrayDeque;
import java.util.Queue;

@Getter
@ToString
public class PositionProviderDescription {
    private static final int MAX_STORED_COUNT = 10;

    private final String name;
    private final Queue<Position> positions = new ArrayDeque<>();
    private boolean tampered = false;
    private Position lastPosition = null;


    public PositionProviderDescription(String name) {
        this.name = name;
    }


    /**
     * Stores new position
     *
     * @param position new position
     * @return average speed between this point and last stored point
     */
    public double addPosition(Position position) {
        positions.add(position);
        if (positions.size() > MAX_STORED_COUNT) positions.remove();

        if (lastPosition != null) {
            double positionDelta = lastPosition.distance(position);
            long timeDelta = position.getTimestampSeconds() - lastPosition.getTimestampSeconds();
            return positionDelta / timeDelta;
        }
        lastPosition = position;
        return 0;
    }

    public void setTampered() {
        tampered = true;
    }
}
