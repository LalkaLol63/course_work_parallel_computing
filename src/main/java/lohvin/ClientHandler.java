package lohvin;

import com.google.gson.Gson;

import java.io.*;
import java.net.Socket;
import java.nio.file.Path;
import java.util.Map;
import java.util.Set;

public class ClientHandler implements Runnable {
    private final Socket clientSocket;
    private static final Gson gson = new Gson();

    public ClientHandler(Socket clientSocket) {
        this.clientSocket = clientSocket;
    }

    @Override
    public void run() {
        try (BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
             BufferedWriter out = new BufferedWriter(new OutputStreamWriter(clientSocket.getOutputStream()))) {

            HttpRequest request = HttpUtils.parseRequest(in);
            HttpResponse response;

            switch (request.getPath()) {
                case "/search":
                    response = handleSearch(request);
                    break;
                case "/document":
                    if ("POST".equalsIgnoreCase(request.getMethod())) {
                        response = handleAddDocument(request);
                    } else if ("GET".equalsIgnoreCase(request.getMethod())) {
                        response = handleGetDocument(request);
                    } else {
                        response = new HttpResponse(405, "Method Not Allowed", Map.of(), "Unsupported method.");
                    }
                    break;
                default:
                    response = new HttpResponse(404, "Not Found", Map.of(), "Endpoint not found.");
            }

            out.write(response.toString());
            out.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public HttpResponse handleSearch(HttpRequest request) {
        String query = HttpUtils.extractQueryParam(request.getPath(), "query");
        if (query == null || query.isEmpty()) {
            return errorResponse(400, "Bad Request","Missing 'query' parameter");
        }

        Set<String> documentIds = IndexService.getInstance().search(query);

        Map<String, Object> responseBody = Map.of(
                "status", "success",
                "query", query,
                "results", documentIds
        );
        return new HttpResponse(200, "OK", Map.of("Content-Type", "application/json"), gson.toJson(responseBody));
    }

    public HttpResponse handleAddDocument(HttpRequest request) {
        Map<String, String> requestBody = gson.fromJson(request.getBody(), Map.class);
        String id = requestBody.get("id");
        String text = requestBody.get("text");

        if (id == null || text == null) {
            return errorResponse(400, "Bad Request","Missing 'id' or 'text' parameter");
        }

        FileManager fileManager = FileManager.getInstance();
        Path filePath = fileManager.idToPath(id);
        if (fileManager.fileExists(filePath)) {
            return errorResponse(409, "Conflict","File already exists.");
        }

        fileManager.saveFile(filePath, text);
        IndexService.getInstance().addDocument(id, text);
        Map<String, Object> responseBody = Map.of(
                "status", "success",
                "message", "Document successfully added",
                "id", id
        );

        return new HttpResponse(201, "Created", Map.of("Content-Type", "application/json"), gson.toJson(responseBody));
    }

    public HttpResponse handleGetDocument(HttpRequest request) {
        String path = request.getPath();
        if (!path.startsWith("/document/")) {
            return errorResponse(400, "Bad Request","Invalid path format");
        }

        String id = path.substring("/document/".length());
        if (id.isEmpty()) {
            return errorResponse(400, "Bad Request", "Missing 'id' parameter");
        }

        FileManager fileManager = FileManager.getInstance();
        Path filePath = fileManager.idToPath(id);
        if (!fileManager.fileExists(filePath)) {
            return errorResponse(404, "Not Found", "File not found.");
        }

        String content = fileManager.loadFile(filePath);
        Map<String, Object> responseBody = Map.of(
                "status", "success",
                "id", id,
                "content", content
        );
        return new HttpResponse(200, "OK", Map.of("Content-Type", "application/json"), gson.toJson(responseBody));
    }

    private HttpResponse errorResponse(int statusCode, String statusMessage, String message) {
        Map<String, Object> responseBody = Map.of(
                "status", "error",
                "message", message
        );
        return new HttpResponse(
                statusCode,
                statusMessage,
                Map.of("Content-Type", "application/json"),
                gson.toJson(responseBody)
        );
    }
}
