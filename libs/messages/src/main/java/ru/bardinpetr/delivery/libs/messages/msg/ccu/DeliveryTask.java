package ru.bardinpetr.delivery.libs.messages.msg.ccu;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import ru.bardinpetr.delivery.libs.messages.msg.location.Position;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DeliveryTask {
    private String userId;
    private Position position;
    private String pin;


    /**
     * Get string used to generate signature of task.
     *
     * @return string representation of components to sign
     */
    public String toSignString() {
        return "%s#%s".formatted(position.getX(), position.getY());
    }
}
