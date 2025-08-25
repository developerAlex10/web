import org.apache.http.NameValuePair;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static common.СonfigConstants.*;

public class Server {
    private final Map<String, Map<String, Handler>> handlers = new ConcurrentHashMap<>();
    private final ExecutorService threadPool;

    public Server() {
        this(THREAD_POOL_SIZE);
    }

    public Server(int threadPoolSize) {
        this.threadPool = Executors.newFixedThreadPool(threadPoolSize);
    }

    public void addHandler(String method, String path, Handler handler) {
        handlers.computeIfAbsent(method.toUpperCase(), k -> new ConcurrentHashMap<>())
                .put(path, handler);
    }

    public void listen(int port) throws IOException {
        try (ServerSocket serverSocket = new ServerSocket(port)) {
            while (true) {
                Socket socket = serverSocket.accept();
                threadPool.submit(() -> handleConnection(socket));
            }
        } finally {
            threadPool.shutdown();
        }
    }

    private void handleConnection(Socket socket) {
        try (
                socket;
                BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                BufferedOutputStream out = new BufferedOutputStream(socket.getOutputStream())
        ) {
            Request request = parseRequest(in);
            if (request == null) return;

            Handler handler = findHandler(request.getMethod(), request.getPathWithoutQuery());
            if (handler != null) {
                handler.handle(request, out);
            } else {
                sendNotFound(out);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private Request parseRequest(BufferedReader in) throws IOException {
        String requestLine = in.readLine();
        String[] requestParts = requestLine.split(" ");
        if (requestParts.length != 3) return null;

        String method = requestParts[0];
        String path = requestParts[1];
        List<NameValuePair> queryParams = Request.parseQuery(path);

        Map<String, String> headers = new HashMap<>();
        String headerLine;
        while (!(headerLine = in.readLine()).isEmpty()) {
            int separator = headerLine.indexOf(HEADER_SEPARATOR);
            if (separator > 0) {
                String key = headerLine.substring(0, separator).trim();
                String value = headerLine.substring(separator + HEADER_SEPARATOR.length()).trim();
                headers.put(key, value);
            }
        }

        InputStream bodyStream = InputStream.nullInputStream();
        if (headers.containsKey(CONTENT_LENGTH_HEADER)) {
            int contentLength = Integer.parseInt(headers.get(CONTENT_LENGTH_HEADER));
            if (contentLength > 0) {
                bodyStream = new ByteArrayInputStream(in.readLine().getBytes(StandardCharsets.UTF_8));
            }
        }

        return Request.builder()
                .method(method)
                .path(path)
                .headers(headers)
                .body(bodyStream)
                .queryParams(queryParams)
                .build();
    }

    private Handler findHandler(String method, String path) {
        Map<String, Handler> methodHandlers = handlers.get(method.toUpperCase());
        return methodHandlers.get(path);
    }

    private void sendNotFound(BufferedOutputStream out) throws IOException {
        String response = buildResponse(NOT_FOUND_STATUS, "", "");
        out.write(response.getBytes(StandardCharsets.UTF_8));
        out.flush();
    }

    protected String buildResponse(int statusCode, String contentType, String body) {
        return String.join(LINE_SEPARATOR,
                HTTP_VERSION + " " + statusCode + " " + getStatusMessage(statusCode),
                CONTENT_TYPE_HEADER + HEADER_SEPARATOR + contentType,
                CONTENT_LENGTH_HEADER + HEADER_SEPARATOR + body.length(),
                CONNECTION_HEADER + HEADER_SEPARATOR + CLOSE_CONNECTION,
                "",
                body);
    }

    private String getStatusMessage(int statusCode) {
        return statusCode == OK_STATUS ? "OK" : "Not Found";
    }
}
