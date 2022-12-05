package ru.bardinpetr.delivery.backend.store;

import com.fasterxml.jackson.databind.ObjectMapper;
import ru.bardinpetr.delivery.libs.crypto.SignatureCryptoService;
import ru.bardinpetr.delivery.libs.crypto.keystore.KeystoreServiceSign;

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
        var sign = new SignatureCryptoService();


        var task = new Task("1", "1", "");
        task.setAddressSignature(sign.sign(key, task.getAddress()));

        String data = (new ObjectMapper()).writeValueAsString(task);

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
