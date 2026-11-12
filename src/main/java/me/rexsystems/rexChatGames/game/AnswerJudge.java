package me.rexsystems.rexChatGames.game;

import java.util.List;

public final class AnswerJudge {

    public enum Result {
        IGNORE,
        WRONG,
        CAPITALIZATION,
        CORRECT
    }

    private AnswerJudge() {
    }

    public static Result judge(String message, List<String> answers, boolean caseSensitive, boolean numeric) {
        if (message == null || answers == null || answers.isEmpty()) {
            return Result.IGNORE;
        }
        String text = message.trim();
        if (text.isEmpty()) {
            return Result.IGNORE;
        }
        if (numeric) {
            return judgeNumber(text, answers);
        }
        boolean capitalization = false;
        for (String answer : answers) {
            if (answer == null) {
                continue;
            }
            String expected = answer.trim();
            if (expected.isEmpty()) {
                continue;
            }
            if (caseSensitive) {
                if (text.equals(expected)) {
                    return Result.CORRECT;
                }
                if (text.equalsIgnoreCase(expected)) {
                    capitalization = true;
                }
            } else if (text.equalsIgnoreCase(expected)) {
                return Result.CORRECT;
            }
        }
        return capitalization ? Result.CAPITALIZATION : Result.IGNORE;
    }

    private static Result judgeNumber(String text, List<String> answers) {
        if (!text.matches("-?\\d+")) {
            return Result.IGNORE;
        }
        Integer value;
        try {
            value = Integer.parseInt(text);
        } catch (NumberFormatException ex) {
            return Result.WRONG;
        }
        for (String answer : answers) {
            if (answer == null) {
                continue;
            }
            try {
                if (value == Integer.parseInt(answer.trim())) {
                    return Result.CORRECT;
                }
            } catch (NumberFormatException ignored) {
            }
        }
        return Result.WRONG;
    }
}
