package lohvin;

import java.nio.file.Path;
import java.util.*;
import java.util.stream.Collectors;

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

    public Set<String> search(String query) {
        String[] words = query.split(" ");
        if (words.length == 0) {
            return Collections.emptySet();
        }

        Set<DocWordPositions> currentDocs = index.get(words[0]);
        if (currentDocs == null) {
            return Collections.emptySet();
        }

        for (int i = 1; i < words.length; i++) {
            String word = words[i];
            Set<DocWordPositions> nextDocs = index.get(word);
            if (nextDocs == null) {
                return Collections.emptySet();
            }

            Set<DocWordPositions> matchedDocs = new HashSet<>();

            for (DocWordPositions currentDoc : currentDocs) {
                for (DocWordPositions nextDoc : nextDocs) {
                    if (currentDoc.getId().equals(nextDoc.getId())) {
                        int[] filteredPositions = filterPositions(
                                currentDoc.getPositions(),
                                nextDoc.getPositions()
                        );
                        if (filteredPositions.length > 0) {
                            matchedDocs.add(new DocWordPositions(filteredPositions, nextDoc.getId()));
                        }
                    }
                }
            }

            if (matchedDocs.isEmpty()) {
                return Collections.emptySet();
            }

            currentDocs = matchedDocs;
        }

        return currentDocs.stream()
                .map(DocWordPositions::getId)
                .collect(Collectors.toSet());
    }

    private int[] filterPositions(int[] positions1, int[] positions2) {
        List<Integer> filtered = new ArrayList<>();
        for (int pos1 : positions1) {
            for (int pos2 : positions2) {
                if (pos2 == pos1 + 1) {
                    filtered.add(pos2);
                }
            }
        }
        return filtered.stream().mapToInt(Integer::intValue).toArray();
    }
}
