package com.hitw.wall;

import com.hitw.arena.Arena;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.util.BoundingBox;
import org.bukkit.util.Vector;

import java.util.*;

/**
 * Represents a moving wall with holes that players must dodge through
 */
public class Wall {
    
    private final String id;
    private final Arena arena;
    private final WallPattern pattern;
    private final Vector direction;
    private final double speed;
    private final Material wallMaterial;
    private final int width;
    private final int height;
    
    private Location currentLocation;
    private List<Block> wallBlocks;
    private List<Block> holeBlocks;
    private BoundingBox boundingBox;
    private boolean active;
    private long spawnTime;
    private int ticksAlive;
    
    public Wall(String id, Arena arena, WallPattern pattern, Location spawnLocation, Vector direction, double speed) {
        this.id = id;
        this.arena = arena;
        this.pattern = pattern;
        this.direction = direction.normalize();
        this.speed = speed;
        this.wallMaterial = Material.RED_CONCRETE;
        this.width = arena.getPlatformSize() + 2; // Slightly larger than platform
        this.height = arena.getBarrierHeight() - 1;
        
        this.currentLocation = spawnLocation.clone();
        this.wallBlocks = new ArrayList<>();
        this.holeBlocks = new ArrayList<>();
        this.active = true;
        this.spawnTime = System.currentTimeMillis();
        this.ticksAlive = 0;
        
        generateWall();
    }
    
    /**
     * Generate the wall structure based on pattern
     */
    private void generateWall() {
        World world = currentLocation.getWorld();
        if (world == null) return;
        
        boolean[][] holes = pattern.generatePattern(width, height);
        int startX = currentLocation.getBlockX() - width / 2;
        int startY = currentLocation.getBlockY();
        int startZ = currentLocation.getBlockZ();
        
        // Adjust coordinates based on direction
        if (Math.abs(direction.getX()) > Math.abs(direction.getZ())) {
            // Moving along X-axis, wall is on Z-axis
            for (int y = 0; y < height; y++) {
                for (int z = 0; z < width; z++) {
                    Block block = world.getBlockAt(startX, startY + y, startZ + z - width / 2);
                    if (holes[z][y]) {
                        holeBlocks.add(block);
                    } else {
                        wallBlocks.add(block);
                        block.setType(wallMaterial);
                    }
                }
            }
        } else {
            // Moving along Z-axis, wall is on X-axis
            for (int y = 0; y < height; y++) {
                for (int x = 0; x < width; x++) {
                    Block block = world.getBlockAt(startX + x - width / 2, startY + y, startZ);
                    if (holes[x][y]) {
                        holeBlocks.add(block);
                    } else {
                        wallBlocks.add(block);
                        block.setType(wallMaterial);
                    }
                }
            }
        }
        
        updateBoundingBox();
    }
    
    /**
     * Update the wall's bounding box for collision detection
     */
    private void updateBoundingBox() {
        if (wallBlocks.isEmpty()) return;
        
        double minX = Double.MAX_VALUE, minY = Double.MAX_VALUE, minZ = Double.MAX_VALUE;
        double maxX = Double.MIN_VALUE, maxY = Double.MIN_VALUE, maxZ = Double.MIN_VALUE;
        
        for (Block block : wallBlocks) {
            minX = Math.min(minX, block.getX());
            minY = Math.min(minY, block.getY());
            minZ = Math.min(minZ, block.getZ());
            maxX = Math.max(maxX, block.getX() + 1);
            maxY = Math.max(maxY, block.getY() + 1);
            maxZ = Math.max(maxZ, block.getZ() + 1);
        }
        
        boundingBox = new BoundingBox(minX, minY, minZ, maxX, maxY, maxZ);
    }
    
    /**
     * Move the wall one tick
     */
    public void tick() {
        if (!active) return;
        
        ticksAlive++;
        
        // Calculate new position
        Vector movement = direction.clone().multiply(speed / 20.0); // Convert to blocks per tick
        Location newLocation = currentLocation.add(movement);
        
        // Check if wall has moved out of bounds
        if (!arena.contains(newLocation) && getDistanceFromArena() > width) {
            remove();
            return;
        }
        
        // Move wall blocks
        moveWallBlocks(movement);
        
        currentLocation = newLocation;
        updateBoundingBox();
    }
    
    /**
     * Move all wall blocks by the given movement vector
     */
    private void moveWallBlocks(Vector movement) {
        World world = currentLocation.getWorld();
        if (world == null) return;
        
        // Clear current blocks
        for (Block block : wallBlocks) {
            block.setType(Material.AIR);
        }
        
        // Create new blocks at new position
        List<Block> newWallBlocks = new ArrayList<>();
        List<Block> newHoleBlocks = new ArrayList<>();
        
        for (Block oldBlock : wallBlocks) {
            Location newBlockLocation = oldBlock.getLocation().add(movement);
            Block newBlock = world.getBlockAt(newBlockLocation);
            
            // Only place block if it's within arena bounds or close to it
            if (arena.contains(newBlockLocation) || getDistanceFromArena() <= width) {
                newBlock.setType(wallMaterial);
                newWallBlocks.add(newBlock);
            }
        }
        
        for (Block oldBlock : holeBlocks) {
            Location newBlockLocation = oldBlock.getLocation().add(movement);
            Block newBlock = world.getBlockAt(newBlockLocation);
            newHoleBlocks.add(newBlock);
        }
        
        wallBlocks = newWallBlocks;
        holeBlocks = newHoleBlocks;
    }
    
    /**
     * Check collision with a player
     */
    public boolean checkCollision(Player player) {
        if (!active || boundingBox == null) return false;
        
        Location playerLoc = player.getLocation();
        
        // Check if player is in wall's bounding box
        if (!boundingBox.contains(playerLoc.toVector())) {
            return false;
        }
        
        // Check if player is in a hole
        for (Block holeBlock : holeBlocks) {
            if (isPlayerInBlock(player, holeBlock)) {
                return false; // Player is in a hole, no collision
            }
        }
        
        // Check if player is in a wall block
        for (Block wallBlock : wallBlocks) {
            if (isPlayerInBlock(player, wallBlock)) {
                return true; // Collision detected
            }
        }
        
        return false;
    }
    
    /**
     * Check if player is within a specific block
     */
    private boolean isPlayerInBlock(Player player, Block block) {
        Location playerLoc = player.getLocation();
        Location blockLoc = block.getLocation();
        
        // Check if player's position overlaps with block
        return playerLoc.getX() >= blockLoc.getX() &&
               playerLoc.getX() <= blockLoc.getX() + 1 &&
               playerLoc.getY() >= blockLoc.getY() &&
               playerLoc.getY() <= blockLoc.getY() + 2 && // Player height
               playerLoc.getZ() >= blockLoc.getZ() &&
               playerLoc.getZ() <= blockLoc.getZ() + 1;
    }
    
    /**
     * Get distance from arena center
     */
    public double getDistanceFromArena() {
        return currentLocation.distance(arena.getCenter());
    }
    
    /**
     * Check if wall is close to a player (for warnings)
     */
    public boolean isCloseToPlayer(Player player, double distance) {
        if (!active) return false;
        return currentLocation.distance(player.getLocation()) <= distance;
    }
    
    /**
     * Get the closest hole to a player
     */
    public Location getClosestHole(Player player) {
        if (holeBlocks.isEmpty()) return null;
        
        Location playerLoc = player.getLocation();
        Location closest = null;
        double closestDistance = Double.MAX_VALUE;
        
        for (Block holeBlock : holeBlocks) {
            double distance = playerLoc.distance(holeBlock.getLocation());
            if (distance < closestDistance) {
                closestDistance = distance;
                closest = holeBlock.getLocation();
            }
        }
        
        return closest;
    }
    
    /**
     * Remove the wall from the world
     */
    public void remove() {
        if (!active) return;
        
        active = false;
        
        // Clear all wall blocks
        for (Block block : wallBlocks) {
            if (block.getType() == wallMaterial) {
                block.setType(Material.AIR);
            }
        }
        
        wallBlocks.clear();
        holeBlocks.clear();
    }
    
    /**
     * Get wall information for debugging
     */
    public String getInfo() {
        return String.format("Wall[%s] Pattern: %s, Speed: %.2f, Blocks: %d, Holes: %d, Age: %d ticks",
            id, pattern.getName(), speed, wallBlocks.size(), holeBlocks.size(), ticksAlive);
    }
    
    // Getters
    
    public String getId() {
        return id;
    }
    
    public Arena getArena() {
        return arena;
    }
    
    public WallPattern getPattern() {
        return pattern;
    }
    
    public Vector getDirection() {
        return direction.clone();
    }
    
    public double getSpeed() {
        return speed;
    }
    
    public Location getCurrentLocation() {
        return currentLocation.clone();
    }
    
    public List<Block> getWallBlocks() {
        return new ArrayList<>(wallBlocks);
    }
    
    public List<Block> getHoleBlocks() {
        return new ArrayList<>(holeBlocks);
    }
    
    public boolean isActive() {
        return active;
    }
    
    public long getSpawnTime() {
        return spawnTime;
    }
    
    public int getTicksAlive() {
        return ticksAlive;
    }
    
    public int getAge() {
        return ticksAlive;
    }
    
    public BoundingBox getBoundingBox() {
        return boundingBox != null ? boundingBox.clone() : null;
    }
}