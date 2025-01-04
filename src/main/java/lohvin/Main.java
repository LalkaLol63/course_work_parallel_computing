package lohvin;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);

        String baseDirectory;
        while (true) {
            System.out.println("Введіть базову директорію (обов'язково): ");
            baseDirectory = scanner.nextLine().trim();
            if (!baseDirectory.isEmpty()) {
                break;
            }
            System.out.println("Базова директорія не може бути порожньою. Спробуйте ще раз.");
        }

        System.out.println("Введіть кількість потоків для побудови індексу (за замовчуванням: 8):");
        String numFillerThreadsInput = scanner.nextLine().trim();
        int numFillerThreads = numFillerThreadsInput.isEmpty() ? 8 : Integer.parseInt(numFillerThreadsInput);

        System.out.println("Введіть кількість робочих потоків у тред пулі серверу(за замовчуванням: 8):");
        String numWorkerThreadsInput = scanner.nextLine().trim();
        int numWorkerThreads = numWorkerThreadsInput.isEmpty() ? 30 : Integer.parseInt(numWorkerThreadsInput);

        System.out.println("Введите частоту оновлення (в секундах, за замовчуванням: 30):");
        String updateIntervalInput = scanner.nextLine().trim();
        int updateInterval = updateIntervalInput.isEmpty() ? 30 : Integer.parseInt(updateIntervalInput);

        FileManager.getInstance(Path.of(baseDirectory));

        InvertedIndex invertedIndex = new InvertedIndex();
        IndexService.getInstance(invertedIndex);

        IndexBuilder indexBuilder = new IndexBuilder(numFillerThreads);
        indexBuilder.buildIndex();
        System.out.println("Індекс побудовано. Час побудови: " + indexBuilder.getBuildingTime() / 1000000.0 + "ms");

        Server server = new Server( numWorkerThreads);
        IndexUpdater indexUpdater = new IndexUpdater(updateInterval);

        System.out.println("Натисніть будь-яку клавішу для запуску серверу...");
        scanner.nextLine();
        System.out.println("Запуск сервера...");

        server.start();
        indexUpdater.start();

        System.out.println("Введіть \"stop\" для зупинки серверу");
        while (true) {
            String input = scanner.nextLine();
            if ("stop".equalsIgnoreCase(input)) {
                server.stopServer();
                indexUpdater.interrupt();
                try {
                    server.join();
                    indexUpdater.join();
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
                break;
            }
        }

    }
}