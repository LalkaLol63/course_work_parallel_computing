package lohvin;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public class IndexBuilder {
    private int numThreads;
    private long buildingTime;

    public IndexBuilder(int numThreads) {
        this.numThreads = numThreads;
    }

    public void buildIndex() {
        ArrayList<Thread> threads = new ArrayList<>();
        List<Path> files = FileManager.getInstance().getAllFiles();
        long start = System.nanoTime();
        for (int i = 0; i < numThreads; i++) {
            threads.add(new IndexFiller(files, i, numThreads));
        }
        for (Thread thread : threads) {
            thread.start();
        }
        for (Thread thread : threads) {
            try {
                thread.join();
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
        long end = System.nanoTime();
        buildingTime = end - start;
    }

    public long getBuildingTime() {
        return buildingTime;
    }
}
