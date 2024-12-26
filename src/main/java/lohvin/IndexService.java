package lohvin;

import java.nio.file.Path;
import java.util.*;

public class IndexService {
    private final InvertedIndex index;
    private static volatile IndexService instance;

    private IndexService(InvertedIndex index) {
        this.index = index;
    }

    public synchronized static IndexService getInstance(InvertedIndex index) {
        if (instance == null) {
            instance = new IndexService(index);
        }
        return instance;
    }

    public static IndexService getInstance() {
        if (instance == null) {
            throw new IllegalStateException("IndexService ще не ініціалізований." );
        }
        return instance;
    }

    public void addDocument(String id, String text) {
        List<String> tokens = TextProcessor.preprocess(text);
        Map<String, List<Integer>> wordPositions = new HashMap<>();

        for (int i = 0; i < tokens.size(); i++) {
            String word = tokens.get(i);
            wordPositions
                    .computeIfAbsent(word, k -> new ArrayList<>())
                    .add(i);
        }

        for (Map.Entry<String, List<Integer>> entry : wordPositions.entrySet()) {
            String word = entry.getKey();
            int[] positions = entry.getValue().stream().mapToInt(Integer::intValue).toArray();
            DocWordPositions docWordPositions = new DocWordPositions(positions, id);

            index.put(word, docWordPositions);
        }
    }

    public Set<DocWordPositions> search(String query) {
        String[] words = query.split(" ");
        Set<DocWordPositions> files = index.get(words[0]);
        if(files == null) {
            return null;
        }
        Set<DocWordPositions> result = new HashSet<>(files);
        for (int i = 1; i < words.length; i++) {
            files = index.get(words[i]);
            if(files == null) {
                return null;
            }
            result.retainAll(files);
        }
        return result;
    }
}
