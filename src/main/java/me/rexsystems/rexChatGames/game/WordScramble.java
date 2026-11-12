package me.rexsystems.rexChatGames.game;

import java.util.Random;

public final class WordScramble {

    private WordScramble() {
    }

    public static String scramble(String phrase, Random random) {
        if (phrase == null || phrase.isEmpty()) {
            return phrase == null ? "" : phrase;
        }
        String[] parts = phrase.split(" ", -1);
        StringBuilder out = new StringBuilder(phrase.length());
        for (int i = 0; i < parts.length; i++) {
            if (i > 0) {
                out.append(' ');
            }
            out.append(scrambleWord(parts[i], random));
        }
        return out.toString();
    }

    private static String scrambleWord(String word, Random random) {
        if (word.length() < 2) {
            return word;
        }
        char[] chars = word.toCharArray();
        String current = word;
        for (int attempt = 0; attempt < 8; attempt++) {
            shuffle(chars, random);
            current = new String(chars);
            if (!current.equals(word) || !hasDistinct(chars)) {
                return current;
            }
        }
        return current;
    }

    private static void shuffle(char[] chars, Random random) {
        for (int i = chars.length - 1; i > 0; i--) {
            int j = random.nextInt(i + 1);
            char tmp = chars[i];
            chars[i] = chars[j];
            chars[j] = tmp;
        }
    }

    private static boolean hasDistinct(char[] chars) {
        char first = chars[0];
        for (int i = 1; i < chars.length; i++) {
            if (chars[i] != first) {
                return true;
            }
        }
        return false;
    }
}
