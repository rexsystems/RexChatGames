package me.rexsystems.rexChatGames.player;

import me.rexsystems.rexChatGames.RexChatGames;
import me.rexsystems.rexChatGames.util.SchedulerUtils;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public final class OnlinePlayers {

    private final RexChatGames plugin;
    private final ConcurrentHashMap<UUID, String> worlds = new ConcurrentHashMap<>();

    public OnlinePlayers(RexChatGames plugin) {
        this.plugin = plugin;
    }

    public void remember(Player player) {
        if (player == null) {
            return;
        }
        try {
            worlds.put(player.getUniqueId(), player.getWorld().getName());
        } catch (Throwable ignored) {
        }
    }

    public void forget(UUID playerId) {
        if (playerId != null) {
            worlds.remove(playerId);
        }
    }

    public void snapshotOnline() {
        for (Player player : Bukkit.getOnlinePlayers()) {
            SchedulerUtils.runForPlayer(plugin, player, () -> remember(player));
        }
    }

    public int eligible() {
        int count = 0;
        for (Player player : Bukkit.getOnlinePlayers()) {
            if (canAnswer(player)) {
                count++;
            }
        }
        return count;
    }

    public boolean canAnswer(Player player) {
        try {
            if (player == null || !player.isOnline()) {
                return false;
            }
            if (!player.hasPermission("rexchatgames.play")) {
                return false;
            }
            return plugin.settings().worldAllowed(world(player));
        } catch (Throwable ignored) {
            return false;
        }
    }

    public boolean receives(Player player) {
        try {
            if (player == null || !player.isOnline()) {
                return false;
            }
            if (plugin.stats().isMuted(player.getUniqueId())) {
                return false;
            }
            return plugin.settings().worldAllowed(world(player));
        } catch (Throwable ignored) {
            return false;
        }
    }

    private String world(Player player) {
        String cached = worlds.get(player.getUniqueId());
        if (cached != null) {
            return cached;
        }
        String live = player.getWorld().getName();
        worlds.put(player.getUniqueId(), live);
        return live;
    }
}
