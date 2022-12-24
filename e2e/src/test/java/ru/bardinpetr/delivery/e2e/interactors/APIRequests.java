package ru.bardinpetr.delivery.e2e.interactors;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

public class APIRequests {
    private static final HttpClient client = HttpClient.newHttpClient();

    private static String sendGet(String baseUrl, String query) {
        var request = HttpRequest
                .newBuilder()
                .uri(URI.create("%s/%s".formatted(baseUrl, query)))
                .GET()
                .build();
        try {
            var resp = client.send(request, HttpResponse.BodyHandlers.ofString());
            return resp.body();
        } catch (IOException | InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    public static void createRobot(String baseUrl, String url) {
        sendGet(baseUrl, "newRobot?url=%s".formatted(url));
    }

    public static void createTask(String baseUrl, double x, double y) {
        sendGet(baseUrl, "newTask?x=%f&y=%f".formatted(x, y));
    }

    public static String submitPIN(String baseUrl, String pin) {
        return sendGet(baseUrl, "pin?code=%s".formatted(pin));
    }
}
