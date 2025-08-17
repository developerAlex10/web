import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;


public class Server {
    private final int port;
    private final int threadPoolSize;
    private final List<String> validPaths;
    private final ExecutorService threadPool;

    public Server(int port, int threadPoolSize, List<String> validPaths) {
        this.port = port;
        this.threadPoolSize = threadPoolSize;
        this.validPaths = validPaths;
        this.threadPool = Executors.newFixedThreadPool(threadPoolSize);
    }

    public void start() {
        try (final var serverSocket = new ServerSocket(port)) {
            while (true) {
                try {
                    final var socket = serverSocket.accept();
                    threadPool.submit(() -> handleConnection(socket));
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            threadPool.shutdown();
        }
    }

    private void handleConnection(Socket socket) {
        try (
                socket;
                final var in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                final var out = new BufferedOutputStream(socket.getOutputStream())
        ) {
            final var requestLine = in.readLine();
            final var parts = requestLine.split(" ");
            if (parts.length != 3) {
                return;
            }

            final var path = parts[1];
            if (!validPaths.contains(path)) {
                sendNotFoundResponse(out);
                return;
            }

            final var filePath = Path.of(".", "public", path);
            if (path.equals("/classic.html")) {
                handleClassicHtml(filePath, out);
            } else {
                handleRegularFile(filePath, out);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void handleClassicHtml(Path filePath, BufferedOutputStream out) throws IOException {
        final var mimeType = Files.probeContentType(filePath);
        final var template = Files.readString(filePath);
        final var content = template.replace(
                "{time}",
                LocalDateTime.now().toString()
        ).getBytes();

        sendOkResponse(out, mimeType, content.length);
        out.write(content);
        out.flush();
    }

    private void handleRegularFile(Path filePath, BufferedOutputStream out) throws IOException {
        final var mimeType = Files.probeContentType(filePath);
        final var length = Files.size(filePath);

        sendOkResponse(out, mimeType, length);
        Files.copy(filePath, out);
        out.flush();
    }

    private void sendNotFoundResponse(BufferedOutputStream out) throws IOException {
        out.write((
                "HTTP/1.1 404 Not Found\r\n" +
                        "Content-Length: 0\r\n" +
                        "Connection: close\r\n" +
                        "\r\n"
        ).getBytes());
        out.flush();
    }

    private void sendOkResponse(BufferedOutputStream out, String mimeType, long contentLength) throws IOException {
        out.write((
                "HTTP/1.1 200 OK\r\n" +
                        "Content-Type: " + mimeType + "\r\n" +
                        "Content-Length: " + contentLength + "\r\n" +
                        "Connection: close\r\n" +
                        "\r\n"
        ).getBytes());
    }
}
