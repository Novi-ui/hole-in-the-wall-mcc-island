package com.hitw.game;

import org.bukkit.entity.Player;

/**
 * Represents a player participating in a game
 */
public class GamePlayer {
    
    private final Player player;
    private boolean alive;
    private boolean eliminated;
    private int eliminationRound;
    private int score;
    private long survivalTime;
    private int wallsDodged;
    
    public GamePlayer(Player player) {
        this.player = player;
        this.alive = true;
        this.eliminated = false;
        this.eliminationRound = -1;
        this.score = 0;
        this.survivalTime = 0;
        this.wallsDodged = 0;
    }
    
    public Player getPlayer() {
        return player;
    }
    
    public boolean isAlive() {
        return alive && !eliminated;
    }
    
    public void setEliminated(boolean eliminated, int round) {
        this.eliminated = eliminated;
        this.alive = !eliminated;
        this.eliminationRound = round;
    }
    
    public boolean isEliminated() {
        return eliminated;
    }
    
    public int getEliminationRound() {
        return eliminationRound;
    }
    
    public int getScore() {
        return score;
    }
    
    public void addScore(int points) {
        this.score += points;
    }
    
    public long getSurvivalTime() {
        return survivalTime;
    }
    
    public void setSurvivalTime(long time) {
        this.survivalTime = time;
    }
    
    public int getWallsDodged() {
        return wallsDodged;
    }
    
    public void addWallDodged() {
        this.wallsDodged++;
    }
}