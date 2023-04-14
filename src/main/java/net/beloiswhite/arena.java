package net.beloiswhite;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Random;

import static org.apache.commons.lang.math.RandomUtils.nextInt;

public class arena {

    static Plugin plugin = slimewalls.plugin(); // Get plugin

    static HashMap<Player, String> arenaWorld = new HashMap<>();
    static HashMap<String, Boolean> isRunning = new HashMap<>();  // If arena hasn't any player - stop
    static HashMap<Player, Integer> deathY = new HashMap<>();  // Arena's Y coordinate. Switching player to spectator when reaches this point.
    static HashMap<Player, Integer> gameTime = new HashMap<>();  // Time limit on arena.
    static HashMap<Player, Location> centerArena = new HashMap<>();  // Center of arena(Place where players spawn at game starting)

    // Size of arena (Players count)
    static HashMap<Player, Integer> minPlayers = new HashMap<>();
    static HashMap<Player, Integer> maxPlayers = new HashMap<>();

    //wall's spawn-points.
    static HashMap<Player, Location> locNorth = new HashMap<>();
    static HashMap<Player, Location> locSouth = new HashMap<>();
    static HashMap<Player, Location> locEast = new HashMap<>();
    static HashMap<Player, Location> locWest = new HashMap<>();
    static HashMap<Player, Location> locSpawn = new HashMap<>();

    public static void arenaRun(ConfigurationSection arena) {
        for (Player player : Bukkit.getOnlinePlayers()) {
            if (player.getLocation().getWorld().getName().equals(arena.getString("world"))) player.teleport(arena.getLocation("centerArena"));
        }

        String[] facing_list = {"north", "south", "west", "east"};
        isRunning.put(arena.getName(), true);  // Arena will spawn walls

        for (int i = 0; i < (arena.getInt("gameTime")*1200)/(slimewalls.wall_delay * slimewalls.wall_steps); i++) {

            // Stop arena, or continue
            if (!isRunning.get(arena.getName())) {
                return;
            }

            String facing = facing_list[new Random().nextInt(facing_list.length)];
            Bukkit.getScheduler().runTaskLater(plugin, new Runnable() {
                public void run() {
                    wall.wallController(arena.getInt(facing + "X"), arena.getInt(facing + "Y"), arena.getInt(facing + "Z"), Bukkit.getWorld(arena.getString("world")), facing);
                }
            }, (long) slimewalls.wall_delay * slimewalls.wall_steps * i);
        }

        // TODO: Check player meta(arena name) and disable spectator/award
        Bukkit.getScheduler().runTaskLater(plugin, new Runnable() {
            public void run() {
                arenaClose(arena);
            }
        }, (arena.getInt("gameTime") * 1200L) + 60L);


    }

    // Award winners, clear arena etc.
    public static void arenaClose(ConfigurationSection arena) {
        for (Player player : Bukkit.getOnlinePlayers()) {
            if (!player.getMetadata("SlimeWalls").get(0).asString().equals(arena.getName())) continue;
            player.sendMessage(ChatColor.GREEN + "[SlimeWalls] " + ChatColor.GRAY + "Раунд окончен!");
            List<String> winners = new ArrayList<>();

            if (!player.getGameMode().equals(GameMode.SPECTATOR)) {
                winners.add(player.getDisplayName());
                player.sendTitle(ChatColor.DARK_GREEN + ChatColor.BOLD.toString() + "Победа", ChatColor.GREEN + "SlimeWalls", 5,20,5);
            }

            Bukkit.getScheduler().runTaskLater(plugin, new Runnable() {
                public void run() {
                    player.teleport(Bukkit.getWorld(slimewalls.lobby_world).getSpawnLocation()); // teleport player into the lobby world
                    player.setGameMode(GameMode.SURVIVAL);
                    player.sendMessage(ChatColor.GREEN + "[SLimeWalls] Победители: ");
                    for (String s : winners) {
                        player.sendMessage(ChatColor.DARK_GREEN + s);
                    }
                    player.removeMetadata("SlimeWalls", plugin);
                    isRunning.put(arena.getName(), false);  // Arena will not spawn walls
                }
            }, 100L);

        }
    }

    // Listening commands
    public static void arenaSetup(Player player, String[] args) {
        // Setting up arena parameters.
        if (!args[2].equalsIgnoreCase("save")) {

            if (args[2].equalsIgnoreCase("world")) arenaWorld.put(player, player.getWorld().getName());
            else if (args[2].equalsIgnoreCase("deathY") && !args[3].isEmpty()) deathY.put(player, Integer.valueOf(args[3]));
            else if (args[2].equalsIgnoreCase("gameTime") && !args[3].isEmpty()) gameTime.put(player, Integer.valueOf(args[3]));
            else if (args[2].equalsIgnoreCase("centerArena")) centerArena.put(player, player.getLocation());
            else if (args[2].equalsIgnoreCase("north")) locNorth.put(player, player.getLocation());
            else if (args[2].equalsIgnoreCase("south")) locSouth.put(player, player.getLocation());
            else if (args[2].equalsIgnoreCase("east")) locEast.put(player, player.getLocation());
            else if (args[2].equalsIgnoreCase("west")) locWest.put(player, player.getLocation());
            else if (args[2].equalsIgnoreCase("spawn")) locSpawn.put(player, player.getLocation());
            else if (args[2].equalsIgnoreCase("min")) minPlayers.put(player, Integer.valueOf(args[3]));
            else if (args[2].equalsIgnoreCase("max")) maxPlayers.put(player, Integer.valueOf(args[3]));

            player.sendMessage(ChatColor.GREEN + "[SlimeWalls] Parameter " + ChatColor.DARK_GREEN+args[2] + ChatColor.GREEN + " has been added into buffer.");
        }
        // Saving arena.
        else {
            if (arenaWorld.get(player) != null && deathY.get(player) != null && gameTime.get(player) != null &&
                    locNorth.get(player) != null && locSouth.get(player) != null && locEast.get(player) != null && locWest.get(player) != null) {

                int count = arenaCount();

                // setup arena in config
                plugin.getConfig().set("arena" + ".arena" + count + ".world", arenaWorld.get(player));
                plugin.getConfig().set("arena" + ".arena" + count + ".deathY", deathY.get(player));
                plugin.getConfig().set("arena" + ".arena" + count + ".gameTime", gameTime.get(player));
                plugin.getConfig().set("arena" + ".arena" + count + ".centerArena", centerArena.get(player));
                plugin.getConfig().set("arena" + ".arena" + count + ".minPlayers", minPlayers.get(player));
                plugin.getConfig().set("arena" + ".arena" + count + ".maxPlayers", maxPlayers.get(player));

                // setup players spawn-point in arena's config
                plugin.getConfig().set("arena" + ".arena" + count + ".spawn", locSpawn.get(player));

                // setup arena XYZ faces in config
                // NORTH
                plugin.getConfig().set("arena" + ".arena" + count + ".northX", locNorth.get(player).getBlockX());
                plugin.getConfig().set("arena" + ".arena" + count + ".northY", locNorth.get(player).getBlockY());
                plugin.getConfig().set("arena" + ".arena" + count + ".northZ", locNorth.get(player).getBlockZ());

                // SOUTH
                plugin.getConfig().set("arena" + ".arena" + count + ".southX", locSouth.get(player).getBlockX());
                plugin.getConfig().set("arena" + ".arena" + count + ".southY", locSouth.get(player).getBlockY());
                plugin.getConfig().set("arena" + ".arena" + count + ".southZ", locSouth.get(player).getBlockZ());

                // WEST
                plugin.getConfig().set("arena" + ".arena" + count + ".westX", locWest.get(player).getBlockX());
                plugin.getConfig().set("arena" + ".arena" + count + ".westY", locWest.get(player).getBlockY());
                plugin.getConfig().set("arena" + ".arena" + count + ".westZ", locWest.get(player).getBlockZ());

                // EAST
                plugin.getConfig().set("arena" + ".arena" + count + ".eastX", locEast.get(player).getBlockX());
                plugin.getConfig().set("arena" + ".arena" + count + ".eastY", locEast.get(player).getBlockY());
                plugin.getConfig().set("arena" + ".arena" + count + ".eastZ", locEast.get(player).getBlockZ());

                // Write wall into config
                plugin.saveConfig();

                player.sendMessage(ChatColor.GREEN + "[SlimeWalls] Arena has been saved into config.");
            }
        }
    }

    // Count config arenas. Using in arenaSetup() for create unique name of new arena.
    public static int arenaCount() {
        if (plugin.getConfig().getConfigurationSection("arena") != null) {
            return plugin.getConfig().getConfigurationSection("arena").getKeys(false).size();
        }
        return 0;
    }

}
