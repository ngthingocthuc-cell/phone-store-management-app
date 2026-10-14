package Utils;

import java.text.Normalizer;
import java.util.Locale;

/** Helpers for case- and accent-insensitive Vietnamese text searches. */
public final class TextSearch {
    private TextSearch() { }

    public static String normalize(String value) {
        if (value == null) return "";
        String decomposed = Normalizer.normalize(value, Normalizer.Form.NFD)
                .replaceAll("\\p{M}+", "")
                .toLowerCase(Locale.ROOT)
                .replace('đ', 'd');
        return decomposed.replaceAll("\\s+", " ").trim();
    }

    public static boolean contains(String value, String query) {
        String normalizedQuery = normalize(query);
        return normalizedQuery.isEmpty() || normalize(value).contains(normalizedQuery);
    }
}
