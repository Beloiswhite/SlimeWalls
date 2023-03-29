package net.beloiswhite;

import org.bukkit.*;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.util.Vector;

import java.util.*;

public class wall {

    static Plugin plugin = slimewalls.plugin(); // Get plugin

    // Positions which used when creating new wall
    static HashMap<Player, Location> pos1 = new HashMap<>();
    static HashMap<Player, Location> pos2 = new HashMap<>();

    // Main wall logic. Spawn wall, control near players, remove and move wall
    public static void wallController(int x, int y, int z, World world, String facing) {

        String wall = slimewalls.getWallsMaterials()[new Random().nextInt(slimewalls.getWallsMaterials().length)];
        List<String> wallList = Arrays.asList(wall.split(","));

        for (int i = 0; i < slimewalls.wall_steps; i++) {

            //Axis movement variable
            int finalPZ_n = z - i;
            int finalPZ_s = z + i;
            int finalPX_e = x + i;
            int finalPX_w = x - i;

            //First delayed task. Spawns wall in coordinates.
            Bukkit.getScheduler().runTaskLater(plugin, new Runnable() {
                public void run() {

                    //Call function which spawning wall from another class "helper"
                    if (facing.equalsIgnoreCase("north")) wallBuilder(x, y, finalPZ_n, world, facing, wallList);
                    else if (facing.equalsIgnoreCase("south")) wallBuilder(x, y, finalPZ_s, world, facing, wallList);
                    else if (facing.equalsIgnoreCase("east")) wallBuilder(finalPX_e, y, z, world, facing, wallList);
                    else if (facing.equalsIgnoreCase("west")) wallBuilder(finalPX_w, y, z, world, facing, wallList);

                    //Second delayed task. Removes wall in 1sec after spawn(replacing every block by AIR)
                    Bukkit.getScheduler().runTaskLater(plugin, new Runnable() {
                        public void run() {
                            if (facing.equalsIgnoreCase("north")) {
                                playerWallPush(x, y, finalPZ_n, world, facing);
                                wallBreaker(x, y, finalPZ_n, world, facing);
                            } else if (facing.equalsIgnoreCase("south")) {
                                playerWallPush(x, y, finalPZ_s, world, facing);
                                wallBreaker(x, y, finalPZ_s, world, facing);
                            } else if (facing.equalsIgnoreCase("east")) {
                                playerWallPush(finalPX_e, y, z, world, facing);
                                wallBreaker(finalPX_e, y, z, world, facing);
                            } else if (facing.equalsIgnoreCase("west")) {
                                playerWallPush(finalPX_w, y, z, world, facing);
                                wallBreaker(finalPX_w, y, z, world, facing);
                            }
                        }
                    }, slimewalls.wall_delay);
                }
            }, slimewalls.wall_delay * i);
        }
    }

    // Build wall function
    public static void wallBuilder(int x, int y, int z, World world, String facing, List<String> wallList) {
        int block_n = 0;

        // Check wall facing & build (SOUTH / NORTH)
        if (facing.equalsIgnoreCase("south") || facing.equalsIgnoreCase("north")) {
            for (int rows = 0; rows < 5; rows++) {
                for (int col = 0; col < 11; col++) {
                    world.getBlockAt(x + 5 - col, y + 4 - rows, z).setType(Material.valueOf(wallList.get(block_n)));

                    block_n++;
                }
            }
        }
        // Check wall facing & build (EAST / WEST)
        else if (facing.equalsIgnoreCase("east") || facing.equalsIgnoreCase("west")) {
            for (int rows = 0; rows < 5; rows++) {
                for (int col = 0; col < 11; col++) {
                    world.getBlockAt(x, y + 4 - rows, z + 5 - col).setType(Material.valueOf(wallList.get(block_n)));

                    block_n++;
                }
            }
        }
    }

    // Break wall function
    public static void wallBreaker(int x, int y, int z, World world, String facing) {

        int block_n = 0;

        if (facing.equalsIgnoreCase("south") || facing.equalsIgnoreCase("north")) {
            for (int rows = 0; rows < 5; rows++) {
                for (int col = 0; col < 11; col++) {
                    world.getBlockAt(x+5-col, y+4-rows, z).setType(Material.AIR);

                    block_n++;
                }
            }
        }
        else if (facing.equalsIgnoreCase("east") || facing.equalsIgnoreCase("west")) {
            for (int rows = 0; rows < 5; rows++) {
                for (int col = 0; col < 11; col++) {
                    world.getBlockAt(x, y+4-rows, z+5-col).setType(Material.AIR);

                    block_n++;
                }
            }
        }


    }

    // Move player far from wall way
    public static void playerWallPush(int x, int y, int z, World world, String facing) {
        if (facing.equalsIgnoreCase("north")){
            Collection<Entity> nearbyEntites = world.getNearbyEntities(world.getBlockAt(x, y, z).getLocation(), 6.0, 2.0, 1.0);
            for (Entity p : nearbyEntites) {
                if ( !((Player) p).getGameMode().equals(GameMode.ADVENTURE) ) return;
                if (!p.getWorld().getBlockAt(p.getLocation().getBlockX(), p.getLocation().getBlockY(), p.getLocation().getBlockZ() + 1).getType().equals(Material.AIR) ||
                        !p.getWorld().getBlockAt(p.getLocation().getBlockX(), p.getLocation().getBlockY()+1, p.getLocation().getBlockZ() + 1).getType().equals(Material.AIR)) {

                    //setup new player location
                    Location newPLoc = new Location(p.getWorld(), p.getLocation().getX(), p.getLocation().getY(), p.getLocation().getZ() - 1);
                    newPLoc.setPitch(p.getLocation().getPitch()); newPLoc.setYaw(p.getLocation().getYaw());
                    p.teleport(newPLoc);
                    p.setVelocity(new Vector(0.0, 0.2, -0.2));
                }
            }
        }
        else if (facing.equalsIgnoreCase("south")) {
            Collection<Entity> nearbyEntites = world.getNearbyEntities(world.getBlockAt(x, y, z).getLocation(), 6.0, 2.0, 1.5);
            for (Entity p : nearbyEntites) {
                if ( !((Player) p).getGameMode().equals(GameMode.ADVENTURE) ) return;
                if (!p.getWorld().getBlockAt(p.getLocation().getBlockX(), p.getLocation().getBlockY(), p.getLocation().getBlockZ() - 1).getType().equals(Material.AIR) ||
                        !p.getWorld().getBlockAt(p.getLocation().getBlockX(), p.getLocation().getBlockY()+1, p.getLocation().getBlockZ() - 1).getType().equals(Material.AIR)) {
                    //setup new player location
                    Location newPLoc = new Location(p.getWorld(), p.getLocation().getX(), p.getLocation().getY(), p.getLocation().getZ() + 1);
                    newPLoc.setPitch(p.getLocation().getPitch()); newPLoc.setYaw(p.getLocation().getYaw());
                    p.teleport(newPLoc);
                    p.setVelocity(new Vector(0.0, 0.2, 0.2));
                }
            }
        }
        else if (facing.equalsIgnoreCase("west")) {
            Collection<Entity> nearbyEntites = world.getNearbyEntities(world.getBlockAt(x, y, z).getLocation(), 1.5, 2.0, 6.0);
            for (Entity p : nearbyEntites) {
                if ( !((Player) p).getGameMode().equals(GameMode.ADVENTURE) ) return;
                if (!p.getWorld().getBlockAt(p.getLocation().getBlockX() + 1, p.getLocation().getBlockY(), p.getLocation().getBlockZ()).getType().equals(Material.AIR) ||
                        !p.getWorld().getBlockAt(p.getLocation().getBlockX() + 1, p.getLocation().getBlockY()+1, p.getLocation().getBlockZ()).getType().equals(Material.AIR)) {

                    //setup new player location
                    Location newPLoc = new Location(p.getWorld(), p.getLocation().getX() - 1, p.getLocation().getY(), p.getLocation().getZ());
                    newPLoc.setPitch(p.getLocation().getPitch()); newPLoc.setYaw(p.getLocation().getYaw());
                    p.teleport(newPLoc);
                    p.setVelocity(new Vector(-0.2, 0.2, 0.0));
                }
            }
        }
        else if (facing.equalsIgnoreCase("east")) {
            Collection<Entity> nearbyEntites = world.getNearbyEntities(world.getBlockAt(x, y, z).getLocation(), 2.0, 2.0, 6.0);
            for (Entity p : nearbyEntites) {
                if ( !((Player) p).getGameMode().equals(GameMode.ADVENTURE) ) return;
                if (!p.getWorld().getBlockAt(p.getLocation().getBlockX() - 1, p.getLocation().getBlockY(), p.getLocation().getBlockZ()).getType().equals(Material.AIR) ||
                        !p.getWorld().getBlockAt(p.getLocation().getBlockX() - 1, p.getLocation().getBlockY()+1, p.getLocation().getBlockZ()).getType().equals(Material.AIR)) {

                    //setup new player location
                    Location newPLoc = new Location(p.getWorld(), p.getLocation().getX() + 1, p.getLocation().getY(), p.getLocation().getZ());
                    newPLoc.setPitch(p.getLocation().getPitch()); newPLoc.setYaw(p.getLocation().getYaw());
                    p.teleport(newPLoc);
                    p.setVelocity(new Vector(0.2, 0.2, 0.0));
                }
            }
        }

    }

    // Save wall into config by selecting corner positions
    public static void saveInConfig(Location pos1, Location pos2) {
        StringBuilder wall = new StringBuilder();
        //South ot North facing (X equals to X)
        if (pos1.getX() != pos2.getX() && pos1.getZ() == pos2.getZ()) {
            for (int rows = 0; rows <= Math.abs(pos2.getY()-pos1.getY()); rows++) {
                for (int col = 0; col <= Math.abs(pos2.getX()-pos1.getX()); col++) {
                    wall.append(pos1.getWorld().getBlockAt((int) (pos1.getX() - col), (int) (pos1.getY() - rows), (int) pos1.getZ()).getType() + ",");

                }
            }
        }

        if (wall.length() >= 1) wall.deleteCharAt(wall.length() - 1); //remove "," from string ending

        List<String> wallList = plugin.getConfig().getStringList("walls");

        //save wall & blocks facing into config
        wallList.add(wall.toString());

        //save & update config
        plugin.getConfig().set("walls", wallList);
        plugin.saveConfig();
        plugin.reloadConfig();

    }

    // Listening commands
    public static void wallSetup(Player player, String[] args) {
        // first corner
        if (args[2].equalsIgnoreCase("pos1")) {
            Location pos = Objects.requireNonNull(player.getTargetBlockExact(10)).getLocation();
            pos1.put(player, pos);
            player.sendMessage(ChatColor.GREEN + "[SlimeWalls] Wall's 1st block is: " + pos.getX() + " " + pos.getY() + " " + pos.getZ());
        }
        // second corner
        else if (args[2].equalsIgnoreCase("pos2")) {
            Location pos = Objects.requireNonNull(player.getTargetBlockExact(10)).getLocation();
            pos2.put(player, pos);
            player.sendMessage(ChatColor.GREEN + "[SlimeWalls] Wall's 2st block is: " + pos.getX() + " " + pos.getY() + " " + pos.getZ());
        }
        // call saving method saveInConfig()
        else if (args[2].equalsIgnoreCase("save")) {
            if (pos1.get(player) == null || pos2.get(player) == null) player.sendMessage(ChatColor.RED+"[SlimeWalls] Please, select 1st and 2nd pos before saving a wall!");
            else {
                saveInConfig(pos1.get(player), pos2.get(player));
                player.sendMessage(ChatColor.GREEN + "[SlimeWalls] Added new wall into config.");
            }
        }
    }

}