package ru.bardinpetr.delivery.backend.fms.server;

import io.javalin.Javalin;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import ru.bardinpetr.delivery.libs.messages.msg.ccu.InputDeliveryTask;

import java.util.function.Function;


/**
 * Handles fleet robot addition with GET request /newRobot?url=0.0.0.0:0000
 * Accepts HTTP POST requests on /newTask with JSON InputDeliveryTask. Assigns task to robot and sends over CS.
 */
@Slf4j
public class FMSHTTPServer extends Thread {

    private final Javalin app;
    private final int port;

    @Setter
    private Function<String, String> onNewRobot;
    @Setter
    private Function<InputDeliveryTask, String> onStart;


    public FMSHTTPServer(int port) {
        this.port = port;
        app = Javalin.create()
                .post("newTask", ctx -> {
                    if (onStart == null) return;
                    var request = ctx.bodyAsClass(InputDeliveryTask.class);
                    ctx.result(onStart.apply(request));
                })
                .get("newRobot", ctx -> {
                    if (onNewRobot == null) return;
                    ctx.result(onNewRobot.apply(ctx.queryParam("url")));
                });
    }

    @Override
    public void run() {
        app.start("0.0.0.0", port);
    }
}
