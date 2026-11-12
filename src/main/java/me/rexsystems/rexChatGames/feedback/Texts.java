package me.rexsystems.rexChatGames.feedback;

import me.rexsystems.rexChatGames.util.Placeholders;
import me.rexsystems.rexChatGames.util.TextParser;
import net.kyori.adventure.text.Component;
import org.bukkit.entity.Player;

import java.util.Map;

public final class Texts {

    private Texts() {
    }

    public static Component render(Player player, String template, String prefix, Map<String, String> values) {
        String text = template == null ? "" : template;
        String safePrefix = prefix == null ? "" : prefix;
        text = text.replace("{prefix}", safePrefix).replace("<prefix>", safePrefix);
        text = TextParser.apply(text, values);
        if (player != null) {
            text = Placeholders.apply(player, text);
        }
        return TextParser.parse(text);
    }
}
