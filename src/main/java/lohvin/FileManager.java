package lohvin;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.stream.Stream;

public class FileManager {
    private final Path baseDirectory;

    public FileManager(Path baseDirectory) {
        if (!Files.exists(baseDirectory) || !Files.isDirectory(baseDirectory)) {
            throw new IllegalArgumentException("Вказана базова директорія не дійсна: " + baseDirectory);
        }
        this.baseDirectory = baseDirectory;
    }

    public String loadFile(String relativePath) {
        Path path = baseDirectory.resolve(relativePath);
        try {
            return Files.readString(path);
        } catch (IOException e) {
            throw new RuntimeException("Помилка при читанні файлу: " + path, e);
        }
    }

    public void saveFile(String relativePath, String content) {
        Path path = baseDirectory.resolve(relativePath);
        try {
            Files.writeString(path, content);
        } catch (IOException e) {
            throw new RuntimeException("Помилка при записі файлу: " + path, e);
        }
    }

    public List<Path> getAllFiles() throws IOException {
        try (Stream<Path> stream = Files.walk(baseDirectory)) {
            return stream.filter(Files::isRegularFile)
                    .toList();
        }
    }
}
