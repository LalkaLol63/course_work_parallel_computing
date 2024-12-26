package lohvin;

import java.util.Arrays;
import java.util.List;

public class TextProcessor {
    public static List<String> preprocess(String content) {
        return Arrays.stream(content.toLowerCase().split("\\W+"))
                .filter(token -> !token.isEmpty())
                .toList();
    }
}
