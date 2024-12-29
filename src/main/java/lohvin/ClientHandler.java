package lohvin;

import com.google.gson.Gson;

import java.io.*;
import java.net.Socket;
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
            return new HttpResponse(400, "Bad Request", Map.of(), "Missing 'query' parameter");
        }

        Set<String> documentIds = IndexService.getInstance().search(query);

        String responseBody = gson.toJson(documentIds);
        return new HttpResponse(200, "OK", Map.of("Content-Type", "application/json"), responseBody);
    }

    public HttpResponse handleAddDocument(HttpRequest request) {
        return null;
    }

    public HttpResponse handleGetDocument(HttpRequest request) {
        return null;
    }
}
