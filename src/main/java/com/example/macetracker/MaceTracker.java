package com.example.macetracker;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.BlockState;
import org.bukkit.block.Container;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.HashMap;
import java.util.Map;

public class MaceTracker extends JavaPlugin {

    private final Map<Location, ArmorStand> glowingChests = new HashMap<>();

    @Override
    public void onEnable() {
        getLogger().info("MaceTracker enabled!");

        new BukkitRunnable() {
            @Override
            public void run() {
                Map<Location, Boolean> chestsWithMaceThisTick = new HashMap<>();

                for (Player player : Bukkit.getOnlinePlayers()) {
                    if (hasMaceInInventory(player)) {
                        Location loc = player.getLocation();
                        String msg = ChatColor.GOLD + "[Mace] " + player.getName() +
                                " has the mace in inventory at X: " + loc.getBlockX() +
                                " Y: " + loc.getBlockY() +
                                " Z: " + loc.getBlockZ();
                        player.sendActionBar(msg);
                        player.setGlowing(true);
                    } else {
                        player.setGlowing(false);
                    }
                }

                for (org.bukkit.World world : Bukkit.getWorlds()) {
                    for (Entity entity : world.getEntities()) {
                        if (entity instanceof Item) {
                            Item itemEntity = (Item) entity;
                            if (isMace(itemEntity.getItemStack())) {
                                Location loc = itemEntity.getLocation();
                                String msg = ChatColor.AQUA + "[Mace] On ground at X: " +
                                        loc.getBlockX() + " Y: " + loc.getBlockY() +
                                        " Z: " + loc.getBlockZ();
                                for (Player p : Bukkit.getOnlinePlayers()) {
                                    p.sendActionBar(msg);
                                }
                            }
                        }
                    }

                    for (org.bukkit.Chunk chunk : world.getLoadedChunks()) {
                        for (BlockState blockState : chunk.getTileEntities()) {
                            if (blockState instanceof Container) {
                                Inventory inv = ((Container) blockState).getInventory();
                                boolean hasMace = false;
                                for (ItemStack item : inv.getContents()) {
                                    if (isMace(item)) {
                                        hasMace = true;
                                        Location loc = blockState.getLocation();
                                        String msg = ChatColor.AQUA + "[Mace] Stored in container at X: " +
                                                loc.getBlockX() + " Y: " + loc.getBlockY() +
                                                " Z: " + loc.getBlockZ();
                                        for (Player p : Bukkit.getOnlinePlayers()) {
                                            p.sendActionBar(msg);
                                        }
                                        break;
                                    }
                                }

                                Location chestLoc = blockState.getLocation();
                                if (hasMace) {
                                    chestsWithMaceThisTick.put(chestLoc, true);
                                    if (!glowingChests.containsKey(chestLoc)) {
                                        ArmorStand stand = spawnGlowingArmorStand(chestLoc);
                                        glowingChests.put(chestLoc, stand);
                                    }
                                }
                            }
                        }
                    }
                }

                // Clean up armor stands from chests that no longer have the mace
                glowingChests.entrySet().removeIf(entry -> {
                    Location loc = entry.getKey();
                    ArmorStand stand = entry.getValue();
                    if (!chestsWithMaceThisTick.containsKey(loc)) {
                        stand.remove();
                        return true;
                    }
                    return false;
                });
            }
        }.runTaskTimer(this, 0L, 1L); // Every tick
    }

    private boolean isMace(ItemStack item) {
        return item != null && item.getType() == Material.MACE;
    }

    private boolean hasMaceInInventory(Player player) {
        for (ItemStack item : player.getInventory().getContents()) {
            if (isMace(item)) {
                return true;
            }
        }
        return false;
    }

    private ArmorStand spawnGlowingArmorStand(Location chestLocation) {
        Location loc = chestLocation.clone().add(0.5, 1.0, 0.5);
        ArmorStand stand = (ArmorStand) chestLocation.getWorld().spawnEntity(loc, EntityType.ARMOR_STAND);
        stand.setVisible(false);
        stand.setGlowing(true);
        stand.setGravity(false);
        stand.setMarker(true);
        return stand;
    }
}
