package me.rexsystems.rexChatGames.game;

import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Random;

import static org.junit.jupiter.api.Assertions.assertEquals;

class WordScrambleTest {

    @Test
    void scrambleKeepsSpacesAsWordBoundaries() {
        String phrase = "Jungle Sapling";
        for (int seed = 0; seed < 300; seed++) {
            assertWordBoundaries(phrase, WordScramble.scramble(phrase, new Random(seed)));
        }
        assertWordBoundaries("Red  Stone", WordScramble.scramble("Red  Stone", new Random(4)));
        assertWordBoundaries("A B C", WordScramble.scramble("A B C", new Random(9)));
    }

    private static void assertWordBoundaries(String original, String scrambled) {
        String[] left = original.split(" ", -1);
        String[] right = scrambled.split(" ", -1);
        assertEquals(left.length, right.length, scrambled);
        assertEquals(count(original, ' '), count(scrambled, ' '));
        for (int i = 0; i < left.length; i++) {
            assertEquals(sorted(left[i]), sorted(right[i]), scrambled);
        }
    }

    private static int count(String text, char needle) {
        int count = 0;
        for (int i = 0; i < text.length(); i++) {
            if (text.charAt(i) == needle) {
                count++;
            }
        }
        return count;
    }

    private static String sorted(String word) {
        char[] chars = word.toCharArray();
        Arrays.sort(chars);
        return new String(chars);
    }
}
