package ru.bardinpetr.delivery.backend.store;

import com.fasterxml.jackson.databind.ObjectMapper;
import ru.bardinpetr.delivery.libs.crypto.AESCryptoService;
import ru.bardinpetr.delivery.libs.crypto.SignatureCryptoService;
import ru.bardinpetr.delivery.libs.crypto.keystore.KeystoreServicePin;
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

        var keystoreSign = new KeystoreServiceSign();
        var key = keystoreSign.getPrivateFromKeystore(
                props.getProperty("keystore_path"),
                props.getProperty("keystore_pass")
        );
        var sign = new SignatureCryptoService(key);

        var keystorePin = new KeystoreServicePin();
        var keyPin = keystorePin.getFromKeystore(
                props.getProperty("keystore_pin_path"),
                props.getProperty("keystore_pin_pass")
        );
        var crypt = new AESCryptoService(keyPin);

        var task = new DeliveryTask(
                new Position(100, 200),
                crypt.encrypt("123456")
        );
        var outTask = new InputDeliveryTask(
                sign.sign(task.toSignString()),
                task
        );
        System.out.println(outTask);

        String data = (new ObjectMapper()).writeValueAsString(outTask);

        var client = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest
                .newBuilder()
                .uri(URI.create(props.getProperty("fms_url", "http://localhost:5000") + "/newTask"))
                .POST(HttpRequest.BodyPublishers.ofString(data))
                .header("Accept", "application/json")
                .build();

        try {
            client.send(request, HttpResponse.BodyHandlers.ofString());
        } catch (IOException | InterruptedException e) {
            throw new RuntimeException(e);
        }
    }
}
