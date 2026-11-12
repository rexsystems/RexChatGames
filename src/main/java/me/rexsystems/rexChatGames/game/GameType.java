package me.rexsystems.rexChatGames.game;

import java.util.Locale;

public enum GameType {
    COMPLETE("complete", "Complete", false),
    GUESS_NUMBER("guess-number", "Guess the number", true),
    MATH("math", "Math", true),
    TRIVIA("trivia", "Trivia", false),
    TYPE("type", "Type", false),
    REVERSE("reverse", "Reverse", false),
    UNSCRAMBLE("unscramble", "Unscramble", false);

    private final String id;
    private final String label;
    private final boolean numeric;

    GameType(String id, String label, boolean numeric) {
        this.id = id;
        this.label = label;
        this.numeric = numeric;
    }

    public String id() {
        return id;
    }

    public String label() {
        return label;
    }

    public boolean numeric() {
        return numeric;
    }

    public static GameType byId(String raw) {
        if (raw == null || raw.isBlank()) {
            return null;
        }
        String id = raw.toLowerCase(Locale.ROOT);
        for (GameType type : values()) {
            if (type.id.equals(id)) {
                return type;
            }
        }
        return null;
    }
}
