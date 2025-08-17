import java.util.List;

public class Main {
    private static final int PORT = 9999;
    private static final int THREAD_POOL_SIZE = 64;
    private static final List<String> VALID_PATHS = List.of(
            "/index.html", "/spring.svg", "/spring.png", "/resources.html",
            "/styles.css", "/app.js", "/links.html", "/forms.html",
            "/classic.html", "/events.html", "/events.js"
    );

    public static void main(String[] args) {
        Server server = new Server(PORT, THREAD_POOL_SIZE, VALID_PATHS);
        server.start();
    }
}
