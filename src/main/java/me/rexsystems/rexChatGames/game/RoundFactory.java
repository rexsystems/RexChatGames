package me.rexsystems.rexChatGames.game;

import me.rexsystems.rexChatGames.config.GameDef;
import me.rexsystems.rexChatGames.config.TriviaQuestion;
import me.rexsystems.rexChatGames.config.WordList;

import java.util.List;
import java.util.Map;
import java.util.Random;

public final class RoundFactory {

    private RoundFactory() {
    }

    public static Round create(long id, GameDef def, WordList words, List<TriviaQuestion> trivia, Random random, long now) {
        return switch (def.type()) {
            case COMPLETE -> wordRound(id, def, words, random, now, word -> {
                CompletionMask.Result mask = CompletionMask.apply(word, def.hidePercent(), random);
                return Map.of("masked", mask.shown());
            });
            case TYPE -> wordRound(id, def, words, random, now, word -> Map.of("word", word));
            case REVERSE -> wordRound(id, def, words, random, now, word -> Map.of(
                    "reversed", new StringBuilder(word).reverse().toString()));
            case UNSCRAMBLE -> wordRound(id, def, words, random, now, word -> Map.of(
                    "scrambled", WordScramble.scramble(word, random)));
            case GUESS_NUMBER -> numberRound(id, def, random, now);
            case MATH -> mathRound(id, def, random, now);
            case TRIVIA -> triviaRound(id, def, trivia, random, now);
        };
    }

    private static Round wordRound(long id, GameDef def, WordList words, Random random, long now, Prompt prompt) {
        if (words == null || words.isEmpty()) {
            return null;
        }
        String word = words.random(random);
        String template = def.prompt().isBlank() ? def.type().id() : def.prompt();
        return new Round(id, def, fill(template, prompt.values(word)), List.of(word), now);
    }

    private static Round numberRound(long id, GameDef def, Random random, long now) {
        int lo = Math.min(def.min(), def.max());
        int hi = Math.max(def.min(), def.max());
        int span = hi - lo;
        int value = lo;
        if (span > 0) {
            value = lo + random.nextInt(span + 1);
        }
        String template = def.prompt().isBlank() ? "Guess a whole number from {min} to {max}." : def.prompt();
        String text = fill(template, Map.of("min", Integer.toString(lo), "max", Integer.toString(hi)));
        return new Round(id, def, text, List.of(Integer.toString(value)), now);
    }

    private static Round mathRound(long id, GameDef def, Random random, long now) {
        MathProblems.Problem problem = MathProblems.generate(random, def.operations(), def.min(), def.max());
        String template = def.prompt().isBlank() ? "What is {expression}?" : def.prompt();
        String text = fill(template, Map.of("expression", problem.expression()));
        return new Round(id, def, text, List.of(Integer.toString(problem.answer())), now);
    }

    private static Round triviaRound(long id, GameDef def, List<TriviaQuestion> trivia, Random random, long now) {
        if (trivia == null || trivia.isEmpty()) {
            return null;
        }
        TriviaQuestion question = trivia.get(random.nextInt(trivia.size()));
        String template = def.prompt().isBlank() ? "{question}" : def.prompt();
        String text = fill(template, Map.of("question", question.question()));
        return new Round(id, def, text, question.answers(), now);
    }

    private static String fill(String template, Map<String, String> values) {
        String text = template == null ? "" : template;
        for (Map.Entry<String, String> entry : values.entrySet()) {
            String value = entry.getValue() == null ? "" : entry.getValue();
            text = text.replace("{" + entry.getKey() + "}", value);
        }
        return text;
    }

    @FunctionalInterface
    private interface Prompt {
        Map<String, String> values(String word);
    }
}
