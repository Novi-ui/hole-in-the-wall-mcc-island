package com.hitw.game;

import com.hitw.HoleInTheWallPlugin;
import com.hitw.arena.Arena;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.Map;

/**
 * Manages all active games
 */
public class GameManager implements Listener {
    
    private final HoleInTheWallPlugin plugin;
    private final Map<String, Game> activeGames;
    private final Map<Player, Game> playerGames;
    
    public GameManager(HoleInTheWallPlugin plugin) {
        this.plugin = plugin;
        this.activeGames = new ConcurrentHashMap<>();
        this.playerGames = new ConcurrentHashMap<>();
    }
    
    /**
     * Create a new game
     */
    public Game createGame(Arena arena) {
        if (arena == null || !arena.isValid()) {
            return null;
        }
        
        String gameId = "game_" + System.currentTimeMillis();
        Game game = new Game(gameId, arena, plugin);
        activeGames.put(gameId, game);
        
        plugin.getLogger().info("Created new game: " + gameId + " in arena: " + arena.getName());
        return game;
    }
    
    /**
     * Join a player to an available game
     */
    public boolean joinGame(Player player) {
        if (player == null || playerGames.containsKey(player)) {
            return false; // Player already in game
        }
        
        // Find an available game or create new one
        Game availableGame = findAvailableGame();
        
        if (availableGame == null) {
            // Create new game
            Arena arena = plugin.getArenaManager().getRandomArena();
            if (arena == null) {
                return false; // No arenas available
            }
            
            availableGame = createGame(arena);
            if (availableGame == null) {
                return false; // Failed to create game
            }
        }
        
        // Add player to game
        boolean success = availableGame.addPlayer(player);
        if (success) {
            playerGames.put(player, availableGame);
        }
        
        return success;
    }
    
    /**
     * Remove player from their current game
     */
    public boolean leaveGame(Player player) {
        Game game = playerGames.remove(player);
        if (game == null) {
            return false; // Player not in game
        }
        
        game.removePlayer(player);
        
        // Clean up empty games
        if (game.getPlayers().isEmpty()) {
            removeGame(game.getId());
        }
        
        return true;
    }
    
    /**
     * Find an available game that can accept new players
     */
    private Game findAvailableGame() {
        for (Game game : activeGames.values()) {
            if (game.canJoin()) {
                return game;
            }
        }
        return null;
    }
    
    /**
     * Remove a game
     */
    public void removeGame(String gameId) {
        Game game = activeGames.remove(gameId);
        if (game != null) {
            // Remove all players from tracking
            for (Player player : game.getPlayers()) {
                playerGames.remove(player);
            }
            
            game.stop();
            plugin.getLogger().info("Removed game: " + gameId);
        }
    }
    
    /**
     * Update all active games
     */
    public void updateGames() {
        List<String> toRemove = new ArrayList<>();
        
        for (Game game : activeGames.values()) {
            game.tick();
            
            if (game.isFinished()) {
                toRemove.add(game.getId());
            }
        }
        
        // Remove finished games
        for (String gameId : toRemove) {
            removeGame(gameId);
        }
    }
    
    /**
     * Stop all games
     */
    public void stopAllGames() {
        for (Game game : new ArrayList<>(activeGames.values())) {
            game.stop();
        }
        activeGames.clear();
        playerGames.clear();
        
        plugin.getLogger().info("Stopped all games");
    }
    
    /**
     * Get game by player
     */
    public Game getPlayerGame(Player player) {
        return playerGames.get(player);
    }
    
    /**
     * Get all active games
     */
    public List<Game> getActiveGames() {
        return new ArrayList<>(activeGames.values());
    }
    
    /**
     * Get game by ID
     */
    public Game getGame(String gameId) {
        return activeGames.get(gameId);
    }
    
    /**
     * Check if player is in a game
     */
    public boolean isInGame(Player player) {
        return playerGames.containsKey(player);
    }
}