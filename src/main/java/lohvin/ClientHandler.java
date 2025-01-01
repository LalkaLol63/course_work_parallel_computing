package lohvin;

import com.google.gson.Gson;

import java.io.*;
import java.net.Socket;
import java.nio.file.Path;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class ClientHandler implements Runnable {
    private final Socket clientSocket;
    private static final Gson gson = new Gson();
    private static final ReadWriteLock fileLock = new ReentrantReadWriteLock();

    public ClientHandler(Socket clientSocket) {
        this.clientSocket = clientSocket;
    }

    @Override
    public void run() {
        try (BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
             BufferedWriter out = new BufferedWriter(new OutputStreamWriter(clientSocket.getOutputStream()))) {
            HttpRequest request = HttpUtils.parseRequest(in);
            HttpResponse response;
            if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
                response = new HttpResponse(204, "No content", Map.of(), "");
            } else if (request.getPath().startsWith("/search")) {
                response = handleSearch(request);
            } else if (request.getPath().startsWith("/documents")) {
                if ("POST".equalsIgnoreCase(request.getMethod())) {
                    response = handleAddDocument(request);
                } else if ("GET".equalsIgnoreCase(request.getMethod())) {
                    response = handleGetDocument(request);
                } else {
                    response = new HttpResponse(405, "Method Not Allowed", Map.of(), "Unsupported method.");
                }
            } else {
                response = new HttpResponse(404, "Not Found", Map.of(), "Endpoint not found.");
            }

            response.addCorsHeaders();
            out.write(response.toString());
            out.flush();
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("Error handling client request.");
        } finally {
            try {
                clientSocket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public HttpResponse handleSearch(HttpRequest request) {
        String query = HttpUtils.extractQueryParam(request.getPath(), "query");
        if (query == null || query.isEmpty()) {
            return errorResponse(400, "Bad Request", "Missing 'query' parameter");
        }

        Set<String> documentIds = IndexService.getInstance().search(query);

        String message = !documentIds.isEmpty() ? "The documents were found." : "Nothing was found.";
        Map<String, Object> responseBody = Map.of(
                "message", message,
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
            return errorResponse(400, "Bad Request", "Missing 'id' or 'text' parameter");
        }

        FileManager fileManager = FileManager.getInstance();
        Path filePath = fileManager.idToPath(id);

        fileLock.writeLock().lock();
        try {
            if (fileManager.fileExists(filePath)) {
                return errorResponse(409, "Conflict", "File already exists.");
            }
            fileManager.saveFile(filePath, text);
        } finally {
            fileLock.writeLock().unlock();
        }

        Map<String, Object> responseBody = Map.of(
                "message", "Document successfully added",
                "id", id
        );

        return new HttpResponse(201, "Created", Map.of("Content-Type", "application/json"), gson.toJson(responseBody));
    }

    public HttpResponse handleGetDocument(HttpRequest request) {
        String path = request.getPath();
        if (!path.startsWith("/documents/")) {
            return errorResponse(400, "Bad Request", "Invalid path format");
        }

        String id = path.substring("/documents/".length());
        if (id.isEmpty()) {
            return errorResponse(400, "Bad Request", "Missing 'id' parameter");
        }

        FileManager fileManager = FileManager.getInstance();
        Path filePath = fileManager.idToPath(id);
        fileLock.readLock().lock();

        try {
            if (!fileManager.fileExists(filePath)) {
                return errorResponse(404, "Not Found", "File not found.");
            }
            String content = fileManager.loadFile(filePath);
            Map<String, Object> responseBody = Map.of(
                    "id", id,
                    "content", content,
                    "message", "Document found."
            );
            return new HttpResponse(200, "OK", Map.of("Content-Type", "application/json"), gson.toJson(responseBody));
        } finally {
            fileLock.readLock().unlock();
        }
    }

    private HttpResponse errorResponse(int statusCode, String statusMessage, String message) {
        Map<String, Object> responseBody = Map.of(
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
