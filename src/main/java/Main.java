import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Optional;

import static common.СonfigConstants.*;

public class Main {
    public static void main(String[] args) throws IOException {
        Server server = new Server();

        server.addHandler("GET", "/messages", (request, out) -> {
            Optional<String> lastParam = request.getQueryParam("last");
            String responseBody = lastParam
                    .map(s -> "Last " + s + " messages")
                    .orElse("All messages");

            String response = server.buildResponse(
                    OK_STATUS,
                    TEXT_PLAIN,
                    responseBody);
            out.write(response.getBytes());
        });

        server.addHandler("POST", "/messages", (request, out) -> {
            String body = new String(request.getBody().readAllBytes(), StandardCharsets.UTF_8);
            String response = server.buildResponse(
                    OK_STATUS,
                    TEXT_PLAIN,
                    "Received: " + body);
            out.write(response.getBytes());
        });

        server.listen(DEFAULT_PORT);
    }
}

