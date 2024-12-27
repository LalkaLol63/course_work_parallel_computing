package lohvin;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class IndexFiller extends Thread {
    private IndexService indexService;
    private List<Path> files;
    private int threadId;
    private int threadNum;
    private FileManager fileManager;

    public IndexFiller(List<Path> files, int threadId, int threadNum) {
        this.files = files;
        this.threadId = threadId;
        this.threadNum = threadNum;
        indexService = IndexService.getInstance();
        fileManager = FileManager.getInstance();
    }

    @Override
    public void run() {
        int size = files.size();
        for(int i = threadId; i < size; i += threadNum) {
            Path filePath = files.get(i);
            String content = fileManager.loadFile(files.get(i));
            indexService.addDocument(filePath.toString(), content);
        }
    }
}
