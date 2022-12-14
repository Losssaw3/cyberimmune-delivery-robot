package ru.bardinpetr.delivery.robot.location.aggregator;

import lombok.Getter;
import lombok.ToString;
import ru.bardinpetr.delivery.libs.messages.msg.location.Position;

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
     */
    public void addPosition(Position position) {
        positions.add(position);
        if (positions.size() > MAX_STORED_COUNT) positions.remove();
        lastPosition = position;
    }

    /**
     * @return average speed between this point and last stored point
     */
    public double getLastSegmentSpeed(Position position) {
        if (lastPosition == null) return 0;
        double positionDelta = lastPosition.distance(position);
        return positionDelta / getAge(position);
    }

    public long getAge(Position position) {
        return position.getTimestampSeconds() - lastPosition.getTimestampSeconds();
    }

    public void setTampered() {
        tampered = true;
    }

    public boolean isActive() {
        return lastPosition != null;
    }
}
