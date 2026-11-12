package me.rexsystems.rexChatGames.config;

import me.rexsystems.rexChatGames.game.GameType;
import org.bukkit.Material;

import java.util.List;

public record GameDef(
        GameType type,
        boolean enabled,
        int weight,
        int hidePercent,
        int min,
        int max,
        List<String> operations,
        String prompt,
        String hover,
        List<String> commands,
        List<ItemReward> items
) {
    public GameDef {
        operations = operations == null ? List.of() : List.copyOf(operations);
        prompt = prompt == null ? "" : prompt;
        hover = hover == null ? "" : hover;
        commands = commands == null ? List.of() : List.copyOf(commands);
        items = items == null ? List.of() : List.copyOf(items);
        hidePercent = Math.max(0, Math.min(100, hidePercent));
        if (weight < 0) {
            weight = 0;
        }
    }

    public record ItemReward(Material material, int amount, String name) {
    }
}
