package ru.bardinpetr.delivery.robot.communication.server;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;
import lombok.extern.slf4j.Slf4j;
import ru.bardinpetr.delivery.libs.messages.kafka.deserializers.MonitoredNonBusDeserializer;
import ru.bardinpetr.delivery.libs.messages.msg.MessageRequest;

import java.io.IOException;
import java.net.InetSocketAddress;


/**
 * Accepts HTTP POST requests on /msg with JSON eligible to be redirected to the monitor,
 * i.e. have valid ser/des request class, have sender/recipient and payload according to libs.messages.msg
 */
@Slf4j
public class CommHTTPServerService extends Thread {

    private final HttpServer server;
    private final MonitoredNonBusDeserializer deserializer;

    private IRequestListener requestCallback;


    public CommHTTPServerService(int port) {
        try {
            server = HttpServer.create(new InetSocketAddress(port), 0);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        server.createContext("/msg", this::handle);
        server.setExecutor(null);

        deserializer = new MonitoredNonBusDeserializer();
    }

    @Override
    public void run() {
        server.start();
    }

    private void reply(HttpExchange exchange, int code, String body) {
        try {
            exchange.sendResponseHeaders(code, body.length());

            var output = exchange.getResponseBody();
            output.write(body.getBytes());
            output.flush();
            output.close();
        } catch (IOException ignored) {
            log.error("Server http send error", ignored);
        }
    }

    private void handle(HttpExchange exchange) {
        var headers = exchange.getRequestHeaders();
        int contentLength = Integer.parseInt(headers.getFirst("Content-length"));

        if (!exchange.getRequestMethod().equals("POST") ||
                !headers.getFirst("Content-type").equals("application/json") ||
                contentLength == 0) {
            this.reply(exchange, 400, "invalid request");
            return;
        }

        MessageRequest msg;
        try {
            byte[] body = new byte[contentLength];
            exchange.getRequestBody().read(body);

            msg = deserializer.deserialize(body);
        } catch (Exception ex) {
            this.reply(exchange, 500, "unable to process given message");
            return;
        }

        if (!msg.isValid()) {
            this.reply(exchange, 500, "Invalid message type");
            return;
        }

        if (requestCallback != null)
            requestCallback.onMessage(msg);

        this.reply(exchange, 200, "OK");
    }

    public void setRequestCallback(IRequestListener requestCallback) {
        this.requestCallback = requestCallback;
    }
}
