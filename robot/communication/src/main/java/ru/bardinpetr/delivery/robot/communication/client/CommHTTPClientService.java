package ru.bardinpetr.delivery.robot.communication.client;

import lombok.extern.slf4j.Slf4j;
import ru.bardinpetr.delivery.libs.messages.kafka.serializers.MonitoredNonBusSerializer;
import ru.bardinpetr.delivery.libs.messages.msg.MessageRequest;

import java.io.IOException;
import java.net.ConnectException;
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
        return send(baseUrl, messageRequest);
    }

    public boolean send(String url, MessageRequest messageRequest) {
        var body = serializer.serialize(messageRequest);

        log.debug("Sending HTTP {}/msg for {}", url, messageRequest.getActionType());

        HttpRequest request;
        try {
            request = HttpRequest
                    .newBuilder()
                    .uri(URI.create("http://%s/msg".formatted(url)))
                    .POST(HttpRequest.BodyPublishers.ofByteArray(body))
                    .header("Content-type", "application/json")
                    .build();
        } catch (IllegalArgumentException e) {
            log.error("Got invalid message request config", e);
            return false;
        }

        try {
            var res = client.send(request, HttpResponse.BodyHandlers.ofString());
            log.debug("Got HTTP reply from counterpart: {}", res.body());
        } catch (ConnectException ignored) {
            log.error("Could not connect to counterpart comm service");
            return false;
        } catch (IOException | InterruptedException e) {
            log.error("HTTP request failed", e);
            return false;
        }
        return true;
    }
}
