package com.hitw.arena;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.util.BoundingBox;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Represents a game arena with all necessary components
 */
public class Arena {
    
    private final String name;
    private final World world;
    private final Location center;
    private final Location spawnPoint;
    private final int platformSize;
    private final Material platformMaterial;
    private final int barrierHeight;
    private final BoundingBox bounds;
    private final List<Location> wallSpawnPoints;
    
    private boolean enabled;
    private long createdAt;
    
    public Arena(String name, Location center, int platformSize) {
        this.name = name;
        this.world = center.getWorld();
        this.center = center.clone();
        this.platformSize = platformSize;
        this.platformMaterial = Material.STONE;
        this.barrierHeight = 10;
        this.enabled = true;
        this.createdAt = System.currentTimeMillis();
        
        // Calculate spawn point (center of platform)
        this.spawnPoint = center.clone().add(0, 1, 0);
        
        // Calculate bounds
        double halfSize = platformSize / 2.0;
        this.bounds = new BoundingBox(
            center.getX() - halfSize,
            center.getY() - 1,
            center.getZ() - halfSize,
            center.getX() + halfSize,
            center.getY() + barrierHeight,
            center.getZ() + halfSize
        );
        
        // Initialize wall spawn points
        this.wallSpawnPoints = calculateWallSpawnPoints();
    }
    
    /**
     * Calculate wall spawn points around the arena
     */
    private List<Location> calculateWallSpawnPoints() {
        List<Location> points = new ArrayList<>();
        double halfSize = platformSize / 2.0;
        
        // North wall spawn point
        points.add(center.clone().add(0, 1, -halfSize - 5));
        
        // South wall spawn point
        points.add(center.clone().add(0, 1, halfSize + 5));
        
        // East wall spawn point
        points.add(center.clone().add(halfSize + 5, 1, 0));
        
        // West wall spawn point
        points.add(center.clone().add(-halfSize - 5, 1, 0));
        
        return points;
    }
    
    /**
     * Build the arena platform and barriers
     */
    public void build() {
        if (world == null) {
            throw new IllegalStateException("World is null for arena: " + name);
        }
        
        int halfSize = platformSize / 2;
        int centerX = center.getBlockX();
        int centerY = center.getBlockY();
        int centerZ = center.getBlockZ();
        
        // Build platform
        for (int x = centerX - halfSize; x <= centerX + halfSize; x++) {
            for (int z = centerZ - halfSize; z <= centerZ + halfSize; z++) {
                Block block = world.getBlockAt(x, centerY, z);
                block.setType(platformMaterial);
                
                // Clear blocks above platform
                for (int y = centerY + 1; y <= centerY + barrierHeight; y++) {
                    Block aboveBlock = world.getBlockAt(x, y, z);
                    if (aboveBlock.getType() != Material.AIR) {
                        aboveBlock.setType(Material.AIR);
                    }
                }
            }
        }
        
        // Build barriers around platform
        buildBarriers(centerX, centerY, centerZ, halfSize);
    }
    
    /**
     * Build barrier walls around the platform
     */
    private void buildBarriers(int centerX, int centerY, int centerZ, int halfSize) {
        Material barrierMaterial = Material.BARRIER;
        
        // North and South barriers
        for (int x = centerX - halfSize - 1; x <= centerX + halfSize + 1; x++) {
            for (int y = centerY + 1; y <= centerY + barrierHeight; y++) {
                // North barrier
                world.getBlockAt(x, y, centerZ - halfSize - 1).setType(barrierMaterial);
                // South barrier
                world.getBlockAt(x, y, centerZ + halfSize + 1).setType(barrierMaterial);
            }
        }
        
        // East and West barriers
        for (int z = centerZ - halfSize - 1; z <= centerZ + halfSize + 1; z++) {
            for (int y = centerY + 1; y <= centerY + barrierHeight; y++) {
                // East barrier
                world.getBlockAt(centerX + halfSize + 1, y, z).setType(barrierMaterial);
                // West barrier
                world.getBlockAt(centerX - halfSize - 1, y, z).setType(barrierMaterial);
            }
        }
    }
    
    /**
     * Remove the arena (restore original blocks)
     */
    public void remove() {
        if (world == null) {
            return;
        }
        
        int halfSize = platformSize / 2;
        int centerX = center.getBlockX();
        int centerY = center.getBlockY();
        int centerZ = center.getBlockZ();
        
        // Remove platform and barriers
        for (int x = centerX - halfSize - 1; x <= centerX + halfSize + 1; x++) {
            for (int z = centerZ - halfSize - 1; z <= centerZ + halfSize + 1; z++) {
                for (int y = centerY; y <= centerY + barrierHeight; y++) {
                    Block block = world.getBlockAt(x, y, z);
                    block.setType(Material.AIR);
                }
            }
        }
    }
    
    /**
     * Check if a location is within the arena bounds
     */
    public boolean contains(Location location) {
        if (!world.equals(location.getWorld())) {
            return false;
        }
        return bounds.contains(location.toVector());
    }
    
    /**
     * Check if a location is within the platform area
     */
    public boolean isOnPlatform(Location location) {
        if (!world.equals(location.getWorld())) {
            return false;
        }
        
        double halfSize = platformSize / 2.0;
        double x = location.getX();
        double z = location.getZ();
        double y = location.getY();
        
        return x >= center.getX() - halfSize &&
               x <= center.getX() + halfSize &&
               z >= center.getZ() - halfSize &&
               z <= center.getZ() + halfSize &&
               y >= center.getY() &&
               y <= center.getY() + barrierHeight;
    }
    
    /**
     * Get distance from arena edge
     */
    public double getDistanceFromEdge(Location location) {
        if (!world.equals(location.getWorld())) {
            return Double.MAX_VALUE;
        }
        
        double halfSize = platformSize / 2.0;
        double x = location.getX() - center.getX();
        double z = location.getZ() - center.getZ();
        
        double distanceX = Math.max(0, Math.abs(x) - halfSize);
        double distanceZ = Math.max(0, Math.abs(z) - halfSize);
        
        return Math.sqrt(distanceX * distanceX + distanceZ * distanceZ);
    }
    
    /**
     * Validate arena integrity
     */
    public boolean isValid() {
        if (world == null || !world.isLoaded()) {
            return false;
        }
        
        if (spawnPoint == null || center == null) {
            return false;
        }
        
        if (platformSize < 5 || platformSize > 50) {
            return false;
        }
        
        return enabled;
    }
    
    /**
     * Get arena statistics summary
     */
    public String getInfo() {
        return String.format(
            "Arena: %s | World: %s | Size: %dx%d | Center: %d,%d,%d | Enabled: %s",
            name,
            world != null ? world.getName() : "null",
            platformSize,
            platformSize,
            center.getBlockX(),
            center.getBlockY(),
            center.getBlockZ(),
            enabled
        );
    }
    
    // Getters and setters
    
    public String getName() {
        return name;
    }
    
    public World getWorld() {
        return world;
    }
    
    public Location getCenter() {
        return center.clone();
    }
    
    public Location getSpawnPoint() {
        return spawnPoint.clone();
    }
    
    public int getPlatformSize() {
        return platformSize;
    }
    
    public Material getPlatformMaterial() {
        return platformMaterial;
    }
    
    public int getBarrierHeight() {
        return barrierHeight;
    }
    
    public BoundingBox getBounds() {
        return bounds.clone();
    }
    
    public List<Location> getWallSpawnPoints() {
        return new ArrayList<>(wallSpawnPoints);
    }
    
    public boolean isEnabled() {
        return enabled;
    }
    
    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }
    
    public long getCreatedAt() {
        return createdAt;
    }
    
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        Arena arena = (Arena) obj;
        return Objects.equals(name, arena.name);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(name);
    }
    
    @Override
    public String toString() {
        return "Arena{" +
                "name='" + name + '\'' +
                ", world=" + (world != null ? world.getName() : "null") +
                ", center=" + center +
                ", platformSize=" + platformSize +
                ", enabled=" + enabled +
                '}';
    }
}