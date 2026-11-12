package me.rexsystems.rexChatGames.papi;

import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import me.rexsystems.rexChatGames.RexChatGames;
import me.rexsystems.rexChatGames.game.GameRunner;
import me.rexsystems.rexChatGames.game.GameType;
import org.bukkit.OfflinePlayer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public final class RexChatGamesExpansion extends PlaceholderExpansion {

    private final RexChatGames plugin;

    public RexChatGamesExpansion(RexChatGames plugin) {
        this.plugin = plugin;
    }

    @Override
    public @NotNull String getIdentifier() {
        return "rexchatgames";
    }

    @Override
    public @NotNull String getAuthor() {
        return "RexSystems";
    }

    @Override
    public @NotNull String getVersion() {
        return plugin.getDescription().getVersion();
    }

    @Override
    public boolean persist() {
        return true;
    }

    @Override
    public @Nullable String onRequest(OfflinePlayer player, @NotNull String params) {
        if (params.equals("wins")) {
            if (player == null) {
                return "";
            }
            return Integer.toString(plugin.stats().wins(player.getUniqueId()));
        }
        if (params.startsWith("wins_")) {
            if (player == null) {
                return "";
            }
            GameType type = GameType.byId(params.substring("wins_".length()));
            if (type == null) {
                return null;
            }
            return Integer.toString(plugin.stats().wins(player.getUniqueId(), type));
        }
        if (params.startsWith("fastest_")) {
            if (player == null) {
                return "";
            }
            GameType type = GameType.byId(params.substring("fastest_".length()));
            if (type == null) {
                return null;
            }
            Long millis = plugin.stats().fastest(player.getUniqueId(), type);
            return millis == null ? "" : GameRunner.Times.seconds(millis);
        }
        if (params.startsWith("record_")) {
            GameType type = GameType.byId(params.substring("record_".length()));
            if (type == null) {
                return null;
            }
            Long millis = plugin.stats().globalRecord(type);
            return millis == null ? "" : GameRunner.Times.seconds(millis);
        }
        return null;
    }
}
