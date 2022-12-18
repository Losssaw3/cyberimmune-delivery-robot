package ru.bardinpetr.delivery.robot.communication.client;

import lombok.extern.slf4j.Slf4j;
import ru.bardinpetr.delivery.libs.messages.kafka.serializers.MonitoredNonBusSerializer;
import ru.bardinpetr.delivery.libs.messages.msg.MessageRequest;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

@Slf4j
public class CommHTTPClientService {

    private final HttpClient client;
    private final String baseUrl;

    private final MonitoredNonBusSerializer serializer;

    public CommHTTPClientService(String url) {
        serializer = new MonitoredNonBusSerializer();
        client = HttpClient.newHttpClient();
        baseUrl = url;
    }

    public boolean send(MessageRequest messageRequest) {
        var body = serializer.serialize(messageRequest);

        var request = HttpRequest
                .newBuilder()
                .uri(URI.create("%s/msg".formatted(baseUrl)))
                .POST(HttpRequest.BodyPublishers.ofByteArray(body))
                .header("Content-type", "application/json")
                .build();

        try {
            var res = client.send(request, HttpResponse.BodyHandlers.ofString());
            log.debug("Got reply from counterpart: {}", res.body());
        } catch (IOException | InterruptedException e) {
            log.error("Could not send HTTP request", e);
            return false;
        }
        return true;
    }
}
