package me.rexsystems.rexChatGames.config;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Random;

public final class WordList {

    private final List<String> words;

    public WordList(List<String> fileWords, List<String> materialWords) {
        Map<String, String> unique = new LinkedHashMap<>();
        add(unique, fileWords);
        add(unique, materialWords);
        this.words = List.copyOf(unique.values());
    }

    public boolean isEmpty() {
        return words.isEmpty();
    }

    public int size() {
        return words.size();
    }

    public String random(Random random) {
        return words.get(random.nextInt(words.size()));
    }

    private static void add(Map<String, String> unique, List<String> values) {
        if (values == null) {
            return;
        }
        for (String value : values) {
            if (value == null) {
                continue;
            }
            String trimmed = value.trim();
            if (trimmed.isEmpty()) {
                continue;
            }
            unique.putIfAbsent(trimmed.toLowerCase(Locale.ROOT), trimmed);
        }
    }

    public static List<String> materialNames(org.bukkit.Material[] materials) {
        List<String> names = new ArrayList<>();
        if (materials == null) {
            return names;
        }
        for (org.bukkit.Material material : materials) {
            if (material == null || material.isAir()) {
                continue;
            }
            String raw = material.name();
            if (raw.startsWith("LEGACY_")) {
                continue;
            }
            names.add(pretty(raw));
        }
        return names;
    }

    static String pretty(String raw) {
        String[] parts = raw.toLowerCase(Locale.ROOT).split("_");
        StringBuilder out = new StringBuilder();
        for (String part : parts) {
            if (part.isEmpty()) {
                continue;
            }
            if (!out.isEmpty()) {
                out.append(' ');
            }
            out.append(Character.toUpperCase(part.charAt(0)));
            if (part.length() > 1) {
                out.append(part.substring(1));
            }
        }
        return out.toString();
    }
}
