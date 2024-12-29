package lohvin;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

public class IndexUpdater extends Thread {
    private long lastCheckTime = System.currentTimeMillis();
    private final int sleepIntervalSeconds;
    private final FileManager fileManager;
    private final IndexService indexService;

    public IndexUpdater(int sleepIntervalSeconds) {
        this.sleepIntervalSeconds = sleepIntervalSeconds;
        fileManager = FileManager.getInstance();
        indexService = IndexService.getInstance();
    }

    @Override
    public void run() {
        try {
            while (!Thread.currentThread().isInterrupted()) {
                updateIndex();
                Thread.sleep(sleepIntervalSeconds * 1000L);
            }
        } catch (InterruptedException e) {
            System.out.println("Потік IndexUpdater зупинено");
        }
    }

    private void updateIndex() {
        long newLastCheckTime = System.currentTimeMillis();
        List<Path> allFiles = fileManager.getAllFiles();
        Path baseDirectory = fileManager.getBaseDirectory();
        Path filePath = null;
        for (Path file : allFiles) {
            try {
                filePath = baseDirectory.resolve(file);
                long currentModifiedTime = Files.getLastModifiedTime(filePath).toMillis();

                if (currentModifiedTime > lastCheckTime) {
                    String content = fileManager.loadFile(filePath);
                    indexService.addDocument(fileManager.pathToId(filePath), content);
                }
            } catch (IOException e) {
                System.err.println("Помилка обробки файлу: " + filePath);
            }
        }

        lastCheckTime = newLastCheckTime;
    }

}
