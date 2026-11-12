package me.rexsystems.rexChatGames.reward;

import me.rexsystems.rexChatGames.RexChatGames;
import me.rexsystems.rexChatGames.config.GameDef;
import me.rexsystems.rexChatGames.util.TextParser;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.Map;

public final class RewardGiver {

    private RewardGiver() {
    }

    public static void give(RexChatGames plugin, Player player, GameDef def) {
        for (String command : def.commands()) {
            runCommand(plugin, player, command);
        }
        for (GameDef.ItemReward reward : def.items()) {
            giveItem(player, reward);
        }
    }

    private static void runCommand(RexChatGames plugin, Player player, String command) {
        if (command == null || command.isBlank()) {
            return;
        }
        String parsed = command.trim();
        if (parsed.startsWith("/")) {
            parsed = parsed.substring(1);
        }
        parsed = parsed.replace("{player}", player.getName())
                .replace("{uuid}", player.getUniqueId().toString());
        try {
            Bukkit.dispatchCommand(Bukkit.getConsoleSender(), parsed);
        } catch (Throwable failure) {
            plugin.getLogger().warning("Reward command failed: " + failure.getMessage());
        }
    }

    private static void giveItem(Player player, GameDef.ItemReward reward) {
        if (reward == null || reward.material() == null || reward.material().isAir()) {
            return;
        }
        int left = Math.max(1, reward.amount());
        int max = Math.max(1, reward.material().getMaxStackSize());
        while (left > 0) {
            int size = Math.min(max, left);
            ItemStack stack = new ItemStack(reward.material(), size);
            if (reward.name() != null && !reward.name().isBlank()) {
                ItemMeta meta = stack.getItemMeta();
                meta.displayName(TextParser.parse(reward.name()));
                stack.setItemMeta(meta);
            }
            Map<Integer, ItemStack> extra = player.getInventory().addItem(stack);
            for (ItemStack overflow : extra.values()) {
                player.getWorld().dropItemNaturally(player.getLocation(), overflow);
            }
            left -= size;
        }
    }
}
