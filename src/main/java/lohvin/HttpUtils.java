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

    public static HttpResponse parseResponse(BufferedReader in) throws IOException {
        String statusLine = in.readLine();
        if (statusLine == null || statusLine.isEmpty()) {
            throw new IOException("Empty response");
        }

        String[] statusParts = statusLine.split(" ", 3);
        int statusCode = Integer.parseInt(statusParts[1]);
        String statusMessage = statusParts[2];

        Map<String, String> headers = new HashMap<>();
        String line;
        while ((line = in.readLine()) != null && !line.isEmpty()) {
            String[] headerParts = line.split(": ", 2);
            headers.put(headerParts[0], headerParts[1]);
        }

        StringBuilder body = new StringBuilder();
        while (in.ready() && (line = in.readLine()) != null) {
            body.append(line).append("\n");
        }

        return new HttpResponse(statusCode, statusMessage, headers, body.toString().trim());
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
