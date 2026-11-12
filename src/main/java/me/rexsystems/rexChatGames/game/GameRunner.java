package me.rexsystems.rexChatGames.game;

import io.papermc.paper.threadedregions.scheduler.ScheduledTask;
import me.rexsystems.rexChatGames.RexChatGames;
import me.rexsystems.rexChatGames.config.GameDef;
import me.rexsystems.rexChatGames.reward.RewardGiver;
import me.rexsystems.rexChatGames.util.SchedulerUtils;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Random;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;

public final class GameRunner {

    private final RexChatGames plugin;
    private final AtomicReference<Round> active = new AtomicReference<>();
    private final AtomicLong ids = new AtomicLong();
    private final AtomicReference<ScheduledTask> timeout = new AtomicReference<>();
    private final ConcurrentHashMap<UUID, Recent> recent = new ConcurrentHashMap<>();

    public GameRunner(RexChatGames plugin) {
        this.plugin = plugin;
    }

    public void tryAuto() {
        if (active.get() != null) {
            return;
        }
        if (plugin.onlinePlayers().eligible() < plugin.settings().minPlayers()) {
            return;
        }
        GameType type = pick(ThreadLocalRandom.current(), false);
        if (type == null) {
            return;
        }
        start(null, type);
    }

    public void startCommand(CommandSender sender, GameType type) {
        if (active.get() != null) {
            plugin.broadcaster().toSender(sender, "already-running", Map.of());
            return;
        }
        if (type == null) {
            type = pick(ThreadLocalRandom.current(), false);
        }
        if (type == null) {
            plugin.broadcaster().toSender(sender, "no-content", Map.of());
            return;
        }
        GameDef def = plugin.settings().game(type);
        if (def == null || !def.enabled()) {
            plugin.broadcaster().toSender(sender, "type-disabled", labels(type));
            return;
        }
        if (plugin.onlinePlayers().eligible() < plugin.settings().minPlayers()) {
            plugin.broadcaster().toSender(sender, "not-enough-players", Map.of(
                    "min", Integer.toString(plugin.settings().minPlayers())));
            return;
        }
        if (!start(sender, type)) {
            plugin.broadcaster().toSender(sender, "already-running", Map.of());
        }
    }

    public void stop(CommandSender sender) {
        Round round = active.get();
        if (round == null) {
            plugin.broadcaster().toSender(sender, "none-running", Map.of());
            return;
        }
        if (!round.closeIfUnclaimed()) {
            plugin.broadcaster().toSender(sender, "already-ending", Map.of());
            return;
        }
        active.compareAndSet(round, null);
        cancelTimeout();
        plugin.stats().log(round.type(), null, null, null, "stopped");
        plugin.broadcaster().toReceivers("stopped", tokens(round, null, null), null);
    }

    public void expire(long roundId) {
        Round round = active.get();
        if (round == null || round.id() != roundId) {
            return;
        }
        if (!round.closeIfUnclaimed()) {
            return;
        }
        active.compareAndSet(round, null);
        cancelTimeout();
        plugin.stats().log(round.type(), null, null, null, "expired");
        plugin.broadcaster().toReceivers("timeout", tokens(round, null, null), null);
    }

    public void onQuit(Player player) {
        plugin.onlinePlayers().forget(player.getUniqueId());
        Round round = active.get();
        if (round == null || !player.getUniqueId().equals(round.winner())) {
            return;
        }
        if (!round.beginPayout()) {
            return;
        }
        unpaid(round, player.getUniqueId(), player.getName());
    }

    public boolean onChat(Player player, String text) {
        Round round = active.get();
        if (round == null || round.isClosed()) {
            return false;
        }
        if (!plugin.onlinePlayers().canAnswer(player)) {
            return false;
        }
        AnswerJudge.Result result = AnswerJudge.judge(
                text,
                round.answers(),
                plugin.settings().caseSensitive(),
                round.numeric());
        if (result == AnswerJudge.Result.IGNORE) {
            return false;
        }
        String trimmed = text == null ? "" : text.trim();
        if (duplicate(player.getUniqueId(), trimmed)) {
            return true;
        }
        return switch (result) {
            case IGNORE -> false;
            case WRONG -> {
                plugin.broadcaster().toSender(player, "answer-wrong", tokens(round, player, null));
                yield true;
            }
            case CAPITALIZATION -> {
                String key = plugin.settings().warnOnCapitalization() ? "answer-capitalization" : "answer-wrong";
                plugin.broadcaster().toSender(player, key, tokens(round, player, null));
                yield true;
            }
            case CORRECT -> accept(player, round);
        };
    }

    public void shutdown() {
        cancelTimeout();
    }

    private boolean accept(Player player, Round round) {
        long elapsed = Math.max(0L, System.currentTimeMillis() - round.startedAt());
        if (!round.claim(player.getUniqueId(), elapsed)) {
            if (!round.isClosed()) {
                plugin.broadcaster().toSender(player, "answer-late", tokens(round, player, elapsed));
            }
            return true;
        }
        if (!player.isOnline()) {
            if (round.beginPayout()) {
                unpaid(round, player.getUniqueId(), player.getName());
            }
            return true;
        }
        SchedulerUtils.runForPlayer(plugin, player, () -> pay(player, round));
        return true;
    }

    private void pay(Player player, Round round) {
        if (!player.isOnline()) {
            if (round.beginPayout()) {
                unpaid(round, player.getUniqueId(), player.getName());
            }
            return;
        }
        if (!round.beginPayout()) {
            return;
        }
        try {
            RewardGiver.give(plugin, player, round.def());
        } catch (Throwable failure) {
            plugin.getLogger().warning("Reward failed for " + player.getName() + ": " + failure.getMessage());
        }
        active.compareAndSet(round, null);
        cancelTimeout();
        plugin.stats().recordWin(player.getUniqueId(), player.getName(), round.type(), round.elapsed());
        plugin.broadcaster().toReceivers("win", tokens(round, player, round.elapsed()), plugin.settings().winSound());
        plugin.getLogger().info(player.getName() + " won " + round.type().id() + " in " + Times.seconds(round.elapsed()));
    }

    private void unpaid(Round round, UUID playerId, String name) {
        active.compareAndSet(round, null);
        cancelTimeout();
        plugin.stats().log(round.type(), playerId, name, round.elapsed(), "winner-left");
        plugin.broadcaster().toReceivers("winner-left", tokens(round, null, round.elapsed()), null);
        plugin.getLogger().info((name == null ? "Winner" : name) + " left before the reward for " + round.type().id());
    }

    private boolean start(CommandSender sender, GameType type) {
        if (active.get() != null) {
            return false;
        }
        GameDef def = plugin.settings().game(type);
        if (def == null || !def.enabled()) {
            return false;
        }
        Round round = RoundFactory.create(
                ids.incrementAndGet(),
                def,
                plugin.settings().words(),
                plugin.settings().trivia(),
                ThreadLocalRandom.current(),
                System.currentTimeMillis());
        if (round == null) {
            if (sender != null) {
                plugin.broadcaster().toSender(sender, "no-content", labels(type));
            }
            return true;
        }
        if (!active.compareAndSet(null, round)) {
            return false;
        }
        plugin.stats().markStarted(type);
        plugin.stats().log(type, null, null, null, "start");
        armTimeout(round);
        Map<String, String> values = tokens(round, null, null);
        plugin.broadcaster().announceStart(round, values);
        plugin.getLogger().info("Started " + type.id());
        if (sender != null && (!(sender instanceof Player player) || !plugin.onlinePlayers().receives(player))) {
            plugin.broadcaster().toSender(sender, "started", values);
        }
        return true;
    }

    private GameType pick(Random random, boolean ignoreWeight) {
        List<GameDef> ready = new ArrayList<>();
        int total = 0;
        for (GameDef def : plugin.settings().games()) {
            if (!available(def, ignoreWeight)) {
                continue;
            }
            ready.add(def);
            total += ignoreWeight ? 1 : def.weight();
        }
        if (ready.isEmpty() || total <= 0) {
            return null;
        }
        int roll = random.nextInt(total);
        int cursor = 0;
        for (GameDef def : ready) {
            cursor += ignoreWeight ? 1 : def.weight();
            if (roll < cursor) {
                return def.type();
            }
        }
        return ready.get(0).type();
    }

    private boolean available(GameDef def, boolean ignoreWeight) {
        if (def == null || !def.enabled()) {
            return false;
        }
        if (!ignoreWeight && def.weight() <= 0) {
            return false;
        }
        return switch (def.type()) {
            case TRIVIA -> !plugin.settings().trivia().isEmpty();
            case GUESS_NUMBER, MATH -> true;
            default -> !plugin.settings().words().isEmpty();
        };
    }

    private void armTimeout(Round round) {
        long ticks = plugin.settings().timeoutSeconds() * 20L;
        ScheduledTask task = Bukkit.getGlobalRegionScheduler().runDelayed(
                plugin, scheduled -> expire(round.id()), ticks);
        ScheduledTask previous = timeout.getAndSet(task);
        if (previous != null) {
            previous.cancel();
        }
    }

    private void cancelTimeout() {
        ScheduledTask task = timeout.getAndSet(null);
        if (task != null) {
            task.cancel();
        }
    }

    private boolean duplicate(UUID playerId, String text) {
        long now = System.nanoTime();
        Recent previous = recent.put(playerId, new Recent(text, now));
        return previous != null && previous.text.equals(text) && now - previous.nano < 200_000_000L;
    }

    private Map<String, String> tokens(Round round, Player player, Long elapsed) {
        Map<String, String> values = new HashMap<>();
        values.put("type", round.type().id());
        values.put("type-label", round.type().label());
        values.put("prompt", round.prompt());
        values.put("hover", round.hover().replace("\"", "'"));
        values.put("answer", round.reveal());
        values.put("player", player == null ? "" : player.getName());
        values.put("time", elapsed == null ? "" : Times.seconds(elapsed));
        values.put("min", Integer.toString(round.def().min()));
        values.put("max", Integer.toString(round.def().max()));
        return values;
    }

    private static Map<String, String> labels(GameType type) {
        return Map.of("type", type.id(), "type-label", type.label());
    }

    public static final class Times {
        private Times() {
        }

        public static String seconds(long millis) {
            return String.format(Locale.US, "%.2fs", Math.max(0L, millis) / 1000.0);
        }

        public static String clock(long epochMillis) {
            return java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")
                    .withZone(java.time.ZoneId.systemDefault())
                    .format(java.time.Instant.ofEpochMilli(epochMillis));
        }
    }

    private record Recent(String text, long nano) {
    }
}
