package lohvin;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.stream.Stream;

public class FileManager {
    private static volatile FileManager instance;
    private final Path baseDirectory;

    private FileManager(Path baseDirectory) {
        if (!Files.exists(baseDirectory) || !Files.isDirectory(baseDirectory)) {
            throw new IllegalArgumentException("Вказана базова директорія не дійсна: " + baseDirectory);
        }
        this.baseDirectory = baseDirectory;
    }

    public synchronized static FileManager getInstance(Path baseDirectory) {
        if (instance == null) {
            instance = new FileManager(baseDirectory);
        }
        return instance;
    }

    public static FileManager getInstance() {
        if (instance == null) {
            throw new IllegalStateException("FileManager ще не ініціалізований." );
        }
        return instance;
    }

    public Path idToPath(String id) {
        return baseDirectory.resolve(id.replace("-", File.separator) + ".txt");
    }

    public String pathToId(Path path) {
        return baseDirectory.relativize(path).toString().replace(File.separator, "-").replace(".txt", "");
    }

    public boolean fileExists(Path relativePath) {
        Path path = baseDirectory.resolve(relativePath);
        return Files.exists(path);
    }


    public String loadFile(Path relativePath) {
        Path path = baseDirectory.resolve(relativePath);
        try {
            return Files.readString(path);
        } catch (IOException e) {
            System.out.println("Помилка при читанні файлу: " + path);
            e.printStackTrace();
            return "";
        }
    }

    public void saveFile(Path relativePath, String content) {
        Path path = baseDirectory.resolve(relativePath);
        try {
            Files.createDirectories(path.getParent());
            Files.writeString(path, content);
        } catch (IOException e) {
            System.out.println("Помилка при записі файлу: " + path);
            e.printStackTrace();
        }
    }

    public List<Path> getAllFiles() {
        try (Stream<Path> stream = Files.walk(baseDirectory)) {
            return stream.filter(Files::isRegularFile)
                    .toList();
        } catch (IOException e) {
            System.out.println("Помилка при читанні всіх файлів з директорії: " + baseDirectory);
            e.printStackTrace();
            return List.of();
        }
    }

    public Path getBaseDirectory() {
        return baseDirectory;
    }
}
