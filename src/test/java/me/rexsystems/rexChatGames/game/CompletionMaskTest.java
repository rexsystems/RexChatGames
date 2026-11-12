package me.rexsystems.rexChatGames.game;

import org.junit.jupiter.api.Test;

import java.util.Random;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CompletionMaskTest {

    @Test
    void hidesAboutTheConfiguredPercentAndLeavesALetter() {
        check("Jungle Sapling", 40);
        check("ABCDEFGHIJ", 50);
        check("Diamond", 100);
        CompletionMask.Result single = CompletionMask.apply("A", 100, new Random(3));
        assertEquals(1, single.visibleLetters());
        assertEquals(0, single.hiddenLetters());
        assertEquals("A", single.shown());
    }

    private static void check(String phrase, int percent) {
        int letters = countLetters(phrase);
        int expectedHidden = (int) Math.round(letters * (percent / 100.0));
        if (letters > 0 && expectedHidden >= letters) {
            expectedHidden = letters - 1;
        }
        for (int seed = 0; seed < 40; seed++) {
            CompletionMask.Result result = CompletionMask.apply(phrase, percent, new Random(seed));
            assertEquals(phrase.length(), result.shown().length());
            assertEquals(expectedHidden, result.hiddenLetters());
            assertTrue(result.visibleLetters() >= 1);
            int hidden = 0;
            int visible = 0;
            for (int i = 0; i < phrase.length(); i++) {
                char original = phrase.charAt(i);
                char shown = result.shown().charAt(i);
                if (!Character.isLetter(original)) {
                    assertEquals(original, shown);
                    continue;
                }
                if (shown == CompletionMask.HIDDEN) {
                    hidden++;
                } else {
                    assertEquals(original, shown);
                    visible++;
                }
            }
            assertEquals(hidden, result.hiddenLetters());
            assertEquals(visible, result.visibleLetters());
            if (percent <= 60) {
                double ratio = result.hiddenLetters() / (double) letters;
                assertTrue(Math.abs(ratio - (percent / 100.0)) < 0.15, result.shown());
            }
        }
    }

    private static int countLetters(String phrase) {
        int letters = 0;
        for (int i = 0; i < phrase.length(); i++) {
            if (Character.isLetter(phrase.charAt(i))) {
                letters++;
            }
        }
        return letters;
    }
}
