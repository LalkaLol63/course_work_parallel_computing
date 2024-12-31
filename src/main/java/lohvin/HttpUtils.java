package lohvin;

import java.io.BufferedReader;
import java.io.IOException;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

public class HttpUtils {
    public static HttpRequest parseRequest(BufferedReader in) throws IOException {
        String requestLine = in.readLine();
        if (requestLine == null || requestLine.isEmpty()) {
            throw new IOException("Empty request");
        }

        String[] parts = requestLine.split(" ");
        String method = parts[0];
        String path = URLDecoder.decode(parts[1], StandardCharsets.UTF_8);

        Map<String, String> headers = parseHeaders(in);
        String body = parseBody(in, headers);

        return new HttpRequest(method, path, headers, body);
    }

    public static HttpResponse parseResponse(BufferedReader in) throws IOException {
        String statusLine = in.readLine();
        if (statusLine == null || statusLine.isEmpty()) {
            throw new IOException("Empty response");
        }

        String[] statusParts = statusLine.split(" ", 3);
        int statusCode = Integer.parseInt(statusParts[1]);
        String statusMessage = statusParts[2];

        Map<String, String> headers = parseHeaders(in);
        String body = parseBody(in, headers);

        return new HttpResponse(statusCode, statusMessage, headers, body);
    }

    private static Map<String, String> parseHeaders(BufferedReader in) throws IOException {
        Map<String, String> headers = new HashMap<>();
        String line;
        while ((line = in.readLine()) != null && !line.isEmpty()) {
            String[] headerParts = line.split(": ", 2);
            headers.put(headerParts[0], headerParts[1]);
        }
        return headers;
    }

    private static String parseBody(BufferedReader in, Map<String, String> headers) throws IOException {
        if (!headers.containsKey("Content-Length")) {
            return null;
        }

        int contentLength = Integer.parseInt(headers.get("Content-Length"));
        char[] bodyChars = new char[contentLength];
        int read = in.read(bodyChars, 0, contentLength);

        if (read != contentLength) {
            throw new IOException("Failed to read full body");
        }

        return new String(bodyChars);
    }

    public static String extractQueryParam(String path, String paramName) {
        if (!path.contains("?")) {
            return null;
        }
        String queryPart = path.substring(path.indexOf("?") + 1);
        String[] params = queryPart.split("&");

        for (String param : params) {
            String[] keyValue = param.split("=");
            if (keyValue.length == 2 && keyValue[0].equals(paramName)) {
                return keyValue[1];
            }
        }
        return null;
    }
}
