package ru.bardinpetr.delivery.common.communication.server;

import io.javalin.Javalin;
import io.javalin.http.Context;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import ru.bardinpetr.delivery.common.libs.messages.kafka.deserializers.MonitoredNonBusDeserializer;
import ru.bardinpetr.delivery.common.libs.messages.msg.MessageRequest;


/**
 * Accepts HTTP POST requests on /msg with JSON eligible to be redirected to the monitor,
 * i.e. have valid ser/des request class, have sender/recipient and payload according to libs.messages.msg
 */
@Slf4j
public class CommHTTPServerService extends Thread {

    private final MonitoredNonBusDeserializer deserializer;
    private final Javalin app;
    private final int port;

    @Setter
    private IRequestListener requestCallback;

    public CommHTTPServerService(int port) {
        this.port = port;

        app = Javalin.create()
                .post("msg", this::onMessage);

        deserializer = new MonitoredNonBusDeserializer();
    }

    private void onMessage(Context ctx) {
        var body = ctx.body();

        MessageRequest msg;
        try {
            msg = deserializer.deserialize(body);
        } catch (Exception ex) {
            ctx.status(500);
            return;
        }

        if (!msg.isValid()) {
            ctx.status(400);
            return;
        }

        if (requestCallback != null)
            requestCallback.onMessage(msg);

        ctx.status(200);
    }

    @Override
    public void run() {
        app.start("0.0.0.0", port);
    }
}
