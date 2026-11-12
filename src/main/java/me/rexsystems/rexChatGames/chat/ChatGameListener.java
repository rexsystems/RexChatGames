package me.rexsystems.rexChatGames.chat;

import io.papermc.paper.event.player.AsyncChatEvent;
import me.rexsystems.rexChatGames.RexChatGames;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerChangedWorldEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

public final class ChatGameListener implements Listener {

    private final RexChatGames plugin;

    public ChatGameListener(RexChatGames plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = false)
    public void onPaperChat(AsyncChatEvent event) {
        String text = PlainTextComponentSerializer.plainText().serialize(event.originalMessage());
        if (plugin.runner().onChat(event.getPlayer(), text)) {
            event.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = false)
    public void onLegacyChat(AsyncPlayerChatEvent event) {
        if (plugin.runner().onChat(event.getPlayer(), event.getMessage())) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        plugin.onlinePlayers().remember(event.getPlayer());
    }

    @EventHandler
    public void onWorld(PlayerChangedWorldEvent event) {
        plugin.onlinePlayers().remember(event.getPlayer());
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        plugin.runner().onQuit(event.getPlayer());
    }
}
