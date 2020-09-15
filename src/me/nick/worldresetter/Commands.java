package me.nkightly.worldresetter;

import java.util.ArrayList;
import java.io.File;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.boss.DragonBattle;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.util.Vector;

public class Commands implements CommandExecutor, TabExecutor {
    String root = Bukkit.getServer().getWorldContainer().getAbsolutePath();

    public static BukkitTask task;
    public static boolean skip = false;
    public static boolean paused = false;
    public static int attempts = 0;

    public static int counter;

    public boolean onCommand(CommandSender commandsender, Command command, String label, String[] args) {
        if (command.getName().equalsIgnoreCase("resetter")) {
            if (args[0].equalsIgnoreCase("reset")) {
                if (args.length == 1) {
                    File dir = new File(root);
                    File[] foundFiles;
                    for (Player player : Bukkit.getOnlinePlayers()) {
                        if (player.getWorld() != Bukkit.getWorlds().get(0)) {
                            player.teleport(Bukkit.getWorld("world").getSpawnLocation());
                        }
                    }
                    foundFiles = dir.listFiles((dir1, name) -> name.startsWith("att_"));
                    for (File file : foundFiles) {
                        Utils.deleteDirectory(file);
                    }
                    commandsender.sendMessage(ChatColor.GREEN + "Reset world files.");
                    return true;
                }
                commandsender.sendMessage(ChatColor.RED + "Usage: /resetter reset");
                return true;
            }
            else if (args[0].equalsIgnoreCase("settings")) {
                if (args.length == 2 && args[1].equalsIgnoreCase("get")) {
                    commandsender.sendMessage(WorldResetter.getSettings());
                    return true;
                }
                else if (args.length == 3 && args[1].equalsIgnoreCase("get")) {
                    commandsender.sendMessage(args[2] + ": " + WorldResetter.GetSettingStr(args[2]));
                    return true;
                }
                else if (args.length == 3 && args[1].equalsIgnoreCase("set")) {
                    commandsender.sendMessage(ChatColor.RED + "Usage: /resetter settings set <setting> <value>");
                    return true;
                }
                else if (args.length == 4 && args[1].equalsIgnoreCase("set")) {
                    commandsender.sendMessage(WorldResetter.SetSetting(args[2], args[3]));
                    return true;
                }
                commandsender.sendMessage(ChatColor.RED + "Usage: /resetter settings <get,set>");
                return true;
            }
            else if (args[0].equalsIgnoreCase("start")) {
                if (task != null && !task.isCancelled()) {
                    commandsender.sendMessage(ChatColor.RED + "Resetter is already running!");
                    return true;
                }

                long seed = WorldResetter.GetSettingLong("seed");
                File tmp;

                tmp = new File(root + "/att_0");
                if (args.length == 2 && args[1].equalsIgnoreCase("override")) {
                    if (tmp.exists()) {
                        Utils.deleteWorlds("att_0");
                    }
                } else {
                    if (tmp.exists()) {
                        commandsender.sendMessage(ChatColor.DARK_GREEN + "Old saves detected; aborting.");
                        commandsender.sendMessage(ChatColor.DARK_GREEN + "Run /resetter start override or /resetter reset to fix this.");
                        return true;
                    }
                }
                Utils.limbo();

                commandsender.sendMessage(ChatColor.GREEN + "Generating...");

                WorldResetter.makeWorlds("att_0", seed);

                World targetWorld = Bukkit.getWorld("att_0");
                Location location = targetWorld.getSpawnLocation();
                location.setWorld(targetWorld);

                Utils.gather(location);

                attempts = 0;

                Bukkit.broadcastMessage(ChatColor.GOLD + "" + ChatColor.BOLD + "Begin attempt 1.");
                task = (new BukkitRunnable() {
                    int timer = WorldResetter.GetSettingInt("interval");
                    int counter = this.timer;
                    int placeholder;
                    long seed;
                    int max;

                    public void run() {
                        timer = WorldResetter.GetSettingInt("interval");
                        seed = WorldResetter.GetSettingLong("seed");
                        max = WorldResetter.GetSettingInt("maxRetries");

                        if (Commands.paused) {
                            return;
                        }
                        if (!Commands.skip) {
                            this.counter--;
                            Commands.counter = this.counter;
                            if (this.counter % 6000 == 0 && this.counter > 0) {
                                Bukkit.broadcastMessage(ChatColor.GOLD + String.format("%d minutes remaining.", this.counter/1200));
                            }
                            else if (this.counter == 1200) {
                                Bukkit.broadcastMessage(ChatColor.GOLD + String.format("%d minute remaining!", this.counter/1200));
                            }
                            else if (this.counter % 1200 == 0 && this.counter < 6000 && this.counter > 0) {
                                Bukkit.broadcastMessage(ChatColor.GOLD + String.format("%d minutes remaining!", this.counter/1200));
                            }
                            else if (this.counter == 600) {
                                Bukkit.broadcastMessage(ChatColor.GOLD + "30 seconds remaining.");
                            }
                            else if (this.counter % 20 == 1 && this.counter < 115) {
                                Bukkit.broadcastMessage(ChatColor.GOLD + String.format("%d seconds left.", this.counter/20));
                            }
                            if (this.counter != 0) {
                                return;
                            }
                        } else {
                            Bukkit.broadcastMessage(ChatColor.GREEN + "Skipping to next reset...");
                            Commands.skip = false;
                        }

                        if (Commands.attempts <= this.max) {
                            Bukkit.broadcastMessage(ChatColor.RED + "Max attempts reached. Stopping.");
                            cancel();
                        }

                        this.counter = this.timer;

                        Bukkit.broadcastMessage(ChatColor.GREEN + "" + ChatColor.BOLD + "Resetting...");

                        DragonBattle battle = Bukkit.getWorld("att_" + Commands.attempts + "_the_end").getEnderDragonBattle();
                        battle.getBossBar().removeAll();

                        Utils.limbo();

                        placeholder = Commands.attempts;
                        Commands.attempts++;

                        if (new File(root + "/att_" + Commands.attempts).exists()) {
                            Utils.deleteWorlds("att_" + Commands.attempts);
                        }

                        WorldResetter.makeWorlds("att_" + Commands.attempts, this.seed);

                        World targetWorld = Bukkit.getWorld("att_" + Commands.attempts);
                        Location location = targetWorld.getSpawnLocation();
                        location.setWorld(targetWorld);

                        Utils.gather(location);

                        Utils.unloadWorlds("att_" + placeholder);

                        Bukkit.broadcastMessage(ChatColor.GOLD + "" + ChatColor.BOLD + String.format("Begin attempt %d.", Commands.attempts + 1));
                    }
                }).runTaskTimer(WorldResetter.getInstance(), 0L, 1L);
                return true;
            }
            else if (args[0].equalsIgnoreCase("stop")) {
                if (task != null && !task.isCancelled()) {
                    Bukkit.broadcastMessage(ChatColor.GREEN + "Teleporting to home world...");
                    for (Player player : Bukkit.getOnlinePlayers()) {
                        if (player.getWorld() != Bukkit.getWorlds().get(0)) {
                            player.teleport(Bukkit.getWorld("world").getSpawnLocation());
                        }
                    }
                    commandsender.sendMessage(ChatColor.RED + "" + ChatColor.BOLD + "Stopping.");
                    Utils.unloadWorlds("att_" + attempts);
                    task.cancel();
                } else {
                    commandsender.sendMessage(ChatColor.RED + "No task to stop.");
                }
                return true;
            }
            else if (args[0].equalsIgnoreCase("skip")) {
                if (task != null && !task.isCancelled()) {
                    skip = true;
                } else {
                    commandsender.sendMessage(ChatColor.RED + "Game is not running.");
                }
                return true;
            }
            else if (args[0].equalsIgnoreCase("remaining")) {
                if (task != null && !task.isCancelled()) {
                    int timeTaken = (WorldResetter.GetSettingInt("interval") - counter);
                    commandsender.sendMessage(ChatColor.GREEN + "" + ChatColor.BOLD + String.format("%d minutes, %d seconds remaining", timeTaken/1200, ((timeTaken % 1200) / 20)));
                    return true;
                } else {
                    commandsender.sendMessage(ChatColor.RED + "Game is not running.");
                }
            }
            else if (args[0].equalsIgnoreCase("pause")) {
                if (task != null && !task.isCancelled()) {
                    paused = !paused;
                    if (paused) {
                        commandsender.sendMessage(ChatColor.GREEN + "Paused.");
                        Utils.limbo();
                    } else {
                        commandsender.sendMessage(ChatColor.GREEN + "Unpaused.");

                        for (Player player : Bukkit.getOnlinePlayers()) {
                            HashMap<String, Object> playermap = Utils.LastSeen.get(player.getUniqueId());

                            player.teleport((Location)playermap.get("location"));
                            player.setGameMode((GameMode)playermap.get("gamemode"));
                            player.addPotionEffects((Collection<PotionEffect>)playermap.get("effects"));
                            player.setRemainingAir((int)playermap.get("airtime"));
                            player.setVelocity((Vector)playermap.get("velocity"));
                        }
                    }
                } else {
                    commandsender.sendMessage(ChatColor.RED + "Game is not running.");
                }
                return true;
            }
            if (task != null && !task.isCancelled()) {
                commandsender.sendMessage(ChatColor.RED + "Usage: /resetter <reset,settings,start,stop,skip,remaining,pause>");
            } else {
                commandsender.sendMessage(ChatColor.RED + "Usage: /resetter <reset,settings,start>");
            }
            return true;
        }
        return true;
    }

    public List<String> onTabComplete(CommandSender sender, Command command, String label, String[] args) {
        List<String> list = new ArrayList<>();
        if (args.length == 1) {
            list.add("reset");
            list.add("settings");
            list.add("start");

            if (task != null && !task.isCancelled()) {
                list.add("skip");
                list.add("stop");
                list.add("remaining");
                list.add("pause");
            }
        }
        else if (args[0].equalsIgnoreCase("settings")) {
            if (args.length == 3 && (args[1].equalsIgnoreCase("set") || args[1].equalsIgnoreCase("get"))) {
                list.add("seed");
                list.add("interval");
                list.add("maxRetries");
                return list;
            }
            if (args.length == 2) {
                list.add("get");
                list.add("set");
                return list;
            }
        }
        else if (args[0].equalsIgnoreCase("start")) {
            list.add("override");
        }
        return list;
    }
}