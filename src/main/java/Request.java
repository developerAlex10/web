import lombok.Builder;
import lombok.Getter;
import lombok.ToString;
import org.apache.http.NameValuePair;
import org.apache.http.client.utils.URLEncodedUtils;

import java.io.InputStream;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static common.СonfigConstants.QUERY_SEPARATOR;
import static org.apache.http.Consts.UTF_8;

@Getter
@Builder
@ToString
public class Request {
    private final String method;
    private final String path;
    private final Map<String, String> headers;
    private final InputStream body;
    private final List<NameValuePair> queryParams;

    public static List<NameValuePair> parseQuery(String path) {
        if (!path.contains(QUERY_SEPARATOR)) {
            return Collections.emptyList();
        }
        String query = path.substring(path.indexOf(QUERY_SEPARATOR) + 1);
        return URLEncodedUtils.parse(query, UTF_8);
    }

    public String getPathWithoutQuery() {
        int queryIndex = path.indexOf(QUERY_SEPARATOR);
        return queryIndex == -1 ? path : path.substring(0, queryIndex);
    }

    public Optional<String> getQueryParam(String name) {
        return queryParams.stream()
                .filter(pair -> pair.getName().equalsIgnoreCase(name))
                .map(NameValuePair::getValue)
                .findFirst();
    }
}
