package com.hitw;

import com.hitw.arena.ArenaManager;
import com.hitw.commands.HitwAdminCommand;
import com.hitw.commands.HitwCommand;
import com.hitw.config.ConfigManager;
import com.hitw.config.DatabaseManager;
import com.hitw.effects.EffectsManager;
import com.hitw.game.GameManager;
import com.hitw.integration.IntegrationManager;
import com.hitw.player.PlayerManager;
import com.hitw.score.ScoreManager;
import com.hitw.utils.MessageUtil;
import com.hitw.wall.WallManager;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.logging.Level;

/**
 * Main plugin class for Hole in the Wall - MCC Style Plugin
 * Manages all core components and provides API access
 */
public class HoleInTheWallPlugin extends JavaPlugin {
    
    private static HoleInTheWallPlugin instance;
    
    // Core managers
    private ConfigManager configManager;
    private DatabaseManager databaseManager;
    private ArenaManager arenaManager;
    private GameManager gameManager;
    private PlayerManager playerManager;
    private WallManager wallManager;
    private ScoreManager scoreManager;
    private EffectsManager effectsManager;
    private IntegrationManager integrationManager;
    
    // Plugin state
    private boolean enabled = false;
    
    @Override
    public void onLoad() {
        instance = this;
        getLogger().info("Loading Hole in the Wall Plugin...");
    }
    
    @Override
    public void onEnable() {
        try {
            // Initialize core systems
            initializeManagers();
            
            // Register commands
            registerCommands();
            
            // Register events
            registerEvents();
            
            // Setup integrations
            setupIntegrations();
            
            // Start background tasks
            startTasks();
            
            enabled = true;
            getLogger().info("Hole in the Wall Plugin has been enabled successfully!");
            
            // Display startup message
            displayStartupInfo();
            
        } catch (Exception e) {
            getLogger().log(Level.SEVERE, "Failed to enable Hole in the Wall Plugin!", e);
            getServer().getPluginManager().disablePlugin(this);
        }
    }
    
    @Override
    public void onDisable() {
        try {
            enabled = false;
            getLogger().info("Disabling Hole in the Wall Plugin...");
            
            // Stop all games
            if (gameManager != null) {
                gameManager.stopAllGames();
            }
            
            // Restore player inventories and states
            if (playerManager != null) {
                playerManager.restoreAllPlayers();
            }
            
            // Close database connections
            if (databaseManager != null) {
                databaseManager.close();
            }
            
            // Cleanup integrations
            if (integrationManager != null) {
                integrationManager.disable();
            }
            
            getLogger().info("Hole in the Wall Plugin has been disabled successfully!");
            
        } catch (Exception e) {
            getLogger().log(Level.SEVERE, "Error while disabling plugin!", e);
        }
    }
    
    /**
     * Initialize all core managers
     */
    private void initializeManagers() {
        getLogger().info("Initializing managers...");
        
        // Configuration manager (must be first)
        configManager = new ConfigManager(this);
        configManager.loadConfig();
        
        // Database manager
        databaseManager = new DatabaseManager(this);
        databaseManager.initialize();
        
        // Core game managers
        arenaManager = new ArenaManager(this);
        wallManager = new WallManager(this);
        effectsManager = new EffectsManager(this);
        scoreManager = new ScoreManager(this);
        playerManager = new PlayerManager(this);
        gameManager = new GameManager(this);
        
        // Integration manager
        integrationManager = new IntegrationManager(this);
        
        getLogger().info("All managers initialized successfully!");
    }
    
    /**
     * Register plugin commands
     */
    private void registerCommands() {
        getLogger().info("Registering commands...");
        
        // Main player command
        HitwCommand hitwCommand = new HitwCommand(this);
        getCommand("hitw").setExecutor(hitwCommand);
        getCommand("hitw").setTabCompleter(hitwCommand);
        
        // Admin command
        HitwAdminCommand adminCommand = new HitwAdminCommand(this);
        getCommand("hitwadmin").setExecutor(adminCommand);
        getCommand("hitwadmin").setTabCompleter(adminCommand);
        
        getLogger().info("Commands registered successfully!");
    }
    
    /**
     * Register event listeners
     */
    private void registerEvents() {
        getLogger().info("Registering event listeners...");
        
        // Register managers that handle events
        Bukkit.getPluginManager().registerEvents(playerManager, this);
        Bukkit.getPluginManager().registerEvents(gameManager, this);
        Bukkit.getPluginManager().registerEvents(arenaManager, this);
        
        getLogger().info("Event listeners registered successfully!");
    }
    
    /**
     * Setup external plugin integrations
     */
    private void setupIntegrations() {
        getLogger().info("Setting up integrations...");
        integrationManager.initialize();
        getLogger().info("Integrations setup complete!");
    }
    
    /**
     * Start background tasks
     */
    private void startTasks() {
        getLogger().info("Starting background tasks...");
        
        // Game update task (runs every tick)
        Bukkit.getScheduler().runTaskTimer(this, () -> {
            if (gameManager != null) {
                gameManager.updateGames();
            }
        }, 0L, 1L);
        
        // Statistics update task (runs every 5 minutes)
        Bukkit.getScheduler().runTaskTimerAsynchronously(this, () -> {
            if (scoreManager != null) {
                scoreManager.updateLeaderboards();
            }
        }, 0L, 6000L);
        
        // Memory cleanup task (runs every 10 minutes)
        Bukkit.getScheduler().runTaskTimer(this, () -> {
            if (configManager.getBoolean("advanced.memory.force-gc", false)) {
                System.gc();
            }
        }, 0L, 12000L);
        
        getLogger().info("Background tasks started successfully!");
    }
    
    /**
     * Display startup information
     */
    private void displayStartupInfo() {
        String[] lines = {
            "",
            "§8▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓",
            "§c  _   _ _ _____      __",
            "§c | | | (_)_   \\    / /",
            "§c | |_| |_  | |\\ \\/\\/ /",
            "§c |  _  | | | | \\    /",
            "§c |_| |_|_| |_|  \\/\\/",
            "§8▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓",
            "§e  Hole in the Wall - MCC Style Plugin",
            "§7  Version: §a" + getDescription().getVersion(),
            "§7  Author: §a" + getDescription().getAuthors(),
            "§7  Minecraft: §a" + Bukkit.getBukkitVersion(),
            "",
            "§a  ✓ Plugin enabled successfully!",
            "§7  Use §e/hitw help §7for player commands",
            "§7  Use §e/hitwadmin help §7for admin commands",
            "§8▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓",
            ""
        };
        
        for (String line : lines) {
            getServer().getConsoleSender().sendMessage(line);
        }
    }
    
    /**
     * Reload the plugin configuration and managers
     */
    public void reload() {
        try {
            getLogger().info("Reloading plugin configuration...");
            
            // Reload configuration
            configManager.loadConfig();
            
            // Reload managers that support it
            if (arenaManager != null) {
                arenaManager.reload();
            }
            
            if (wallManager != null) {
                wallManager.reload();
            }
            
            if (effectsManager != null) {
                effectsManager.reload();
            }
            
            if (integrationManager != null) {
                integrationManager.reload();
            }
            
            getLogger().info("Plugin reloaded successfully!");
            
        } catch (Exception e) {
            getLogger().log(Level.SEVERE, "Error while reloading plugin!", e);
            throw new RuntimeException("Failed to reload plugin", e);
        }
    }
    
    // ============================
    // API METHODS
    // ============================
    
    /**
     * Get the plugin instance
     * @return Plugin instance
     */
    public static HoleInTheWallPlugin getInstance() {
        return instance;
    }
    
    /**
     * Check if plugin is enabled and ready
     * @return true if plugin is enabled
     */
    public boolean isPluginEnabled() {
        return enabled;
    }
    
    // Manager getters for API access
    
    public ConfigManager getConfigManager() {
        return configManager;
    }
    
    public DatabaseManager getDatabaseManager() {
        return databaseManager;
    }
    
    public ArenaManager getArenaManager() {
        return arenaManager;
    }
    
    public GameManager getGameManager() {
        return gameManager;
    }
    
    public PlayerManager getPlayerManager() {
        return playerManager;
    }
    
    public WallManager getWallManager() {
        return wallManager;
    }
    
    public ScoreManager getScoreManager() {
        return scoreManager;
    }
    
    public EffectsManager getEffectsManager() {
        return effectsManager;
    }
    
    public IntegrationManager getIntegrationManager() {
        return integrationManager;
    }
}