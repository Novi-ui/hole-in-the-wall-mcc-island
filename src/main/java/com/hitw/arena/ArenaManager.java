package com.hitw.arena;

import com.hitw.HoleInTheWallPlugin;
import com.hitw.config.ConfigManager;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.world.WorldLoadEvent;
import org.bukkit.event.world.WorldUnloadEvent;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;

/**
 * Manages all game arenas
 */
public class ArenaManager implements Listener {
    
    private final HoleInTheWallPlugin plugin;
    private final Map<String, Arena> arenas;
    private final Map<String, Arena> disabledArenas;
    private FileConfiguration arenasConfig;
    private File arenasFile;
    
    public ArenaManager(HoleInTheWallPlugin plugin) {
        this.plugin = plugin;
        this.arenas = new ConcurrentHashMap<>();
        this.disabledArenas = new ConcurrentHashMap<>();
        
        loadArenasConfig();
        loadArenas();
    }
    
    /**
     * Load arenas configuration file
     */
    private void loadArenasConfig() {
        arenasFile = new File(plugin.getDataFolder(), "arenas.yml");
        
        if (!arenasFile.exists()) {
            try {
                arenasFile.createNewFile();
            } catch (IOException e) {
                plugin.getLogger().log(Level.SEVERE, "Could not create arenas.yml", e);
            }
        }
        
        arenasConfig = YamlConfiguration.loadConfiguration(arenasFile);
    }
    
    /**
     * Load all arenas from configuration
     */
    private void loadArenas() {
        ConfigurationSection arenasSection = arenasConfig.getConfigurationSection("arenas");
        if (arenasSection == null) {
            plugin.getLogger().info("No arenas found in configuration");
            return;
        }
        
        int loaded = 0;
        int failed = 0;
        
        for (String arenaName : arenasSection.getKeys(false)) {
            try {
                Arena arena = loadArena(arenaName, arenasSection.getConfigurationSection(arenaName));
                if (arena != null) {
                    if (arena.isValid()) {
                        arenas.put(arenaName, arena);
                        loaded++;
                    } else {
                        disabledArenas.put(arenaName, arena);
                        plugin.getLogger().warning("Arena '" + arenaName + "' is invalid and has been disabled");
                    }
                }
            } catch (Exception e) {
                plugin.getLogger().log(Level.WARNING, "Failed to load arena: " + arenaName, e);
                failed++;
            }
        }
        
        plugin.getLogger().info(String.format("Loaded %d arenas (%d failed, %d disabled)", 
            loaded, failed, disabledArenas.size()));
    }
    
    /**
     * Load a single arena from configuration
     */
    private Arena loadArena(String name, ConfigurationSection section) {
        if (section == null) {
            return null;
        }
        
        try {
            String worldName = section.getString("world");
            double x = section.getDouble("x");
            double y = section.getDouble("y");
            double z = section.getDouble("z");
            int size = section.getInt("size", 15);
            boolean enabled = section.getBoolean("enabled", true);
            
            World world = Bukkit.getWorld(worldName);
            if (world == null) {
                plugin.getLogger().warning("World '" + worldName + "' not found for arena: " + name);
                return null;
            }
            
            Location center = new Location(world, x, y, z);
            Arena arena = new Arena(name, center, size);
            arena.setEnabled(enabled);
            
            return arena;
            
        } catch (Exception e) {
            plugin.getLogger().log(Level.WARNING, "Error loading arena configuration: " + name, e);
            return null;
        }
    }
    
    /**
     * Save arenas to configuration
     */
    public void saveArenas() {
        try {
            arenasConfig.set("arenas", null); // Clear existing
            
            for (Arena arena : getAllArenas()) {
                String path = "arenas." + arena.getName();
                Location center = arena.getCenter();
                
                arenasConfig.set(path + ".world", center.getWorld().getName());
                arenasConfig.set(path + ".x", center.getX());
                arenasConfig.set(path + ".y", center.getY());
                arenasConfig.set(path + ".z", center.getZ());
                arenasConfig.set(path + ".size", arena.getPlatformSize());
                arenasConfig.set(path + ".enabled", arena.isEnabled());
            }
            
            arenasConfig.save(arenasFile);
            plugin.getLogger().info("Arenas saved successfully");
            
        } catch (IOException e) {
            plugin.getLogger().log(Level.SEVERE, "Failed to save arenas configuration", e);
        }
    }
    
    /**
     * Create a new arena
     */
    public boolean createArena(String name, Location location, int size) {
        if (arenas.containsKey(name) || disabledArenas.containsKey(name)) {
            return false; // Arena already exists
        }
        
        // Validate parameters
        ConfigManager config = plugin.getConfigManager();
        int minSize = config.getInt("arena.validation.min-platform-size", 10);
        int maxSize = config.getInt("arena.validation.max-platform-size", 30);
        
        if (size < minSize || size > maxSize) {
            return false; // Invalid size
        }
        
        if (location.getWorld() == null) {
            return false; // Invalid world
        }
        
        try {
            Arena arena = new Arena(name, location, size);
            
            // Build the arena
            arena.build();
            
            // Add to active arenas
            arenas.put(name, arena);
            
            // Save configuration
            saveArenas();
            
            plugin.getLogger().info("Created arena: " + name + " at " + 
                location.getWorld().getName() + " " + 
                location.getBlockX() + "," + 
                location.getBlockY() + "," + 
                location.getBlockZ());
            
            return true;
            
        } catch (Exception e) {
            plugin.getLogger().log(Level.SEVERE, "Failed to create arena: " + name, e);
            return false;
        }
    }
    
    /**
     * Delete an arena
     */
    public boolean deleteArena(String name) {
        Arena arena = arenas.remove(name);
        if (arena == null) {
            arena = disabledArenas.remove(name);
        }
        
        if (arena == null) {
            return false; // Arena doesn't exist
        }
        
        try {
            // Remove arena blocks
            arena.remove();
            
            // Save configuration
            saveArenas();
            
            plugin.getLogger().info("Deleted arena: " + name);
            return true;
            
        } catch (Exception e) {
            plugin.getLogger().log(Level.SEVERE, "Failed to delete arena: " + name, e);
            return false;
        }
    }
    
    /**
     * Get an arena by name
     */
    public Arena getArena(String name) {
        return arenas.get(name);
    }
    
    /**
     * Get a random available arena
     */
    public Arena getRandomArena() {
        List<Arena> availableArenas = new ArrayList<>();
        
        for (Arena arena : arenas.values()) {
            if (arena.isEnabled() && arena.isValid()) {
                availableArenas.add(arena);
            }
        }
        
        if (availableArenas.isEmpty()) {
            return null;
        }
        
        Random random = new Random();
        return availableArenas.get(random.nextInt(availableArenas.size()));
    }
    
    /**
     * Get the best arena for a player (closest available)
     */
    public Arena getBestArena(Player player) {
        if (player == null || player.getLocation() == null) {
            return getRandomArena();
        }
        
        Arena closest = null;
        double closestDistance = Double.MAX_VALUE;
        
        for (Arena arena : arenas.values()) {
            if (!arena.isEnabled() || !arena.isValid()) {
                continue;
            }
            
            if (!arena.getWorld().equals(player.getWorld())) {
                continue;
            }
            
            double distance = arena.getCenter().distance(player.getLocation());
            if (distance < closestDistance) {
                closestDistance = distance;
                closest = arena;
            }
        }
        
        return closest != null ? closest : getRandomArena();
    }
    
    /**
     * Get all active arenas
     */
    public Collection<Arena> getArenas() {
        return new ArrayList<>(arenas.values());
    }
    
    /**
     * Get all arenas (including disabled)
     */
    public Collection<Arena> getAllArenas() {
        List<Arena> all = new ArrayList<>(arenas.values());
        all.addAll(disabledArenas.values());
        return all;
    }
    
    /**
     * Get arena names
     */
    public Set<String> getArenaNames() {
        return new HashSet<>(arenas.keySet());
    }
    
    /**
     * Check if arena exists
     */
    public boolean arenaExists(String name) {
        return arenas.containsKey(name) || disabledArenas.containsKey(name);
    }
    
    /**
     * Enable/disable an arena
     */
    public boolean setArenaEnabled(String name, boolean enabled) {
        Arena arena = arenas.get(name);
        if (arena == null) {
            arena = disabledArenas.get(name);
        }
        
        if (arena == null) {
            return false;
        }
        
        arena.setEnabled(enabled);
        
        if (enabled && arena.isValid()) {
            disabledArenas.remove(name);
            arenas.put(name, arena);
        } else if (!enabled) {
            arenas.remove(name);
            disabledArenas.put(name, arena);
        }
        
        saveArenas();
        return true;
    }
    
    /**
     * Validate all arenas
     */
    public void validateArenas() {
        List<String> toDisable = new ArrayList<>();
        
        for (Map.Entry<String, Arena> entry : arenas.entrySet()) {
            Arena arena = entry.getValue();
            if (!arena.isValid()) {
                toDisable.add(entry.getKey());
            }
        }
        
        for (String name : toDisable) {
            Arena arena = arenas.remove(name);
            disabledArenas.put(name, arena);
            plugin.getLogger().warning("Arena '" + name + "' has been disabled due to validation failure");
        }
        
        if (!toDisable.isEmpty()) {
            saveArenas();
        }
    }
    
    /**
     * Get arena statistics
     */
    public Map<String, Object> getStatistics() {
        Map<String, Object> stats = new HashMap<>();
        
        stats.put("total_arenas", arenas.size() + disabledArenas.size());
        stats.put("active_arenas", arenas.size());
        stats.put("disabled_arenas", disabledArenas.size());
        
        Map<String, Integer> worldCounts = new HashMap<>();
        for (Arena arena : getAllArenas()) {
            String worldName = arena.getWorld() != null ? arena.getWorld().getName() : "null";
            worldCounts.put(worldName, worldCounts.getOrDefault(worldName, 0) + 1);
        }
        stats.put("arenas_by_world", worldCounts);
        
        return stats;
    }
    
    /**
     * Reload arena manager
     */
    public void reload() {
        // Clear existing arenas
        arenas.clear();
        disabledArenas.clear();
        
        // Reload configuration
        loadArenasConfig();
        loadArenas();
        
        plugin.getLogger().info("Arena manager reloaded");
    }
    
    // Event handlers
    
    @EventHandler
    public void onWorldLoad(WorldLoadEvent event) {
        // Check if any disabled arenas can be re-enabled
        validateArenas();
    }
    
    @EventHandler
    public void onWorldUnload(WorldUnloadEvent event) {
        // Disable arenas in unloaded worlds
        String worldName = event.getWorld().getName();
        
        List<String> toDisable = new ArrayList<>();
        for (Map.Entry<String, Arena> entry : arenas.entrySet()) {
            Arena arena = entry.getValue();
            if (arena.getWorld() != null && arena.getWorld().getName().equals(worldName)) {
                toDisable.add(entry.getKey());
            }
        }
        
        for (String name : toDisable) {
            Arena arena = arenas.remove(name);
            disabledArenas.put(name, arena);
            plugin.getLogger().info("Arena '" + name + "' disabled due to world unload");
        }
    }
}