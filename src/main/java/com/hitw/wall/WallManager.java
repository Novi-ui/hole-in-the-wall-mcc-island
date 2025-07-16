package com.hitw.wall;

import com.hitw.HoleInTheWallPlugin;
import com.hitw.arena.Arena;
import org.bukkit.Location;
import org.bukkit.util.Vector;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Manages wall patterns and wall creation
 */
public class WallManager {
    
    private final HoleInTheWallPlugin plugin;
    private final Map<String, WallPattern> patterns;
    private final Map<WallPattern.PatternDifficulty, List<WallPattern>> patternsByDifficulty;
    private final Random random;
    
    public WallManager(HoleInTheWallPlugin plugin) {
        this.plugin = plugin;
        this.patterns = new ConcurrentHashMap<>();
        this.patternsByDifficulty = new EnumMap<>(WallPattern.PatternDifficulty.class);
        this.random = new Random();
        
        initializePatterns();
        organizePatternsbyDifficulty();
    }
    
    /**
     * Initialize all wall patterns
     */
    private void initializePatterns() {
        // Register all pattern implementations
        registerPattern(new SimpleCrossPattern());
        registerPattern(new SingleHolePattern());
        registerPattern(new DoubleHolePattern());
        registerPattern(new ZigzagPattern());
        registerPattern(new SpiralPattern());
        registerPattern(new RandomHolesPattern());
        registerPattern(new ComplexMazePattern());
        registerPattern(new ShiftingHolesPattern());
        
        plugin.getLogger().info("Registered " + patterns.size() + " wall patterns");
    }
    
    /**
     * Register a wall pattern
     */
    private void registerPattern(WallPattern pattern) {
        patterns.put(pattern.getName(), pattern);
    }
    
    /**
     * Organize patterns by difficulty for easy selection
     */
    private void organizePatternsbyDifficulty() {
        for (WallPattern.PatternDifficulty difficulty : WallPattern.PatternDifficulty.values()) {
            patternsByDifficulty.put(difficulty, new ArrayList<>());
        }
        
        for (WallPattern pattern : patterns.values()) {
            patternsByDifficulty.get(pattern.getDifficulty()).add(pattern);
        }
    }
    
    /**
     * Create a wall with specified parameters
     */
    public Wall createWall(Arena arena, WallPattern pattern, double speed) {
        if (arena == null || pattern == null) {
            return null;
        }
        
        // Select random spawn point and direction
        List<Location> spawnPoints = arena.getWallSpawnPoints();
        if (spawnPoints.isEmpty()) {
            return null;
        }
        
        Location spawnPoint = spawnPoints.get(random.nextInt(spawnPoints.size()));
        Vector direction = calculateDirection(spawnPoint, arena.getCenter());
        
        String wallId = generateWallId();
        return new Wall(wallId, arena, pattern, spawnPoint, direction, speed);
    }
    
    /**
     * Create a wall with random pattern based on difficulty
     */
    public Wall createWall(Arena arena, WallPattern.PatternDifficulty difficulty, double speed) {
        WallPattern pattern = getRandomPattern(difficulty);
        return createWall(arena, pattern, speed);
    }
    
    /**
     * Create a wall with random pattern from config
     */
    public Wall createWall(Arena arena, int round, double baseSpeed) {
        WallPattern.PatternDifficulty difficulty = getDifficultyForRound(round);
        double speed = calculateSpeedForRound(round, baseSpeed);
        
        return createWall(arena, difficulty, speed);
    }
    
    /**
     * Get pattern by name
     */
    public WallPattern getPattern(String name) {
        return patterns.get(name);
    }
    
    /**
     * Get random pattern by difficulty
     */
    public WallPattern getRandomPattern(WallPattern.PatternDifficulty difficulty) {
        List<WallPattern> availablePatterns = patternsByDifficulty.get(difficulty);
        if (availablePatterns.isEmpty()) {
            // Fall back to basic patterns
            availablePatterns = patternsByDifficulty.get(WallPattern.PatternDifficulty.BASIC);
        }
        
        if (availablePatterns.isEmpty()) {
            return new SingleHolePattern(); // Ultimate fallback
        }
        
        return availablePatterns.get(random.nextInt(availablePatterns.size()));
    }
    
    /**
     * Get all patterns
     */
    public Collection<WallPattern> getAllPatterns() {
        return new ArrayList<>(patterns.values());
    }
    
    /**
     * Get patterns by difficulty
     */
    public List<WallPattern> getPatternsByDifficulty(WallPattern.PatternDifficulty difficulty) {
        return new ArrayList<>(patternsByDifficulty.get(difficulty));
    }
    
    /**
     * Calculate direction from spawn point to arena center
     */
    private Vector calculateDirection(Location spawnPoint, Location center) {
        Vector direction = center.toVector().subtract(spawnPoint.toVector());
        return direction.normalize();
    }
    
    /**
     * Determine difficulty based on round number
     */
    private WallPattern.PatternDifficulty getDifficultyForRound(int round) {
        int basicToAdvanced = plugin.getConfigManager().getInt("difficulty.pattern-thresholds.basic-to-advanced", 5);
        int advancedToExpert = plugin.getConfigManager().getInt("difficulty.pattern-thresholds.advanced-to-expert", 10);
        
        if (round < basicToAdvanced) {
            return WallPattern.PatternDifficulty.BASIC;
        } else if (round < advancedToExpert) {
            return WallPattern.PatternDifficulty.ADVANCED;
        } else {
            return WallPattern.PatternDifficulty.EXPERT;
        }
    }
    
    /**
     * Calculate wall speed based on round
     */
    private double calculateSpeedForRound(int round, double baseSpeed) {
        double speedIncrement = plugin.getConfigManager().getDouble("walls.movement.speed-increment", 0.2);
        double maxSpeed = plugin.getConfigManager().getDouble("walls.movement.max-speed", 8.0);
        
        double speed = baseSpeed + (round * speedIncrement);
        return Math.min(speed, maxSpeed);
    }
    
    /**
     * Generate unique wall ID
     */
    private String generateWallId() {
        return "wall_" + System.currentTimeMillis() + "_" + random.nextInt(1000);
    }
    
    /**
     * Get wall statistics
     */
    public Map<String, Object> getStatistics() {
        Map<String, Object> stats = new HashMap<>();
        
        stats.put("total_patterns", patterns.size());
        
        Map<String, Integer> difficultyCount = new HashMap<>();
        for (WallPattern.PatternDifficulty difficulty : WallPattern.PatternDifficulty.values()) {
            difficultyCount.put(difficulty.name().toLowerCase(), patternsByDifficulty.get(difficulty).size());
        }
        stats.put("patterns_by_difficulty", difficultyCount);
        
        return stats;
    }
    
    /**
     * Reload wall manager
     */
    public void reload() {
        // Patterns are hardcoded, but we can reload configuration-based settings
        organizePatternsbyDifficulty();
        plugin.getLogger().info("Wall manager reloaded");
    }
}