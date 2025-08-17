import java.nio.charset.StandardCharsets;

import static common.СonfigConstants.*;

public class Main {
    public static void main(String[] args) {
        final var server = new Server();

        server.addHandler("GET", "/messages", (request, out) -> {
            String responseBody = "GET messages";
            String response = server.buildResponse(OK_STATUS, TEXT_PLAIN, responseBody);
            out.write(response.getBytes(StandardCharsets.UTF_8));
            out.flush();
        });

        server.addHandler("POST", "/messages", (request, out) -> {
            String body = new String(request.getBody().readAllBytes(), StandardCharsets.UTF_8);
            String responseBody = "POST messages: " + body;
            String response = server.buildResponse(
                    OK_STATUS,
                    TEXT_PLAIN,
                    responseBody);
            out.write(response.getBytes(StandardCharsets.UTF_8));
            out.flush();
        });

        server.listen(DEFAULT_PORT);
    }
}
