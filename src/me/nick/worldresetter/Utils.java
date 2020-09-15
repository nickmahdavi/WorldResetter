package me.nkightly.worldresetter;

import java.io.File;
import java.util.UUID;
import java.util.HashMap;

import org.bukkit.*;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;

public class Utils {
    public static String root = Bukkit.getServer().getWorldContainer().getAbsolutePath();
    public static HashMap<UUID, HashMap<String, Object>> LastSeen = new HashMap<>();

    public static void deleteWorlds(String dir1) {
        File dir = new File(root + "/" + dir1);
        File dir_nether = new File(root + "/" + dir1 + "_nether");
        File dir_end = new File(root + "/" + dir1 + "_the_end");

        Utils.deleteDirectory(dir);
        Utils.deleteDirectory(dir_nether);
        Utils.deleteDirectory(dir_end);
    }

    public static void unloadWorlds(String dir1) {
        World undir = Bukkit.getWorld(dir1);
        World undir_nether = Bukkit.getWorld(dir1 + "_nether");
        World undir_end = Bukkit.getWorld(dir1 + "_the_end");

        if (undir != null)
            Bukkit.getServer().unloadWorld(undir, true);

        if (undir_nether != null)
            Bukkit.getServer().unloadWorld(undir_nether, true);

        if (undir_end != null)
            Bukkit.getServer().unloadWorld(undir_end, true);
    }

    public static void handleEndCompletion() {
        if (Commands.task != null && !Commands.task.isCancelled()) {
            int timeTaken = (WorldResetter.GetSettingInt("interval") - Commands.counter);
            Bukkit.broadcastMessage(ChatColor.GREEN + "" + ChatColor.BOLD + String.format("You finished in %d minutes, %.2f seconds.", timeTaken/1200, (double)((timeTaken % 1200) / 20)));
            Commands.task.cancel();
        }
    }

    public static void deleteDirectory(File path) {
        if(path.exists()) {
            File[] files = path.listFiles();
            for (File file : files) {
                if (file.isDirectory()) {
                    deleteDirectory(file);
                } else {
                    file.delete();
                }
            }
        }
        path.delete();
    }

    public static void gather(Location location) {
        for (Player player : Bukkit.getOnlinePlayers()) {
            for (World w : Bukkit.getWorlds()) w.setGameRule(GameRule.SEND_COMMAND_FEEDBACK, false);
            Bukkit.getServer().dispatchCommand(Bukkit.getServer().getConsoleSender(), "advancement revoke @a everything");
            for (World w : Bukkit.getWorlds()) w.setGameRule(GameRule.SEND_COMMAND_FEEDBACK, true);
            player.getInventory().clear();
            player.teleport(location);
            for (PotionEffect effect : player.getActivePotionEffects()) player.removePotionEffect(effect.getType());
            player.setNoDamageTicks(60);
            player.setGameMode((GameMode)LastSeen.get(player.getUniqueId()).get("gamemode"));
            player.setHealth(20.0);
            player.setFoodLevel(20);
            player.setSaturation(5);
            player.setExp(0.0F);
            player.setLevel(0);
            player.setFireTicks(0);
        }
    }

    public static void limbo() {
        World limbo = Bukkit.getWorld("limbo");
        Location limboSpawn = new Location(limbo, 100000, 69, 100000);

        for (Player player : Bukkit.getOnlinePlayers()) {
            HashMap<String,Object> map = new HashMap<>();
            map.put("location", player.getLocation());
            map.put("gamemode", player.getGameMode());
            map.put("effects", player.getActivePotionEffects());
            map.put("airtime", player.getRemainingAir());

            LastSeen.put(player.getUniqueId(), map);
            player.teleport(limboSpawn);
            player.setGameMode(GameMode.SPECTATOR);
        }
    }
}
