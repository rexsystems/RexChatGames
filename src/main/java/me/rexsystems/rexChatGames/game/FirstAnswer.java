package me.rexsystems.rexChatGames.game;

import java.util.concurrent.atomic.AtomicBoolean;

public final class FirstAnswer {

    private final AtomicBoolean taken = new AtomicBoolean(false);

    public boolean claim() {
        return taken.compareAndSet(false, true);
    }
}
