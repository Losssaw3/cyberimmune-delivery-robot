package ru.bardinpetr.delivery.backend.store;

import com.fasterxml.jackson.databind.ObjectMapper;
import ru.bardinpetr.delivery.libs.crypto.SignatureCryptoService;
import ru.bardinpetr.delivery.libs.crypto.keystore.KeystoreServiceSign;
import ru.bardinpetr.delivery.libs.messages.msg.ccu.DeliveryTask;
import ru.bardinpetr.delivery.libs.messages.msg.ccu.InputDeliveryTask;
import ru.bardinpetr.delivery.libs.messages.msg.location.Position;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Properties;

/**
 * This is just a demonstration of how a real web market should trigger delivery
 */

public class Main {

    public static void main(String[] args) throws IOException {
        var props = new Properties();
        try (var is = Main.class.getClassLoader().getResourceAsStream("config.properties")) {
            props.load(is);
        }
        var fmsUrl = props.getProperty("fms_url", "http://localhost:9040");
        var client = HttpClient.newHttpClient();

        HttpRequest request = HttpRequest
                .newBuilder()
                .uri(URI.create("%s/newRobot?url=%s".formatted(
                        fmsUrl,
                        props.getProperty("robot_url", "localhost:9011")
                )))
                .GET()
                .build();
        try {
            client.send(request, HttpResponse.BodyHandlers.ofString());
        } catch (IOException | InterruptedException e) {
            throw new RuntimeException(e);
        }

        var task = getTask(props);
        var request2 = HttpRequest
                .newBuilder()
                .uri(URI.create(fmsUrl + "/newTask"))
                .POST(
                        HttpRequest.BodyPublishers.ofString(
                                (new ObjectMapper()).writeValueAsString(task)
                        )
                )
                .header("Accept", "application/json")
                .build();
        try {
            client.send(request2, HttpResponse.BodyHandlers.ofString());
        } catch (IOException | InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    private static InputDeliveryTask getTask(Properties props) {
        var sign = new SignatureCryptoService(
                (new KeystoreServiceSign()).getPrivateFromKeystore(
                        props.getProperty("keystore_path"),
                        props.getProperty("keystore_pass")
                )
        );

        var task = new DeliveryTask("user1", new Position(50, 100), "");
        var outTask = new InputDeliveryTask(
                sign.sign(task.toSignString()),
                task
        );
        System.out.println(outTask);
        return outTask;
    }
}
