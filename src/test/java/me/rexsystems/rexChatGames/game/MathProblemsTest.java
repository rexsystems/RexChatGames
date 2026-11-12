package me.rexsystems.rexChatGames.game;

import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Random;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class MathProblemsTest {

    @Test
    void noDivisionByZeroOverManySeeds() {
        List<String> onlyDivide = List.of("divide");
        List<String> all = List.of("add", "subtract", "multiply", "divide");
        int[][] ranges = {{-20, 20}, {0, 0}, {1, 12}, {-8, -1}, {0, 5}};
        for (int seed = 0; seed < 2000; seed++) {
            for (int[] range : ranges) {
                assertSafe(MathProblems.generate(new Random(seed), onlyDivide, range[0], range[1]));
                assertSafe(MathProblems.generate(new Random(seed + 17), all, range[0], range[1]));
            }
        }
    }

    private static void assertSafe(MathProblems.Problem problem) {
        long expected = switch (problem.operator()) {
            case "+" -> (long) problem.left() + problem.right();
            case "-" -> (long) problem.left() - problem.right();
            case "*" -> (long) problem.left() * (long) problem.right();
            case "/" -> {
                assertNotEquals(0, problem.right());
                assertFalse(problem.expression().endsWith("/ 0"));
                assertFalse(problem.left() == Integer.MIN_VALUE && problem.right() == -1);
                assertEquals(0, problem.left() % problem.right());
                yield problem.left() / problem.right();
            }
            default -> throw new AssertionError(problem.operator());
        };
        assertTrue(expected <= Integer.MAX_VALUE && expected >= Integer.MIN_VALUE);
        assertEquals((int) expected, problem.answer());
        assertEquals(problem.left() + " " + problem.operator() + " " + problem.right(), problem.expression());
    }
}
