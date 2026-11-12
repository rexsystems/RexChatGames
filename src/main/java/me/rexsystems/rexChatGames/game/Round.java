package me.rexsystems.rexChatGames.game;

import me.rexsystems.rexChatGames.config.GameDef;

import java.util.List;
import java.util.UUID;

public final class Round {

    private final Object lock = new Object();
    private final FirstAnswer firstAnswer = new FirstAnswer();
    private final long id;
    private final GameDef def;
    private final String prompt;
    private final List<String> answers;
    private final long startedAt;
    private UUID winner;
    private long elapsedMillis;
    private boolean closed;
    private boolean payoutStarted;

    public Round(long id, GameDef def, String prompt, List<String> answers, long startedAt) {
        this.id = id;
        this.def = def;
        this.prompt = prompt == null ? "" : prompt;
        this.answers = List.copyOf(answers);
        this.startedAt = startedAt;
    }

    public long id() {
        return id;
    }

    public GameDef def() {
        return def;
    }

    public GameType type() {
        return def.type();
    }

    public String prompt() {
        return prompt;
    }

    public String hover() {
        return def.hover();
    }

    public List<String> answers() {
        return answers;
    }

    public boolean numeric() {
        return type().numeric();
    }

    public long startedAt() {
        return startedAt;
    }

    public String reveal() {
        return answers.isEmpty() ? "" : answers.get(0);
    }

    public boolean claim(UUID playerId, long elapsed) {
        synchronized (lock) {
            if (closed) {
                return false;
            }
            if (!firstAnswer.claim()) {
                return false;
            }
            winner = playerId;
            elapsedMillis = elapsed;
            return true;
        }
    }

    public boolean closeIfUnclaimed() {
        synchronized (lock) {
            if (closed || winner != null) {
                return false;
            }
            closed = true;
            return true;
        }
    }

    public boolean beginPayout() {
        synchronized (lock) {
            if (payoutStarted || winner == null) {
                return false;
            }
            payoutStarted = true;
            closed = true;
            return true;
        }
    }

    public UUID winner() {
        synchronized (lock) {
            return winner;
        }
    }

    public long elapsed() {
        synchronized (lock) {
            return elapsedMillis;
        }
    }

    public boolean isClosed() {
        synchronized (lock) {
            return closed;
        }
    }
}
