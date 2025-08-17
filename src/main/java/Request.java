import lombok.Builder;
import lombok.Getter;
import lombok.ToString;

import java.io.InputStream;
import java.util.Map;

@Getter
@Builder
@ToString
public class Request {
    private final String method;
    private final String path;
    private final Map<String, String> headers;
    private final InputStream body;
}
