package me.rexsystems.rexChatGames;

import io.papermc.paper.threadedregions.scheduler.ScheduledTask;
import me.rexsystems.rexChatGames.chat.ChatGameListener;
import me.rexsystems.rexChatGames.command.ChatGamesCommand;
import me.rexsystems.rexChatGames.config.Settings;
import me.rexsystems.rexChatGames.feedback.Broadcaster;
import me.rexsystems.rexChatGames.game.GameRunner;
import me.rexsystems.rexChatGames.papi.RexChatGamesExpansion;
import me.rexsystems.rexChatGames.player.OnlinePlayers;
import me.rexsystems.rexChatGames.stats.StatsStore;
import org.bukkit.Bukkit;
import org.bukkit.command.PluginCommand;
import org.bstats.bukkit.Metrics;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;

public final class RexChatGames extends JavaPlugin {

    private static final int BSTATS_PLUGIN_ID = 35270;

    private Settings settings;
    private StatsStore stats;
    private OnlinePlayers onlinePlayers;
    private Broadcaster broadcaster;
    private GameRunner runner;
    private ScheduledTask ticker;

    @Override
    public void onEnable() {
        this.settings = new Settings(this);
        this.settings.reload();
        this.stats = new StatsStore(new File(getDataFolder(), "stats.db"), getLogger());
        this.stats.open();
        this.onlinePlayers = new OnlinePlayers(this);
        this.onlinePlayers.snapshotOnline();
        this.broadcaster = new Broadcaster(this);
        this.runner = new GameRunner(this);

        getServer().getPluginManager().registerEvents(new ChatGameListener(this), this);
        ChatGamesCommand command = new ChatGamesCommand(this);
        PluginCommand pluginCommand = getCommand("rexchatgames");
        if (pluginCommand != null) {
            pluginCommand.setExecutor(command);
            pluginCommand.setTabCompleter(command);
        }
        if (getServer().getPluginManager().getPlugin("PlaceholderAPI") != null) {
            new RexChatGamesExpansion(this).register();
            getLogger().info("PlaceholderAPI expansion registered.");
        }
        startTicking();
        new Metrics(this, BSTATS_PLUGIN_ID);
        getLogger().info("RexChatGames enabled.");
    }

    @Override
    public void onDisable() {
        stopTicking();
        if (runner != null) {
            runner.shutdown();
        }
        if (stats != null) {
            stats.close();
        }
        getLogger().info("RexChatGames disabled.");
    }

    public void reloadPlugin() {
        settings.reload();
        startTicking();
    }

    public void startTicking() {
        stopTicking();
        long period = settings.intervalSeconds() * 20L;
        ticker = Bukkit.getGlobalRegionScheduler().runAtFixedRate(this, task -> runner.tryAuto(), period, period);
    }

    public void stopTicking() {
        if (ticker != null) {
            ticker.cancel();
            ticker = null;
        }
    }

    public Settings settings() {
        return settings;
    }

    public StatsStore stats() {
        return stats;
    }

    public OnlinePlayers onlinePlayers() {
        return onlinePlayers;
    }

    public Broadcaster broadcaster() {
        return broadcaster;
    }

    public GameRunner runner() {
        return runner;
    }
}
