package me.rexsystems.rexChatGames.config;

import java.util.List;

public record TriviaQuestion(String question, List<String> answers) {
    public TriviaQuestion {
        question = question == null ? "" : question;
        answers = answers == null ? List.of() : List.copyOf(answers);
    }
}
