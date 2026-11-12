package me.rexsystems.rexChatGames.game;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;

public final class CompletionMask {

    public static final char HIDDEN = '_';

    public record Result(String shown, int hiddenLetters, int visibleLetters) {
    }

    private CompletionMask() {
    }

    public static Result apply(String phrase, int percent, Random random) {
        String text = phrase == null ? "" : phrase;
        int clamped = Math.max(0, Math.min(100, percent));
        List<Integer> letters = new ArrayList<>();
        for (int i = 0; i < text.length(); i++) {
            if (Character.isLetter(text.charAt(i))) {
                letters.add(i);
            }
        }
        int hide = (int) Math.round(letters.size() * (clamped / 100.0));
        if (!letters.isEmpty() && hide >= letters.size()) {
            hide = letters.size() - 1;
        }
        if (hide < 0) {
            hide = 0;
        }
        Collections.shuffle(letters, random);
        Set<Integer> hidden = new HashSet<>(letters.subList(0, hide));
        StringBuilder shown = new StringBuilder(text.length());
        for (int i = 0; i < text.length(); i++) {
            shown.append(hidden.contains(i) ? HIDDEN : text.charAt(i));
        }
        return new Result(shown.toString(), hide, letters.size() - hide);
    }
}
