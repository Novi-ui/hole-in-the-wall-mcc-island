package com.hitw.wall;

import java.util.Random;

/**
 * Interface for defining wall patterns
 */
public interface WallPattern {
    
    /**
     * Generate a pattern of holes in the wall
     * @param width Width of the wall
     * @param height Height of the wall
     * @return 2D array where true = hole, false = wall
     */
    boolean[][] generatePattern(int width, int height);
    
    /**
     * Get the pattern name
     */
    String getName();
    
    /**
     * Get the difficulty level
     */
    PatternDifficulty getDifficulty();
    
    enum PatternDifficulty {
        BASIC, ADVANCED, EXPERT
    }
}

/**
 * Simple cross pattern with a hole in the center
 */
class SimpleCrossPattern implements WallPattern {
    
    @Override
    public boolean[][] generatePattern(int width, int height) {
        boolean[][] pattern = new boolean[width][height];
        
        int centerX = width / 2;
        int centerY = height / 2;
        
        // Create cross pattern
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                // Horizontal line
                if (y == centerY && x >= centerX - 1 && x <= centerX + 1) {
                    pattern[x][y] = true;
                }
                // Vertical line
                else if (x == centerX && y >= centerY - 1 && y <= centerY + 1) {
                    pattern[x][y] = true;
                }
            }
        }
        
        return pattern;
    }
    
    @Override
    public String getName() {
        return "simple_cross";
    }
    
    @Override
    public PatternDifficulty getDifficulty() {
        return PatternDifficulty.BASIC;
    }
}

/**
 * Single hole pattern
 */
class SingleHolePattern implements WallPattern {
    
    private final Random random = new Random();
    
    @Override
    public boolean[][] generatePattern(int width, int height) {
        boolean[][] pattern = new boolean[width][height];
        
        // Random hole position
        int holeX = random.nextInt(width - 2) + 1; // Avoid edges
        int holeY = random.nextInt(height - 2) + 1;
        
        // Create 2x2 hole
        for (int x = holeX; x < holeX + 2 && x < width; x++) {
            for (int y = holeY; y < holeY + 2 && y < height; y++) {
                pattern[x][y] = true;
            }
        }
        
        return pattern;
    }
    
    @Override
    public String getName() {
        return "single_hole";
    }
    
    @Override
    public PatternDifficulty getDifficulty() {
        return PatternDifficulty.BASIC;
    }
}

/**
 * Double hole pattern
 */
class DoubleHolePattern implements WallPattern {
    
    private final Random random = new Random();
    
    @Override
    public boolean[][] generatePattern(int width, int height) {
        boolean[][] pattern = new boolean[width][height];
        
        // First hole
        int hole1X = width / 4;
        int hole1Y = height / 2;
        
        // Second hole
        int hole2X = (3 * width) / 4;
        int hole2Y = height / 2;
        
        // Create holes
        createHole(pattern, hole1X, hole1Y, width, height);
        createHole(pattern, hole2X, hole2Y, width, height);
        
        return pattern;
    }
    
    private void createHole(boolean[][] pattern, int centerX, int centerY, int width, int height) {
        for (int x = centerX - 1; x <= centerX + 1; x++) {
            for (int y = centerY - 1; y <= centerY + 1; y++) {
                if (x >= 0 && x < width && y >= 0 && y < height) {
                    pattern[x][y] = true;
                }
            }
        }
    }
    
    @Override
    public String getName() {
        return "double_hole";
    }
    
    @Override
    public PatternDifficulty getDifficulty() {
        return PatternDifficulty.BASIC;
    }
}

/**
 * Zigzag pattern
 */
class ZigzagPattern implements WallPattern {
    
    @Override
    public boolean[][] generatePattern(int width, int height) {
        boolean[][] pattern = new boolean[width][height];
        
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                // Create zigzag pattern
                int offset = (x % 4 < 2) ? 0 : 2;
                if ((y + offset) % 4 == 0 || (y + offset) % 4 == 1) {
                    pattern[x][y] = true;
                }
            }
        }
        
        return pattern;
    }
    
    @Override
    public String getName() {
        return "zigzag";
    }
    
    @Override
    public PatternDifficulty getDifficulty() {
        return PatternDifficulty.ADVANCED;
    }
}

/**
 * Spiral pattern
 */
class SpiralPattern implements WallPattern {
    
    @Override
    public boolean[][] generatePattern(int width, int height) {
        boolean[][] pattern = new boolean[width][height];
        
        int centerX = width / 2;
        int centerY = height / 2;
        
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                double dx = x - centerX;
                double dy = y - centerY;
                double distance = Math.sqrt(dx * dx + dy * dy);
                double angle = Math.atan2(dy, dx);
                
                // Create spiral holes
                if ((distance + angle * 2) % 3 < 1.5) {
                    pattern[x][y] = true;
                }
            }
        }
        
        return pattern;
    }
    
    @Override
    public String getName() {
        return "spiral";
    }
    
    @Override
    public PatternDifficulty getDifficulty() {
        return PatternDifficulty.ADVANCED;
    }
}

/**
 * Random holes pattern
 */
class RandomHolesPattern implements WallPattern {
    
    private final Random random = new Random();
    
    @Override
    public boolean[][] generatePattern(int width, int height) {
        boolean[][] pattern = new boolean[width][height];
        
        // Create random holes (30% chance for each position)
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                if (random.nextDouble() < 0.3) {
                    pattern[x][y] = true;
                }
            }
        }
        
        // Ensure there's at least one player-sized hole
        ensurePlayerHole(pattern, width, height);
        
        return pattern;
    }
    
    private void ensurePlayerHole(boolean[][] pattern, int width, int height) {
        int holeX = random.nextInt(width - 1);
        int holeY = random.nextInt(height - 1);
        
        // Create 2x2 hole
        for (int x = holeX; x < holeX + 2 && x < width; x++) {
            for (int y = holeY; y < holeY + 2 && y < height; y++) {
                pattern[x][y] = true;
            }
        }
    }
    
    @Override
    public String getName() {
        return "random_holes";
    }
    
    @Override
    public PatternDifficulty getDifficulty() {
        return PatternDifficulty.ADVANCED;
    }
}

/**
 * Complex maze pattern
 */
class ComplexMazePattern implements WallPattern {
    
    private final Random random = new Random();
    
    @Override
    public boolean[][] generatePattern(int width, int height) {
        boolean[][] pattern = new boolean[width][height];
        
        // Create maze-like structure
        for (int x = 0; x < width; x += 2) {
            for (int y = 0; y < height; y += 2) {
                if (random.nextDouble() < 0.7) {
                    // Create small holes
                    if (x < width) pattern[x][y] = true;
                    if (x + 1 < width && y < height) pattern[x + 1][y] = true;
                    if (x < width && y + 1 < height) pattern[x][y + 1] = true;
                }
            }
        }
        
        // Add some larger passages
        addLargePassages(pattern, width, height);
        
        return pattern;
    }
    
    private void addLargePassages(boolean[][] pattern, int width, int height) {
        int passages = 2 + random.nextInt(2); // 2-3 passages
        
        for (int i = 0; i < passages; i++) {
            int startX = random.nextInt(width - 3);
            int startY = random.nextInt(height - 3);
            
            // Create 3x3 passage
            for (int x = startX; x < startX + 3 && x < width; x++) {
                for (int y = startY; y < startY + 3 && y < height; y++) {
                    pattern[x][y] = true;
                }
            }
        }
    }
    
    @Override
    public String getName() {
        return "complex_maze";
    }
    
    @Override
    public PatternDifficulty getDifficulty() {
        return PatternDifficulty.EXPERT;
    }
}

/**
 * Shifting holes pattern that changes over time
 */
class ShiftingHolesPattern implements WallPattern {
    
    private final Random random = new Random();
    private long seed;
    
    public ShiftingHolesPattern() {
        this.seed = System.currentTimeMillis();
    }
    
    @Override
    public boolean[][] generatePattern(int width, int height) {
        boolean[][] pattern = new boolean[width][height];
        Random seededRandom = new Random(seed + System.currentTimeMillis() / 1000); // Changes every second
        
        // Create shifting pattern
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                double noise = seededRandom.nextGaussian();
                if (noise > -0.5 && noise < 0.5) {
                    pattern[x][y] = true;
                }
            }
        }
        
        return pattern;
    }
    
    @Override
    public String getName() {
        return "shifting_holes";
    }
    
    @Override
    public PatternDifficulty getDifficulty() {
        return PatternDifficulty.EXPERT;
    }
}