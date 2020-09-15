package me.nkightly.worldresetter;

import org.bukkit.Bukkit;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.Material;
import org.bukkit.Location;
import org.bukkit.GameRule;
import org.bukkit.World;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.world.WorldInitEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerPortalEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.event.player.PlayerTeleportEvent;

public class Listeners implements Listener {

    @EventHandler
    public void onPlayerPortal(PlayerPortalEvent event) {
        Location from = event.getFrom();
        Location to;
        if (event.getCause() == PlayerTeleportEvent.TeleportCause.NETHER_PORTAL) {
            for (World w : Bukkit.getWorlds()) w.setGameRule(GameRule.SEND_COMMAND_FEEDBACK, false);
            Bukkit.getServer().dispatchCommand(Bukkit.getServer().getConsoleSender(), "advancement grant " + event.getPlayer().getName() + " only story/enter_the_nether");
            for (World w : Bukkit.getWorlds()) w.setGameRule(GameRule.SEND_COMMAND_FEEDBACK, true);

            if (from.getWorld().getName().startsWith("att_")) {
                if (from.getWorld().getEnvironment() == World.Environment.NORMAL) {
                    to = new Location(Bukkit.getWorld(from.getWorld().getName() + "_nether"), from.getBlockX() / 8, from.getBlockY(), from.getBlockZ() / 8);
                    event.setTo(to);
                }
                else if (from.getWorld().getEnvironment() == World.Environment.NETHER) {
                    to = new Location(Bukkit.getWorld(from.getWorld().getName().replace("_nether", "")), from.getBlockX() * 8, from.getBlockY(), from.getBlockZ() * 8);
                    event.setTo(to);
                }

            }
        }
        else if (event.getCause() == PlayerTeleportEvent.TeleportCause.END_PORTAL) {
            if (from.getWorld().getName().startsWith("att_")) {
                if (event.getPlayer().getWorld().getEnvironment() == World.Environment.THE_END) {
                    World world = Bukkit.getWorld("world");
                    to = world.getSpawnLocation();

                    Utils.handleEndCompletion();

                    to.setWorld(world);
                    event.setTo(to);
                } else {
                    to = new Location(Bukkit.getWorld(from.getWorld().getName().replace("_nether", "") + "_the_end"), 100, 50, 0);
                    event.setTo(to);
                    Block block = to.getBlock();
                    for (int x = block.getX() - 2; x <= block.getX() + 2; x++) {
                        for (int z = block.getZ() - 2; z <= block.getZ() + 2; z++) {
                            Block platformBlock = to.getWorld().getBlockAt(x, block.getY() - 1, z);
                            if (platformBlock.getType() != Material.OBSIDIAN) {
                                platformBlock.setType(Material.OBSIDIAN);
                            }
                            for (int yMod = 1; yMod <= 3; yMod++) {
                                Block b = platformBlock.getRelative(BlockFace.UP, yMod);
                                if (b.getType() != Material.AIR) {
                                    b.setType(Material.AIR);
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    @EventHandler
    public void onPlayerRespawn(PlayerRespawnEvent event) {
        World fromWorld = event.getPlayer().getWorld();
        if (fromWorld.getEnvironment() != World.Environment.NORMAL) {
            fromWorld = Bukkit.getWorld(fromWorld.getName().replace("_nether", "").replace("_the_end", ""));
        }
        Location to = new Location(fromWorld, fromWorld.getSpawnLocation().getX(), fromWorld.getSpawnLocation().getY(), fromWorld.getSpawnLocation().getZ());
        event.setRespawnLocation(to);
    }

    @EventHandler
    public void onWorldInit(WorldInitEvent event) {
        if (!event.getWorld().getName().startsWith("world"))
            event.getWorld().setKeepSpawnInMemory(false);
    }

    @EventHandler
    public void onPlayerMove(PlayerMoveEvent event) {
        if (event.getTo().getWorld().getName().equals("limbo")) {
            World limbo = Bukkit.getWorld("limbo");
            Location limboSpawn = new Location(limbo, 100000, 69, 100000);

            if (event.getPlayer().getLocation().distance(limboSpawn) > 1) {
                event.getPlayer().teleport(limboSpawn);
            }
        }
    }
}
