package me.nkightly.worldresetter;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.World;
import org.bukkit.WorldCreator;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.configuration.file.FileConfiguration;


public class WorldResetter extends JavaPlugin {
    static FileConfiguration config;
    private static WorldResetter instance;

    public static WorldResetter getInstance() {
        return instance;
    }

    public static String getSettings() {
        return "Interval: " + config.get("interval") + ", MaxRetries: " + config.get("maxRetries") + ", Seed: " + config.get("seed");
    }

    public static String GetSettingStr(String setting) {
        return (String)config.get(setting);
    }

    public static int GetSettingInt(String setting) {
        return (int)config.get(setting);
    }

    public static long GetSettingLong(String setting) { return (long)config.get(setting); }

    public static String SetSetting(String setting, String value) {
        /*if (setting.equals("boolPlaceholder")) {
            if (!(value.equals("true") || value.equals("false"))) {
                return ChatColor.RED + "Must be true or false (case sensitive).";
            }
            config.set(setting, value);
            return ChatColor.GREEN + "Set " + setting + " to " + value + ".";
        }*/
        if (setting.equals("maxRetries") || setting.equals("interval")) {
            int intValue;
            try {
                intValue = Integer.parseInt(value);
            } catch (NumberFormatException e) {
                return ChatColor.RED + "Must be a number.";
            }
            if (intValue < 1 && setting.equals("interval")) {
                return ChatColor.RED + "Must be a positive number.";
            }
            config.set(setting, intValue);
            return ChatColor.GREEN + "Set " + setting + " to " + value + ".";
        }
        if (setting.equals("seed")) {
            long longValue;
            try {
                longValue = Long.parseLong(value);
            } catch (NumberFormatException e) {
                return ChatColor.RED + "Must be a number.";

            }
            config.set(setting, longValue);
            return ChatColor.GREEN + "Set " + setting + " to " + value + ".";
        }

        return ChatColor.RED + "Usage: /resetter settings set <setting> <value>";
    }

    public void onEnable() {
        instance = this;

        config = getConfig();
        config.addDefault("seed", Bukkit.getWorld("world").getSeed());
        config.addDefault("interval", 23620);
        config.addDefault("maxRetries", -1);

        System.out.println("[WorldResetter] Enabled plugin!");

        Commands commands = new Commands();
        for (String command : getDescription().getCommands().keySet()) {
            getServer().getPluginCommand(command).setExecutor(commands);
            getServer().getPluginCommand(command).setTabCompleter(commands);
        }
        getServer().getPluginManager().registerEvents(new Listeners(), this);

        WorldCreator wc = new WorldCreator("limbo");
        wc.generator(new LimboGenerator());
        wc.environment(World.Environment.THE_END);
        wc.createWorld();
    }

    public static void makeWorlds(String name, long seed) {
        WorldCreator creator = new WorldCreator(name);
        WorldCreator creatorNether = new WorldCreator(name + "_nether");
        WorldCreator creatorEnd = new WorldCreator(name + "_the_end");

        creator.seed(seed);
        creatorNether.seed(seed);
        creatorEnd.seed(seed);

        creator.environment(World.Environment.NORMAL);
        creatorNether.environment(World.Environment.NETHER);
        creatorEnd.environment(World.Environment.THE_END);

        Bukkit.getWorlds().add(creator.createWorld());
        Bukkit.getWorlds().add(creatorNether.createWorld());
        Bukkit.getWorlds().add(creatorEnd.createWorld());

    }

    public void onDisable() {
        Utils.gather(Bukkit.getWorld("world").getSpawnLocation());
        config.options().copyDefaults(true);
        saveConfig();
        instance = null;
    }
}