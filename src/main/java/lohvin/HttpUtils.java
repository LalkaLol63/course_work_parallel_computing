package lohvin;

import java.io.BufferedReader;
import java.io.IOException;
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
        String path = parts[1];
        Map<String, String> headers = new HashMap<>();
        String body = null;

        String line;
        while ((line = in.readLine()) != null && !line.isEmpty()) {
            String[] headerParts = line.split(": ", 2);
            headers.put(headerParts[0], headerParts[1]);
        }

        if ("POST".equals(method)) {
            StringBuilder bodyBuilder = new StringBuilder();
            while (in.ready() && (line = in.readLine()) != null) {
                bodyBuilder.append(line).append("\n");
            }
            body = bodyBuilder.toString().trim();
        }

        return new HttpRequest(method, path, headers, body);
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
