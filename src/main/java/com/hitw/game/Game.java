package com.hitw.game;

import com.hitw.HoleInTheWallPlugin;
import com.hitw.arena.Arena;
import com.hitw.wall.Wall;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.Map;

/**
 * Represents a single game instance
 */
public class Game {
    
    private final String id;
    private final Arena arena;
    private final HoleInTheWallPlugin plugin;
    private final Map<Player, GamePlayer> players;
    private final List<Wall> activeWalls;
    
    private GameState state;
    private long startTime;
    private int currentRound;
    private int ticksAlive;
    
    public enum GameState {
        WAITING, STARTING, ACTIVE, FINISHED
    }
    
    public Game(String id, Arena arena, HoleInTheWallPlugin plugin) {
        this.id = id;
        this.arena = arena;
        this.plugin = plugin;
        this.players = new ConcurrentHashMap<>();
        this.activeWalls = new ArrayList<>();
        this.state = GameState.WAITING;
        this.currentRound = 1;
        this.ticksAlive = 0;
    }
    
    /**
     * Add a player to the game
     */
    public boolean addPlayer(Player player) {
        if (players.size() >= plugin.getConfigManager().getInt("game.max-players", 20)) {
            return false; // Game full
        }
        
        if (state != GameState.WAITING) {
            return false; // Game already started
        }
        
        GamePlayer gamePlayer = new GamePlayer(player);
        players.put(player, gamePlayer);
        
        // Teleport player to arena
        player.teleport(arena.getSpawnPoint());
        
        // Check if we can start
        checkStartConditions();
        
        return true;
    }
    
    /**
     * Remove a player from the game
     */
    public void removePlayer(Player player) {
        GamePlayer gamePlayer = players.remove(player);
        if (gamePlayer != null) {
            // Restore player state
            plugin.getPlayerManager().restorePlayer(player);
        }
        
        // Check if game should end
        if (players.size() <= 1 && state == GameState.ACTIVE) {
            endGame();
        }
    }
    
    /**
     * Check if game can start
     */
    private void checkStartConditions() {
        int minPlayers = plugin.getConfigManager().getInt("game.min-players", 2);
        
        if (players.size() >= minPlayers && state == GameState.WAITING) {
            if (plugin.getConfigManager().getBoolean("game.auto-start", true)) {
                startCountdown();
            }
        }
    }
    
    /**
     * Start the countdown
     */
    private void startCountdown() {
        state = GameState.STARTING;
        // TODO: Implement countdown logic
    }
    
    /**
     * Start the actual game
     */
    public void start() {
        if (state != GameState.STARTING) {
            return;
        }
        
        state = GameState.ACTIVE;
        startTime = System.currentTimeMillis();
        
        // Initialize all players
        for (GamePlayer gamePlayer : players.values()) {
            plugin.getPlayerManager().preparePlayer(gamePlayer.getPlayer());
        }
        
        // Start spawning walls
        spawnWall();
    }
    
    /**
     * Update the game (called every tick)
     */
    public void tick() {
        if (state != GameState.ACTIVE) {
            return;
        }
        
        ticksAlive++;
        
        // Update walls
        updateWalls();
        
        // Check collisions
        checkCollisions();
        
        // Spawn new walls periodically
        if (ticksAlive % 60 == 0) { // Every 3 seconds
            spawnWall();
        }
        
        // Check win condition
        if (getAlivePlayers().size() <= 1) {
            endGame();
        }
    }
    
    /**
     * Update all active walls
     */
    private void updateWalls() {
        List<Wall> toRemove = new ArrayList<>();
        
        for (Wall wall : activeWalls) {
            wall.tick();
            
            if (!wall.isActive()) {
                toRemove.add(wall);
            }
        }
        
        // Remove inactive walls
        for (Wall wall : toRemove) {
            activeWalls.remove(wall);
            wall.remove();
        }
    }
    
    /**
     * Check wall collisions with players
     */
    private void checkCollisions() {
        for (Wall wall : activeWalls) {
            for (GamePlayer gamePlayer : players.values()) {
                if (!gamePlayer.isAlive()) continue;
                
                if (wall.checkCollision(gamePlayer.getPlayer())) {
                    eliminatePlayer(gamePlayer);
                }
            }
        }
    }
    
    /**
     * Eliminate a player
     */
    private void eliminatePlayer(GamePlayer gamePlayer) {
        gamePlayer.setEliminated(true, currentRound);
        
        Player player = gamePlayer.getPlayer();
        player.sendMessage("§cYou were eliminated!");
        
        // TODO: Add effects, sounds, etc.
    }
    
    /**
     * Spawn a new wall
     */
    private void spawnWall() {
        double baseSpeed = plugin.getConfigManager().getDouble("walls.movement.base-speed", 2.0);
        Wall wall = plugin.getWallManager().createWall(arena, currentRound, baseSpeed);
        
        if (wall != null) {
            activeWalls.add(wall);
        }
    }
    
    /**
     * End the game
     */
    private void endGame() {
        state = GameState.FINISHED;
        
        // Clean up walls
        for (Wall wall : activeWalls) {
            wall.remove();
        }
        activeWalls.clear();
        
        // Restore all players
        for (Player player : players.keySet()) {
            plugin.getPlayerManager().restorePlayer(player);
        }
        
        // TODO: Calculate rewards, save statistics, etc.
    }
    
    /**
     * Stop the game forcefully
     */
    public void stop() {
        state = GameState.FINISHED;
        
        // Clean up walls
        for (Wall wall : activeWalls) {
            wall.remove();
        }
        activeWalls.clear();
        
        // Restore all players
        for (Player player : players.keySet()) {
            plugin.getPlayerManager().restorePlayer(player);
        }
    }
    
    /**
     * Get alive players
     */
    public List<GamePlayer> getAlivePlayers() {
        List<GamePlayer> alive = new ArrayList<>();
        for (GamePlayer gamePlayer : players.values()) {
            if (gamePlayer.isAlive()) {
                alive.add(gamePlayer);
            }
        }
        return alive;
    }
    
    /**
     * Check if new players can join
     */
    public boolean canJoin() {
        return state == GameState.WAITING && 
               players.size() < plugin.getConfigManager().getInt("game.max-players", 20);
    }
    
    // Getters
    
    public String getId() {
        return id;
    }
    
    public Arena getArena() {
        return arena;
    }
    
    public List<Player> getPlayers() {
        return new ArrayList<>(players.keySet());
    }
    
    public GameState getState() {
        return state;
    }
    
    public boolean isFinished() {
        return state == GameState.FINISHED;
    }
    
    public int getCurrentRound() {
        return currentRound;
    }
    
    public List<Wall> getActiveWalls() {
        return new ArrayList<>(activeWalls);
    }
}