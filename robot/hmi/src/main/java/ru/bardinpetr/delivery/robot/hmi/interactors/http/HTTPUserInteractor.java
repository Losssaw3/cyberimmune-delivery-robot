package ru.bardinpetr.delivery.robot.hmi.interactors.http;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;
import ru.bardinpetr.delivery.robot.hmi.interactors.IUserInteractor;
import ru.bardinpetr.delivery.robot.hmi.interactors.PinHandler;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.regex.Pattern;

/**
 * User interface for HMI unit based on HTTP GET requests.
 * Exposes a server with endpoint /pin; use 'code' param to send 6-digit pin code
 */
public class HTTPUserInteractor implements IUserInteractor {

    private static final Pattern pinQueryPattern = Pattern.compile("^code=(\\d+)$", Pattern.CASE_INSENSITIVE);
    private final HttpServer server;
    private PinHandler handler;

    public HTTPUserInteractor(int port) {
        try {
            server = HttpServer.create(new InetSocketAddress(port), 0);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        server.createContext("/pin", this::handle);
        server.setExecutor(null);
    }

    public void start() {
        server.start();
    }

    private void reply(HttpExchange exchange, int code, String body) throws IOException {
        exchange.sendResponseHeaders(code, body.length());

        var output = exchange.getResponseBody();
        output.write(body.getBytes());
        output.flush();
        output.close();
    }

    private void handle(HttpExchange exchange) throws IOException {
        if (!exchange.getRequestMethod().equals("GET")) {
            this.reply(exchange, 404, "invalid method");
            return;
        }

        var query = exchange.getRequestURI().getQuery();
        var match = pinQueryPattern.matcher(query);

        if (!match.matches()) {
            this.reply(exchange, 400, "invalid request. use /pin?code=123456");
            return;
        }
        String pin = match.group(1);

        String body = handler.process(pin);
        this.reply(exchange, 200, body);
    }

    @Override
    public void registerPINHandler(PinHandler handler) {
        this.handler = handler;
    }
}
