package me.rexsystems.rexChatGames.feedback;

import me.rexsystems.rexChatGames.RexChatGames;
import me.rexsystems.rexChatGames.config.Settings;
import me.rexsystems.rexChatGames.game.Round;
import me.rexsystems.rexChatGames.util.SchedulerUtils;
import net.kyori.adventure.title.Title;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.time.Duration;
import java.util.Map;

public final class Broadcaster {

    private final RexChatGames plugin;

    public Broadcaster(RexChatGames plugin) {
        this.plugin = plugin;
    }

    public void toSender(CommandSender sender, String key, Map<String, String> values) {
        if (sender == null) {
            return;
        }
        String template = plugin.settings().message(key);
        if (template.isBlank()) {
            return;
        }
        Player player = sender instanceof Player online ? online : null;
        sender.sendMessage(Texts.render(player, template, plugin.settings().prefix(), values));
    }

    public void toReceivers(String key, Map<String, String> values, Settings.SoundSetting sound) {
        String template = plugin.settings().message(key);
        for (Player player : Bukkit.getOnlinePlayers()) {
            if (!plugin.onlinePlayers().receives(player)) {
                continue;
            }
            SchedulerUtils.runForPlayer(plugin, player, () -> {
                if (!player.isOnline() || !plugin.onlinePlayers().receives(player)) {
                    return;
                }
                if (!template.isBlank()) {
                    player.sendMessage(Texts.render(player, template, plugin.settings().prefix(), values));
                }
                play(player, sound);
            });
        }
    }

    public void announceStart(Round round, Map<String, String> values) {
        String template = plugin.settings().message("start");
        Settings.SoundSetting sound = plugin.settings().startSound();
        for (Player player : Bukkit.getOnlinePlayers()) {
            if (!plugin.onlinePlayers().receives(player)) {
                continue;
            }
            SchedulerUtils.runForPlayer(plugin, player, () -> {
                if (!player.isOnline() || !plugin.onlinePlayers().receives(player)) {
                    return;
                }
                if (!template.isBlank()) {
                    player.sendMessage(Texts.render(player, template, plugin.settings().prefix(), values));
                }
                showTitle(player, values);
                showActionBar(player, values);
                play(player, sound);
            });
        }
    }

    private void showTitle(Player player, Map<String, String> values) {
        Settings.TitleSetting title = plugin.settings().title();
        if (title == null || !title.enabled()) {
            return;
        }
        player.showTitle(Title.title(
                Texts.render(player, title.title(), plugin.settings().prefix(), values),
                Texts.render(player, title.subtitle(), plugin.settings().prefix(), values),
                Title.Times.times(
                        Duration.ofMillis(title.fadeIn() * 50L),
                        Duration.ofMillis(title.stay() * 50L),
                        Duration.ofMillis(title.fadeOut() * 50L))));
    }

    private void showActionBar(Player player, Map<String, String> values) {
        if (!plugin.settings().actionBarEnabled()) {
            return;
        }
        String text = plugin.settings().actionBarText();
        if (text.isBlank()) {
            return;
        }
        player.sendActionBar(Texts.render(player, text, plugin.settings().prefix(), values));
    }

    private static void play(Player player, Settings.SoundSetting sound) {
        if (sound == null || !sound.enabled() || sound.sound() == null) {
            return;
        }
        player.playSound(player, sound.sound(), sound.volume(), sound.pitch());
    }
}
