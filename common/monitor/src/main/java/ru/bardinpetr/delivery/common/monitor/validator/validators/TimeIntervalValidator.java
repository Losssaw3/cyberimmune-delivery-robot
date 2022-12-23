package ru.bardinpetr.delivery.common.monitor.validator.validators;

import lombok.AllArgsConstructor;
import lombok.Data;
import ru.bardinpetr.delivery.common.libs.messages.msg.MessageRequest;
import ru.bardinpetr.delivery.common.monitor.validator.IValidator;

import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

public class TimeIntervalValidator implements IValidator {

    private final Map<Class<? extends MessageRequest>, TimeState> times = new HashMap<>();

    public TimeIntervalValidator(Map<Class<? extends MessageRequest>, Long> minIntervalsMillis) {
        minIntervalsMillis.forEach((k, v) -> times.put(k, new TimeState(0L, v)));
    }

    @Override
    public boolean verify(MessageRequest request) {
        var cur = times.get(request.getClass());
        if (cur == null) return true;

        boolean res = (millis() - cur.last) > cur.min;
        cur.last = millis();

        return res;
    }

    private Long millis() {
        return Calendar.getInstance().getTimeInMillis();
    }

    @Data
    @AllArgsConstructor
    private static class TimeState {
        private Long last;
        private Long min;
    }
}
