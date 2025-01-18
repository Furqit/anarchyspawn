package com.jellypudding.anarchySpawn;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

import java.util.*;

public final class AnarchySpawn extends JavaPlugin implements Listener, TabCompleter {
    private int spawnRadius;
    private int maxAttempts;
    private final Random random = new Random();
    private final Set<Material> unsafeBlocks = new HashSet<>();

    @Override
    public void onEnable() {
        // Save default config if it doesn't exist
        saveDefaultConfig();
        loadConfigValues();

        // Register events and tab completers
        getServer().getPluginManager().registerEvents(this, this);
        Objects.requireNonNull(getCommand("spawn")).setTabCompleter(this);
        Objects.requireNonNull(getCommand("anarchyspawn")).setTabCompleter(this);

        getLogger().info("AnarchySpawn has been enabled!");
    }

    @Override
    public void onDisable() {
        getLogger().info("AnarchySpawn has been disabled!");
    }

    private void loadConfigValues() {
        FileConfiguration config = getConfig();
        spawnRadius = config.getInt("spawn-radius", 10000);
        maxAttempts = config.getInt("max-spawn-attempts", 50);

        // Load unsafe blocks from config
        unsafeBlocks.clear();
        List<String> unsafeBlocksList = config.getStringList("unsafe-blocks");
        for (String blockName : unsafeBlocksList) {
            try {
                Material material = Material.valueOf(blockName.toUpperCase());
                unsafeBlocks.add(material);
            } catch (IllegalArgumentException e) {
                getLogger().warning("Invalid material name in config: " + blockName);
            }
        }
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, String[] args) {
        if (command.getName().equalsIgnoreCase("spawn")) {
            if (!(sender instanceof Player player)) {
                sender.sendMessage("This command can only be used by players!");
                return true;
            }

            Location spawnLocation = findSafeSpawnLocation(player.getWorld());
            if (spawnLocation != null) {
                player.teleport(spawnLocation);
                player.sendMessage("Teleported to a random spawn location!");
            } else {
                player.sendMessage("Could not find a safe spawn location!");
            }
            return true;
        } else if (command.getName().equalsIgnoreCase("anarchyspawn")) {
            if (args.length == 0) {
                sender.sendMessage("Usage: /anarchyspawn reload");
                return true;
            }

            if (args[0].equalsIgnoreCase("reload")) {
                if (!sender.hasPermission("anarchyspawn.reload")) {
                    sender.sendMessage("You don't have permission to use this command!");
                    return true;
                }
                reloadConfig();
                loadConfigValues();
                sender.sendMessage("AnarchySpawn configuration reloaded!");
                return true;
            }
            return false;
        }
        return false;
    }

    @Override
    public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String alias, String[] args) {
        if (command.getName().equalsIgnoreCase("anarchyspawn") && args.length == 1) {
            List<String> completions = new ArrayList<>();
            if (sender.hasPermission("anarchyspawn.reload") && "reload".startsWith(args[0].toLowerCase())) {
                completions.add("reload");
            }
            return completions;
        }
        return new ArrayList<>();
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        if (!player.hasPlayedBefore()) {
            Location spawnLocation = findSafeSpawnLocation(player.getWorld());
            if (spawnLocation != null) {
                player.teleport(spawnLocation);
            } else {
                getLogger().warning("Could not find safe spawn location for " + player.getName());
            }
        }
    }

    @EventHandler
    public void onPlayerRespawn(PlayerRespawnEvent event) {
        if (!event.isAnchorSpawn() && !event.isBedSpawn()) {
            World overworld = event.getPlayer().getServer().getWorlds().getFirst(); // Gets default world
            Location spawnLocation = findSafeSpawnLocation(overworld);
            if (spawnLocation != null) {
                event.setRespawnLocation(spawnLocation);
            }
        }
    }

    private Location findSafeSpawnLocation(World world) {
        for (int attempt = 0; attempt < maxAttempts; attempt++) {
            // Generate random coordinates within spawn radius
            int x = random.nextInt(spawnRadius * 2) - spawnRadius;
            int z = random.nextInt(spawnRadius * 2) - spawnRadius;

            // Find the highest non-air block at these coordinates
            int y = world.getHighestBlockYAt(x, z);
            Location location = new Location(world, x + 0.5, y + 1, z + 0.5);

            if (isSafeLocation(location)) {
                return location;
            }
        }
        return null;
    }

    private boolean isSafeLocation(Location location) {
        Block feet = location.getBlock();
        Block ground = location.subtract(0, 1, 0).getBlock();
        Block head = location.add(0, 2, 0).getBlock();

        // Check if there's space for the player
        if (!feet.getType().isAir() || !head.getType().isAir()) {
            return false;
        }

        // Check if the ground is solid and safe
        return ground.getType().isSolid()
                && !unsafeBlocks.contains(ground.getType())
                && !ground.isLiquid();
    }
}