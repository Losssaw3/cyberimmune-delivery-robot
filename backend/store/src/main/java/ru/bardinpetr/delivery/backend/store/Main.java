package ru.bardinpetr.delivery.backend.store;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.javalin.Javalin;
import ru.bardinpetr.delivery.common.libs.crypto.SignatureCryptoService;
import ru.bardinpetr.delivery.common.libs.crypto.keystore.KeystoreServiceSign;
import ru.bardinpetr.delivery.common.libs.messages.msg.ccu.DeliveryTask;
import ru.bardinpetr.delivery.common.libs.messages.msg.ccu.InputDeliveryTask;
import ru.bardinpetr.delivery.common.libs.messages.msg.location.Position;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

/**
 * This is just a demonstration of how a real web market should trigger delivery.
 * creates http server on 9044 with GET endpoints: /newRobot?url=roboturl:9010 and /newTask?x=40&y=60.
 * it is common to use:
 * http://0.0.0.0:9044/newRobot?url=robot-com:9011
 * http://0.0.0.0:9044/newTask?x=40&y=20
 */

public class Main {

    private static String fmsUrl;
    private static HttpClient client;
    private static SignatureCryptoService signService;

    public static void main(String[] args) {
        var env = System.getenv();
        client = HttpClient.newHttpClient();

        fmsUrl = env.get("FMS_URL");

        signService = new SignatureCryptoService(
                (new KeystoreServiceSign()).getPrivateFromKeystore(
                        env.get("KS_PATH"),
                        env.get("KS_PASS")
                )
        );

        Javalin.create()
                .get("newTask", ctx -> sendTask(
                        Double.parseDouble(ctx.queryParam("x")),
                        Double.parseDouble(ctx.queryParam("y"))
                ))
                .get("newRobot", ctx -> createRobot(ctx.queryParam("url")))
                .start(9044);
    }

    private static void createRobot(String robotUrl) {
        HttpRequest request = HttpRequest
                .newBuilder()
                .uri(URI.create("%s/newRobot?url=%s".formatted(
                        fmsUrl,
                        robotUrl
                )))
                .GET()
                .build();
        try {
            client.send(request, HttpResponse.BodyHandlers.ofString());
        } catch (IOException | InterruptedException e) {
            throw new RuntimeException(e);
        }

    }

    private static void sendTask(double x, double y) {
        var task = createTask(x, y);
        String body;

        try {
            body = (new ObjectMapper()).writeValueAsString(task);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }

        var request = HttpRequest
                .newBuilder()
                .uri(URI.create(fmsUrl + "/newTask"))
                .POST(HttpRequest.BodyPublishers.ofString(body))
                .header("Accept", "application/json")
                .build();
        try {
            client.send(request, HttpResponse.BodyHandlers.ofString());
        } catch (IOException | InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    private static InputDeliveryTask createTask(double x, double y) {
        var task = new DeliveryTask("testuser", new Position(x, y), "");
        var outTask = new InputDeliveryTask(
                signService.sign(task.toSignString()),
                task
        );
        System.out.println(outTask);
        return outTask;
    }
}
