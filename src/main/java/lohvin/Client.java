package lohvin;

import com.google.gson.Gson;

import java.io.*;
import java.net.Socket;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;

public class Client {
    private static final String SERVER_HOST = "localhost";
    private static final int SERVER_PORT = 80;
    private static final Gson gson = new Gson();

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        while (true) {
            try (Socket socket = new Socket(SERVER_HOST, SERVER_PORT);
                 BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                 BufferedWriter out = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
            ) {
                System.out.println("Виберіть дію:");
                System.out.println("1. Додати документ");
                System.out.println("2. Отримати документ");
                System.out.println("3. Пошук");
                System.out.println("4. Вихід");

                int choice = Integer.parseInt(scanner.nextLine());
                if (choice == 4) break;

                HttpRequest request;
                switch (choice) {
                    case 1 -> {
                        System.out.println("Введіть ID документа:");
                        String id = scanner.nextLine();
                        System.out.println("Введіть текст документа:");
                        String text = scanner.nextLine();

                        Map<String, String> body = Map.of("id", id, "text", text);
                        request = new HttpRequest("POST", "/document", Map.of("Content-Type", "application/json"), gson.toJson(body));
                    }
                    case 2 -> {
                        System.out.println("Введіть ID документа:");
                        String id = scanner.nextLine();
                        request = new HttpRequest("GET", "/document/" + id, Map.of(), null);
                    }
                    case 3 -> {
                        System.out.println("Введіть запит для пошуку:");
                        String query = URLEncoder.encode(scanner.nextLine(), StandardCharsets.UTF_8);
                        request = new HttpRequest("GET", "/search?query=" + query, Map.of(), null);
                    }
                    default -> {
                        System.out.println("Неправильний вибір. Спробуйте знову");
                        continue;
                    }
                }

                out.write(request.toString());
                out.flush();

                HttpResponse response = HttpUtils.parseResponse(in);
                System.out.println("Відовідь сервера:");
                System.out.println("Код статусу: " + response.getStatusCode());
                System.out.println("Повідомлення: " + response.getStatusMessage());
                System.out.println("Тіло: " + response.getBody());
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
