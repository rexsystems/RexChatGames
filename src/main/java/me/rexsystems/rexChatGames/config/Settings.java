package me.rexsystems.rexChatGames.config;

import me.rexsystems.rexChatGames.RexChatGames;
import me.rexsystems.rexChatGames.game.GameType;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

public final class Settings {

    public record SoundSetting(boolean enabled, Sound sound, float volume, float pitch) {
    }

    public record TitleSetting(boolean enabled, String title, String subtitle, int fadeIn, int stay, int fadeOut) {
    }

    private final RexChatGames plugin;
    private int intervalSeconds = 90;
    private int timeoutSeconds = 45;
    private int minPlayers = 1;
    private boolean caseSensitive;
    private boolean warnOnCapitalization = true;
    private Set<String> disabledWorlds = Set.of();
    private SoundSetting startSound = new SoundSetting(false, null, 1f, 1f);
    private SoundSetting winSound = new SoundSetting(false, null, 1f, 1f);
    private TitleSetting title = new TitleSetting(false, "", "", 5, 40, 10);
    private boolean actionBarEnabled;
    private String actionBarText = "";
    private String prefix = "";
    private Map<String, String> messages = Map.of();
    private List<String> help = List.of();
    private Map<GameType, GameDef> games = Map.of();
    private WordList words = new WordList(List.of(), List.of());
    private List<TriviaQuestion> trivia = List.of();

    public Settings(RexChatGames plugin) {
        this.plugin = plugin;
    }

    public void reload() {
        plugin.saveDefaultConfig();
        plugin.reloadConfig();
        new ConfigAutoUpdater(plugin).update();
        plugin.reloadConfig();
        ensureMissing("words.yml");
        ensureMissing("trivia.yml");

        FileConfiguration config = plugin.getConfig();
        intervalSeconds = Math.max(10, config.getInt("settings.interval-seconds", 90));
        timeoutSeconds = Math.max(15, config.getInt("settings.timeout-seconds", 45));
        minPlayers = Math.max(0, config.getInt("settings.min-players", 1));
        caseSensitive = config.getBoolean("settings.case-sensitive", false);
        warnOnCapitalization = config.getBoolean("settings.warn-on-capitalization", true);
        disabledWorlds = lowerSet(config.getStringList("worlds.disabled"));
        startSound = sound(config, "sounds.start");
        winSound = sound(config, "sounds.win");
        title = new TitleSetting(
                config.getBoolean("title.enabled", false),
                config.getString("title.title", ""),
                config.getString("title.subtitle", ""),
                clampTick(config.getInt("title.fade-in", 5)),
                clampTick(config.getInt("title.stay", 40)),
                clampTick(config.getInt("title.fade-out", 10)));
        actionBarEnabled = config.getBoolean("actionbar.enabled", false);
        actionBarText = config.getString("actionbar.text", "");
        prefix = config.getString("messages.prefix", "");
        messages = readMessages(config);
        help = List.copyOf(config.getStringList("messages.help"));
        games = readGames(config);
        words = new WordList(readWords(), WordList.materialNames(Material.values()));
        trivia = readTrivia();
        plugin.getLogger().info("Loaded " + words.size() + " words and " + trivia.size() + " trivia questions.");
    }

    public int intervalSeconds() {
        return intervalSeconds;
    }

    public int timeoutSeconds() {
        return timeoutSeconds;
    }

    public int minPlayers() {
        return minPlayers;
    }

    public boolean caseSensitive() {
        return caseSensitive;
    }

    public boolean warnOnCapitalization() {
        return warnOnCapitalization;
    }

    public boolean worldAllowed(String world) {
        if (world == null) {
            return false;
        }
        return !disabledWorlds.contains(world.toLowerCase(Locale.ROOT));
    }

    public SoundSetting startSound() {
        return startSound;
    }

    public SoundSetting winSound() {
        return winSound;
    }

    public TitleSetting title() {
        return title;
    }

    public boolean actionBarEnabled() {
        return actionBarEnabled;
    }

    public String actionBarText() {
        return actionBarText == null ? "" : actionBarText;
    }

    public String prefix() {
        return prefix == null ? "" : prefix;
    }

    public String message(String key) {
        String value = messages.get(key);
        return value == null ? "" : value;
    }

    public List<String> help() {
        return help;
    }

    public GameDef game(GameType type) {
        return games.get(type);
    }

    public List<GameDef> games() {
        return List.copyOf(games.values());
    }

    public WordList words() {
        return words;
    }

    public List<TriviaQuestion> trivia() {
        return trivia;
    }

    private void ensureMissing(String name) {
        File file = new File(plugin.getDataFolder(), name);
        if (file.exists()) {
            return;
        }
        plugin.saveResource(name, false);
    }

    private Map<GameType, GameDef> readGames(FileConfiguration config) {
        EnumMap<GameType, GameDef> loaded = new EnumMap<>(GameType.class);
        for (GameType type : GameType.values()) {
            String path = "games." + type.id();
            ConfigurationSection section = config.getConfigurationSection(path);
            boolean enabled = config.getBoolean(path + ".enabled", true);
            int weight = config.getInt(path + ".weight", 10);
            int hide = config.getInt(path + ".hide-percent", 40);
            int min = config.getInt(path + ".min", 1);
            int max = config.getInt(path + ".max", type == GameType.GUESS_NUMBER ? 100 : 30);
            List<String> operations = config.getStringList(path + ".operations");
            if (operations.isEmpty() && type == GameType.MATH) {
                operations = List.of("add", "subtract", "multiply", "divide");
            }
            String prompt = config.getString(path + ".prompt", defaultPrompt(type));
            String hover = config.getString(path + ".hover", defaultHover(type));
            List<String> commands = List.of();
            List<GameDef.ItemReward> items = List.of();
            if (section != null) {
                commands = section.getStringList("rewards.commands");
                items = readItems(section);
            }
            loaded.put(type, new GameDef(type, enabled, weight, hide, min, max, operations, prompt, hover, commands, items));
        }
        return loaded;
    }

    private List<GameDef.ItemReward> readItems(ConfigurationSection section) {
        List<Map<?, ?>> maps = section.getMapList("rewards.items");
        List<GameDef.ItemReward> items = new ArrayList<>();
        for (Map<?, ?> map : maps) {
            Object materialRaw = map.get("material");
            if (materialRaw == null) {
                continue;
            }
            Material material = Material.matchMaterial(materialRaw.toString());
            if (material == null || material.isAir()) {
                plugin.getLogger().warning("Unknown reward material: " + materialRaw);
                continue;
            }
            int amount = 1;
            Object amountRaw = map.get("amount");
            if (amountRaw instanceof Number number) {
                amount = number.intValue();
            }
            amount = Math.max(1, Math.min(576, amount));
            Object nameRaw = map.get("name");
            String name = nameRaw == null ? null : nameRaw.toString();
            items.add(new GameDef.ItemReward(material, amount, name));
        }
        return items;
    }

    private List<String> readWords() {
        File file = new File(plugin.getDataFolder(), "words.yml");
        YamlConfiguration yaml = YamlConfiguration.loadConfiguration(file);
        return yaml.getStringList("words");
    }

    private List<TriviaQuestion> readTrivia() {
        File file = new File(plugin.getDataFolder(), "trivia.yml");
        YamlConfiguration yaml = YamlConfiguration.loadConfiguration(file);
        List<TriviaQuestion> questions = new ArrayList<>();
        for (Map<?, ?> map : yaml.getMapList("questions")) {
            Object questionRaw = map.get("question");
            if (questionRaw == null || questionRaw.toString().isBlank()) {
                continue;
            }
            List<String> answers = new ArrayList<>();
            Object single = map.get("answer");
            if (single != null && !single.toString().isBlank()) {
                answers.add(single.toString().trim());
            }
            Object many = map.get("answers");
            if (many instanceof List<?> list) {
                for (Object item : list) {
                    if (item != null && !item.toString().isBlank()) {
                        answers.add(item.toString().trim());
                    }
                }
            }
            if (answers.isEmpty()) {
                continue;
            }
            questions.add(new TriviaQuestion(questionRaw.toString().trim(), answers));
        }
        return List.copyOf(questions);
    }

    private SoundSetting sound(FileConfiguration config, String path) {
        boolean enabled = config.getBoolean(path + ".enabled", false);
        String name = config.getString(path + ".sound", "");
        Sound sound = null;
        if (name != null && !name.isBlank()) {
            try {
                sound = Sound.valueOf(name.trim().toUpperCase(Locale.ROOT));
            } catch (IllegalArgumentException ex) {
                plugin.getLogger().warning("Unknown sound " + name + " at " + path);
                enabled = false;
            }
        } else {
            enabled = false;
        }
        float volume = (float) config.getDouble(path + ".volume", 1.0);
        float pitch = (float) config.getDouble(path + ".pitch", 1.0);
        return new SoundSetting(enabled && sound != null, sound, volume, pitch);
    }

    private static Map<String, String> readMessages(FileConfiguration config) {
        ConfigurationSection section = config.getConfigurationSection("messages");
        Map<String, String> values = new java.util.LinkedHashMap<>();
        if (section == null) {
            return values;
        }
        for (String key : section.getKeys(false)) {
            if (section.isString(key)) {
                values.put(key, section.getString(key, ""));
            }
        }
        return values;
    }

    private static Set<String> lowerSet(List<String> names) {
        Set<String> worlds = new HashSet<>();
        if (names == null) {
            return worlds;
        }
        for (String name : names) {
            if (name != null && !name.isBlank()) {
                worlds.add(name.toLowerCase(Locale.ROOT));
            }
        }
        return worlds;
    }

    private static int clampTick(int ticks) {
        return Math.max(0, Math.min(200, ticks));
    }

    private static String defaultPrompt(GameType type) {
        return switch (type) {
            case COMPLETE -> "{masked}";
            case GUESS_NUMBER -> "Guess a whole number from {min} to {max}.";
            case MATH -> "What is {expression}?";
            case TRIVIA -> "{question}";
            case TYPE -> "Type this: {word}";
            case REVERSE -> "Reverse this: {reversed}";
            case UNSCRAMBLE -> "Unscramble: {scrambled}";
        };
    }

    private static String defaultHover(GameType type) {
        return switch (type) {
            case COMPLETE -> "Type the full phrase. Underscores are hidden letters.";
            case GUESS_NUMBER -> "Type only the number.";
            case MATH -> "Type the whole number. Division stays a whole number.";
            case TRIVIA -> "Type the answer by itself.";
            case TYPE -> "Type the phrase exactly.";
            case REVERSE -> "Type the phrase forwards.";
            case UNSCRAMBLE -> "Each word is scrambled on its own. Spaces stay put.";
        };
    }
}
