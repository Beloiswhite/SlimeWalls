package net.beloiswhite;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.block.Sign;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.plugin.Plugin;

import java.util.HashMap;

public class lobby {

    static HashMap<String, Integer> lobby_player_quantity = new HashMap<>();

    static Plugin plugin = slimewalls.plugin(); // Get plugin

    public static void onEnableSignSetup() {
        int count = signCount();
        for (int i = 0; i < count; i++) {

            if (plugin.getConfig().getConfigurationSection("sign" + ".sign" + i) == null) continue;

            Location location = plugin.getConfig().getLocation("sign" + ".sign" + i + ".location");
            String meta = plugin.getConfig().getString("sign" + ".sign" + i + ".meta");

            // Check block material. If it's sign - set meta, otherwise remove sign from config
            if (location.getBlock().getState() instanceof Sign) location.getBlock().setMetadata("SlimeWalls", new FixedMetadataValue(plugin, meta));
            else plugin.getConfig().set("sign" + ".sign" + i, null); plugin.saveConfig();
        }
    }
    public static void signSetup(Block block) {
        Sign sign = (Sign) block.getState();
        if (sign.getLine(0).equals("[SlimeWalls]") && !sign.getLine(1).isEmpty()) {

            // Set meta(arena value) to sign
            sign.setMetadata("SlimeWalls", new FixedMetadataValue(plugin, sign.getLine(1)));

            // Write sign data into config
            int count = signCount();
            plugin.getConfig().set("sign" + ".sign" + count + ".location", block.getLocation());
            plugin.getConfig().set("sign" + ".sign" + count + ".meta", sign.getLine(1));

            plugin.saveConfig(); // Write sign into config

            sign.setLine(0, ChatColor.GREEN + "[SlimeWalls]");
            sign.setLine(1, ChatColor.DARK_GREEN + "0 / " + plugin.getConfig().get("arena" + ".arena" + count + ".maxPlayers"));
            sign.setLine(2, ChatColor.DARK_GREEN + "[Ожидание]");
            sign.update(true);
        }
    }

    //TODO: 1.Remove.
    public static void signRClick(Player player, Block block) {
        toLobbyTeleport(player, block.getMetadata("SlimeWalls").get(0).asString());
    }

    public static void toLobbyTeleport(Player player, String string) {
        ConfigurationSection arena = plugin.getConfig().getConfigurationSection("arena." + string);

        // Add player to lobby counter
        int count = 0;
        for (Player p : Bukkit.getOnlinePlayers()) {
            if (!p.hasMetadata("SlimeWalls")) continue;
            count++;
        }

        // Tp player to arena spawn
        if (count <= arena.getInt("maxPlayers")) {
            player.teleport(arena.getLocation("spawn"));
            player.setMetadata("SlimeWalls", new FixedMetadataValue(plugin, string)); // Set meta to player
            player.setGameMode(GameMode.ADVENTURE);
        }

        // Send message to all players in arena
        for (Player p : Bukkit.getOnlinePlayers()) {
            if (!p.getMetadata("SlimeWalls").get(0).asString().equals(string)) continue;
            p.sendMessage(ChatColor.GREEN + "[SlimeWalls] " + ChatColor.RESET + player.getDisplayName() + ChatColor.YELLOW + " присоединился к арене!");
        }

        lobby_player_quantity.put(string, count);
    }

    // Count config signs. Using in signSetup() for create unique name of new sign in world.
    public static int signCount() {
        if (plugin.getConfig().getConfigurationSection("sign") != null) {
            return plugin.getConfig().getConfigurationSection("sign").getKeys(false).size();
        }
        return 0;
    }

}