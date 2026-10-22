package com.example.smartpantrymanager.logic;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

/**
 * Turns free-typed ingredient names into a comparable "key".
 * Handles letter case, extra spaces, punctuation, simple descriptors
 * ("fresh", "large", ...) and singular/plural forms.
 * <p>
 * Both the pantry name and the recipe name go through the same method, so
 * even if a plural rule is imperfect, both sides still agree with each other.
 */
public final class NameNormalizer {

    private static final Set<String> DESCRIPTORS = new HashSet<>(Arrays.asList(
            "fresh", "large", "small", "medium", "ripe", "chopped", "diced",
            "sliced", "grated", "minced", "frozen", "raw"));

    private NameNormalizer() {
    }

    public static String normalize(String name) {
        if (name == null) return "";

        String cleaned = name.toLowerCase(Locale.ENGLISH)
                .replaceAll("[^\\p{L}\\s]", " ")  // drop digits and punctuation
                .replaceAll("\\s+", " ")
                .trim();
        if (cleaned.isEmpty()) return "";

        String[] tokens = cleaned.split(" ");
        List<String> words = new ArrayList<>();
        for (String token : tokens) {
            if (!DESCRIPTORS.contains(token)) {
                words.add(token);
            }
        }
        if (words.isEmpty()) {            // name was only descriptors, keep it
            words.addAll(Arrays.asList(tokens));
        }

        // Only the last word carries the plural: "spring onions" -> "spring onion"
        int last = words.size() - 1;
        words.set(last, singularize(words.get(last)));

        StringBuilder result = new StringBuilder();
        for (int i = 0; i < words.size(); i++) {
            if (i > 0) result.append(' ');
            result.append(words.get(i));
        }
        return result.toString();
    }

    /**
     * Simple English singular rules: enough for pantry items, not full NLP.
     */
    static String singularize(String w) {
        String s = w;
        if (w.length() > 3) {
            if (w.endsWith("ies") && w.length() > 4) {
                s = w.substring(0, w.length() - 3) + "y";          // berries -> berry
            } else if (w.endsWith("oes") || w.endsWith("ches") || w.endsWith("shes")
                    || w.endsWith("sses") || w.endsWith("xes")) {
                s = w.substring(0, w.length() - 2);                // tomatoes -> tomato
            } else if (w.endsWith("ss") || w.endsWith("us") || w.endsWith("is")) {
                s = w;                                             // hummus, asparagus
            } else if (w.endsWith("s")) {
                s = w.substring(0, w.length() - 1);                // eggs -> egg
            }
        }
        // cookie / cookies both end up as "cooky"
        if (s.endsWith("ie") && s.length() > 3) {
            s = s.substring(0, s.length() - 2) + "y";
        }
        return s;
    }
}