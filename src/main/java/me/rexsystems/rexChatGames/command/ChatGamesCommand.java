package me.rexsystems.rexChatGames.command;

import me.rexsystems.rexChatGames.RexChatGames;
import me.rexsystems.rexChatGames.game.GameRunner;
import me.rexsystems.rexChatGames.game.GameType;
import me.rexsystems.rexChatGames.stats.StatsStore;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public final class ChatGamesCommand implements CommandExecutor, TabCompleter {

    private final RexChatGames plugin;

    public ChatGamesCommand(RexChatGames plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (args.length == 0 || args[0].equalsIgnoreCase("help")) {
            return help(sender);
        }
        return switch (args[0].toLowerCase(Locale.ROOT)) {
            case "start" -> start(sender, args);
            case "stop" -> stop(sender);
            case "toggle" -> toggle(sender);
            case "top" -> top(sender, args);
            case "status" -> status(sender);
            case "reload" -> reload(sender);
            default -> help(sender);
        };
    }

    private boolean help(CommandSender sender) {
        plugin.broadcaster().toSender(sender, "help-header", Map.of());
        for (String line : plugin.settings().help()) {
            if (sender instanceof Player player) {
                player.sendMessage(me.rexsystems.rexChatGames.feedback.Texts.render(player, line, plugin.settings().prefix(), Map.of()));
            } else {
                sender.sendMessage(me.rexsystems.rexChatGames.util.TextParser.parse(line));
            }
        }
        return true;
    }

    private boolean start(CommandSender sender, String[] args) {
        if (!allowed(sender, "rexchatgames.start")) {
            plugin.broadcaster().toSender(sender, "no-permission", Map.of());
            return true;
        }
        GameType type = null;
        if (args.length >= 2) {
            type = GameType.byId(args[1]);
            if (type == null) {
                plugin.broadcaster().toSender(sender, "unknown-type", Map.of());
                return true;
            }
        }
        plugin.runner().startCommand(sender, type);
        return true;
    }

    private boolean stop(CommandSender sender) {
        if (!allowed(sender, "rexchatgames.start")) {
            plugin.broadcaster().toSender(sender, "no-permission", Map.of());
            return true;
        }
        plugin.runner().stop(sender);
        return true;
    }

    private boolean toggle(CommandSender sender) {
        if (!allowed(sender, "rexchatgames.toggle")) {
            plugin.broadcaster().toSender(sender, "no-permission", Map.of());
            return true;
        }
        if (!(sender instanceof Player player)) {
            plugin.broadcaster().toSender(sender, "players-only", Map.of());
            return true;
        }
        boolean hidden = plugin.stats().toggle(player.getUniqueId());
        plugin.broadcaster().toSender(sender, hidden ? "toggle-hidden" : "toggle-shown", Map.of());
        return true;
    }

    private boolean top(CommandSender sender, String[] args) {
        GameType type = null;
        if (args.length >= 2) {
            type = GameType.byId(args[1]);
            if (type == null) {
                plugin.broadcaster().toSender(sender, "unknown-type", Map.of());
                return true;
            }
        }
        List<StatsStore.WinRow> rows = type == null
                ? plugin.stats().topTotal(10)
                : plugin.stats().topType(type, 10);
        if (rows.isEmpty()) {
            plugin.broadcaster().toSender(sender, "top-empty", labels(type));
        } else {
            String scope = type == null ? "" : " (" + type.label() + ")";
            Map<String, String> header = new HashMap<>(labels(type));
            header.put("scope", scope);
            plugin.broadcaster().toSender(sender, "top-header", header);
            int place = 1;
            for (StatsStore.WinRow row : rows) {
                Map<String, String> line = new HashMap<>();
                line.put("place", Integer.toString(place++));
                line.put("player", row.name() == null ? "" : row.name());
                line.put("wins", Integer.toString(row.wins()));
                plugin.broadcaster().toSender(sender, "top-line", line);
            }
        }
        if (type != null) {
            StatsStore.RecordRow record = plugin.stats().record(type);
            if (record != null) {
                Map<String, String> line = new HashMap<>(labels(type));
                line.put("player", record.name() == null ? "" : record.name());
                line.put("time", GameRunner.Times.seconds(record.millis()));
                plugin.broadcaster().toSender(sender, "record-line", line);
            }
        }
        return true;
    }

    private boolean status(CommandSender sender) {
        plugin.broadcaster().toSender(sender, "status-header", Map.of());
        String never = plugin.settings().message("status-never");
        if (never.isBlank()) {
            never = "never";
        }
        java.util.Map<GameType, Long> lastRuns = plugin.stats().lastRuns();
        for (GameType type : GameType.values()) {
            Long at = lastRuns.get(type);
            String when = at == null || at <= 0L ? never : GameRunner.Times.clock(at);
            Map<String, String> line = new HashMap<>(labels(type));
            line.put("when", when);
            plugin.broadcaster().toSender(sender, "status-line", line);
        }
        return true;
    }

    private boolean reload(CommandSender sender) {
        if (!allowed(sender, "rexchatgames.reload")) {
            plugin.broadcaster().toSender(sender, "no-permission", Map.of());
            return true;
        }
        plugin.reloadPlugin();
        plugin.broadcaster().toSender(sender, "reload", Map.of());
        return true;
    }

    private boolean allowed(CommandSender sender, String node) {
        return sender.hasPermission("rexchatgames.admin") || sender.hasPermission(node);
    }

    private static Map<String, String> labels(GameType type) {
        if (type == null) {
            return Map.of("type", "", "type-label", "");
        }
        return Map.of("type", type.id(), "type-label", type.label());
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (args.length == 1) {
            List<String> options = new ArrayList<>();
            options.add("help");
            options.add("top");
            options.add("status");
            if (allowed(sender, "rexchatgames.toggle")) {
                options.add("toggle");
            }
            if (allowed(sender, "rexchatgames.start")) {
                options.add("start");
                options.add("stop");
            }
            if (allowed(sender, "rexchatgames.reload")) {
                options.add("reload");
            }
            return filter(options, args[0]);
        }
        if (args.length == 2 && (args[0].equalsIgnoreCase("start") || args[0].equalsIgnoreCase("top"))) {
            List<String> types = new ArrayList<>();
            for (GameType type : GameType.values()) {
                types.add(type.id());
            }
            return filter(types, args[1]);
        }
        return List.of();
    }

    private static List<String> filter(List<String> options, String prefix) {
        String needle = prefix == null ? "" : prefix.toLowerCase(Locale.ROOT);
        List<String> matches = new ArrayList<>();
        for (String option : options) {
            if (option.toLowerCase(Locale.ROOT).startsWith(needle)) {
                matches.add(option);
            }
        }
        return matches;
    }
}
