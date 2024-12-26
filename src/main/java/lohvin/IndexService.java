package lohvin;

import java.util.*;

public class IndexService {
    private  InvertedIndex index;

    public IndexService(InvertedIndex index) {
        this.index = index;
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
