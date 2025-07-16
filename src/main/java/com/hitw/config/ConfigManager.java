package com.hitw.config;

import com.hitw.HoleInTheWallPlugin;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.List;
import java.util.logging.Level;

/**
 * Manages plugin configuration with caching and validation
 */
public class ConfigManager {
    
    private final HoleInTheWallPlugin plugin;
    private FileConfiguration config;
    private FileConfiguration defaultConfig;
    
    public ConfigManager(HoleInTheWallPlugin plugin) {
        this.plugin = plugin;
    }
    
    /**
     * Load configuration from file
     */
    public void loadConfig() {
        try {
            // Save default config if it doesn't exist
            if (!new File(plugin.getDataFolder(), "config.yml").exists()) {
                plugin.saveDefaultConfig();
            }
            
            // Load config
            plugin.reloadConfig();
            config = plugin.getConfig();
            
            // Load default config for comparison
            InputStream defConfigStream = plugin.getResource("config.yml");
            if (defConfigStream != null) {
                defaultConfig = YamlConfiguration.loadConfiguration(new InputStreamReader(defConfigStream));
            }
            
            // Validate config
            validateConfig();
            
            plugin.getLogger().info("Configuration loaded successfully!");
            
        } catch (Exception e) {
            plugin.getLogger().log(Level.SEVERE, "Failed to load configuration!", e);
            throw new RuntimeException("Configuration loading failed", e);
        }
    }
    
    /**
     * Validate configuration values
     */
    private void validateConfig() {
        boolean hasErrors = false;
        
        // Validate game settings
        if (getInt("game.min-players", 2) < 1) {
            plugin.getLogger().warning("game.min-players must be at least 1");
            hasErrors = true;
        }
        
        if (getInt("game.max-players", 20) < getInt("game.min-players", 2)) {
            plugin.getLogger().warning("game.max-players must be greater than min-players");
            hasErrors = true;
        }
        
        // Validate wall settings
        if (getDouble("walls.movement.base-speed", 2.0) <= 0) {
            plugin.getLogger().warning("walls.movement.base-speed must be greater than 0");
            hasErrors = true;
        }
        
        // Validate database settings
        String dbType = getString("database.type", "sqlite");
        if (!dbType.equals("sqlite") && !dbType.equals("mysql")) {
            plugin.getLogger().warning("database.type must be either 'sqlite' or 'mysql'");
            hasErrors = true;
        }
        
        if (hasErrors) {
            plugin.getLogger().warning("Configuration has errors! Plugin may not work correctly.");
        }
    }
    
    /**
     * Get string value from config
     */
    public String getString(String path, String defaultValue) {
        return config.getString(path, defaultValue);
    }
    
    /**
     * Get integer value from config
     */
    public int getInt(String path, int defaultValue) {
        return config.getInt(path, defaultValue);
    }
    
    /**
     * Get double value from config
     */
    public double getDouble(String path, double defaultValue) {
        return config.getDouble(path, defaultValue);
    }
    
    /**
     * Get boolean value from config
     */
    public boolean getBoolean(String path, boolean defaultValue) {
        return config.getBoolean(path, defaultValue);
    }
    
    /**
     * Get list value from config
     */
    public List<String> getStringList(String path) {
        return config.getStringList(path);
    }
    
    /**
     * Get list value from config with default
     */
    public List<String> getStringList(String path, List<String> defaultValue) {
        List<String> value = config.getStringList(path);
        return value.isEmpty() ? defaultValue : value;
    }
    
    /**
     * Set value in config
     */
    public void set(String path, Object value) {
        config.set(path, value);
    }
    
    /**
     * Save config to file
     */
    public void save() {
        try {
            plugin.saveConfig();
        } catch (Exception e) {
            plugin.getLogger().log(Level.SEVERE, "Failed to save configuration!", e);
        }
    }
    
    /**
     * Get raw config object
     */
    public FileConfiguration getConfig() {
        return config;
    }
    
    /**
     * Check if path exists in config
     */
    public boolean contains(String path) {
        return config.contains(path);
    }
    
    /**
     * Get formatted message with prefix
     */
    public String getMessage(String path, String defaultMessage) {
        String prefix = getString("general.prefix", "&8[&cHiTW&8] ");
        String message = getString("messages." + path, defaultMessage);
        return prefix + message;
    }
    
    /**
     * Get message without prefix
     */
    public String getMessageRaw(String path, String defaultMessage) {
        return getString("messages." + path, defaultMessage);
    }
}