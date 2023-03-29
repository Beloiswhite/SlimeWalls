package net.beloiswhite;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.block.Sign;
import org.bukkit.command.*;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.util.StringUtil;

import java.util.*;

// Main class. Listening events
public final class slimewalls extends JavaPlugin implements Listener {

    static int wall_steps;
    static int wall_delay;
    static String lobby_world;
    List<String> listWalls;
    static String[] stringWalls;


    @Override
    public void onEnable() {
        saveDefaultConfig();
        reloadConfigValues(); // Update const config values

        lobby.onEnableSignSetup(); // Set meta for all plugin signs in world

        this.getServer().getPluginManager().registerEvents(this, this);
        this.getCommand("slimewalls").setExecutor(new cmdListener());
        this.getCommand("slimewalls").setTabCompleter(new cmdComplete());
        //this.getCommand("test").setTabCompleter(new tabCompleter());

    }

    // Callable plugin value
    public static Plugin plugin() {
        return getPlugin(slimewalls.class);
    }

    public void reloadConfigValues() {

        reloadConfig();

        wall_steps = getConfig().getInt("wall-steps");
        wall_delay = getConfig().getInt("wall-delay");
        lobby_world = getConfig().getString("lobby-world");
        listWalls = getConfig().getStringList("walls");
        stringWalls = listWalls.toArray(new String[0]);
    }

    public static String[] getWallsMaterials(){
        return stringWalls;
    }

    public class cmdListener implements CommandExecutor {

        @Override
        public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {

            if (command.getName().equalsIgnoreCase("slimewalls")) {

                // reload config
                if (args[0].equalsIgnoreCase("reload")) {
                    reloadConfigValues();
                    sender.sendMessage(ChatColor.GREEN + "[SlimeWalls] Config Reloaded.");
                    return true;
                }
                else if (args[0].equalsIgnoreCase("arena") && args[1].isEmpty()) sender.sendMessage(ChatColor.GREEN + "[SlimeWalls] Parameters:\nworld, deathY, gameTime, centerArena, N/S/W/E, spawn, min, max");
                else if (args[0].equalsIgnoreCase("arena") && args[1].equalsIgnoreCase("setup") && !args[2].isEmpty()) arena.arenaSetup((Player) sender, args);
                else if (args[0].equalsIgnoreCase("arena") && args[1].equalsIgnoreCase("run") && !args[2].isEmpty()) arena.arenaRun(getConfig().getConfigurationSection("arena." + args[2]));
                else if (args[0].equalsIgnoreCase("wall") && args[1].equalsIgnoreCase("setup")) wall.wallSetup((Player) sender, args);
                else if (args[0].equalsIgnoreCase("lobby") && !args[1].isEmpty() && !args[2].isEmpty()) lobby.toLobbyTeleport(Bukkit.getPlayer(args[1]), args[2]);
                else return false;

                return true;
            }

            return false;
        }
    }

    public class cmdComplete implements TabExecutor {

        @Override
        public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
            return false;
        }

        @Override
        public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
            List<String> main = Arrays.asList("reload", "arena", "wall", "lobby");
            List<String> wall = Arrays.asList("pos1", "pos2", "save");
            List<String> arena = Arrays.asList("world", "deathY", "gameTime", "centerArena", "north", "south", "west", "east", "spawn", "min", "max", "save");
            List<String> completions = null;

            for(String s : main){
                if(s.startsWith(args[0].toLowerCase())){

                    if (completions == null){
                        completions = new ArrayList();
                    }

                    completions.add(s);
                }
            }

            switch (args[0].toLowerCase()) {
                case "wall":
                    for (String s1 : wall) completions.add(s1);
                    break;
                case "arena":
                    for (String s1 : arena) completions.add(s1);
                    break;
            }

            if (completions != null)
                Collections.sort(completions);

            return completions;
        }
    }

    // TODO: 1.Remove
    @EventHandler
    // Create or Use SlimeWalls lobby sign
    public void onSignInteract(PlayerInteractEvent e) {
        if (e.getClickedBlock() == null) return;
        if (e.getAction().equals(Action.RIGHT_CLICK_BLOCK) && e.getClickedBlock().getState() instanceof Sign) {
            // If meta of sign is null
            if (!e.getClickedBlock().hasMetadata("SlimeWalls")) {
                lobby.signSetup(e.getClickedBlock());
            }
            // If meta of sign is already be
            else if (e.getClickedBlock().hasMetadata("SlimeWalls")) {
                lobby.signRClick(e.getPlayer(), e.getClickedBlock());
            }
        }
    }

    // TODO: 1.Remove
    @EventHandler
    // Remove SlimeWalls sign when it destroys
    public void onSignDestroy(BlockBreakEvent e) {
        if (e.getBlock().hasMetadata("SlimeWalls")) {
            // remove plugin meta from sign
            e.getBlock().removeMetadata("SlimeWalls", this);
        }
    }

    @EventHandler
    public void playerLoseOnArena(PlayerMoveEvent e) {

        if (!e.getPlayer().hasMetadata("SlimeWalls")) return; // if player has not SlimeWalls meta
        int death_y = getConfig().getConfigurationSection("arena." + e.getPlayer().getMetadata("SlimeWalls").get(0).asString()).getInt("deathY");

        if (!(e.getPlayer().getLocation().getY() < death_y && e.getPlayer().getGameMode().equals(GameMode.ADVENTURE))) return; // if player moves at normally height

        e.getPlayer().sendTitle(ChatColor.DARK_RED + ChatColor.BOLD.toString() + "Поражение", ChatColor.RED + "SlimeWalls", 5,20,5);
        e.getPlayer().setGameMode(GameMode.SPECTATOR);

        int count = 0;
        for (Player p :Bukkit.getOnlinePlayers()) {
            if (!p.getMetadata("SlimeWalls").get(0).asString().equals(e.getPlayer().getMetadata("SlimeWalls").get(0).asString())) continue;
            if (!p.getGameMode().equals(GameMode.ADVENTURE)) continue;
            count++;
        }
        if (count <= 1) arena.arenaClose(getConfig().getConfigurationSection("arena." + e.getPlayer().getMetadata("SlimeWalls").get(0).asString()));
    }

}
