package com.voidlight.event.models;

import org.bukkit.entity.Player;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Represents an active match between two teams
 */
public class Match {
    
    private final List<UUID> redTeam;
    private final List<UUID> blueTeam;
    private final List<UUID> redAlive;
    private final List<UUID> blueAlive;
    private final LocalDateTime startTime;
    private LocalDateTime endTime;
    private Team winningTeam;
    private MatchState state;
    
    public Match(List<Player> redPlayers, List<Player> bluePlayers) {
        this.redTeam = new ArrayList<>();
        this.blueTeam = new ArrayList<>();
        this.redAlive = new ArrayList<>();
        this.blueAlive = new ArrayList<>();
        
        // Add red team players
        for (Player player : redPlayers) {
            UUID uuid = player.getUniqueId();
            this.redTeam.add(uuid);
            this.redAlive.add(uuid);
        }
        
        // Add blue team players
        for (Player player : bluePlayers) {
            UUID uuid = player.getUniqueId();
            this.blueTeam.add(uuid);
            this.blueAlive.add(uuid);
        }
        
        this.startTime = LocalDateTime.now();
        this.state = MatchState.COUNTDOWN;
    }
    
    // Getters
    public List<UUID> getRedTeam() {
        return new ArrayList<>(redTeam);
    }
    
    public List<UUID> getBlueTeam() {
        return new ArrayList<>(blueTeam);
    }
    
    public List<UUID> getRedAlive() {
        return new ArrayList<>(redAlive);
    }
    
    public List<UUID> getBlueAlive() {
        return new ArrayList<>(blueAlive);
    }
    
    public LocalDateTime getStartTime() {
        return startTime;
    }
    
    public LocalDateTime getEndTime() {
        return endTime;
    }
    
    public Team getWinningTeam() {
        return winningTeam;
    }
    
    public MatchState getState() {
        return state;
    }
    
    public void setState(MatchState state) {
        this.state = state;
    }
    
    // Team operations
    public Team getPlayerTeam(UUID playerId) {
        if (redTeam.contains(playerId)) {
            return Team.RED;
        } else if (blueTeam.contains(playerId)) {
            return Team.BLUE;
        }
        return null;
    }
    
    public boolean isPlayerAlive(UUID playerId) {
        return redAlive.contains(playerId) || blueAlive.contains(playerId);
    }
    
    public void eliminatePlayer(UUID playerId) {
        redAlive.remove(playerId);
        blueAlive.remove(playerId);
    }
    
    public boolean hasTeamWon() {
        return redAlive.isEmpty() || blueAlive.isEmpty();
    }
    
    public Team getWinner() {
        if (redAlive.isEmpty() && !blueAlive.isEmpty()) {
            return Team.BLUE;
        } else if (blueAlive.isEmpty() && !redAlive.isEmpty()) {
            return Team.RED;
        }
        return null;
    }
    
    public void endMatch() {
        this.endTime = LocalDateTime.now();
        this.winningTeam = getWinner();
        this.state = MatchState.ENDED;
    }
    
    public List<UUID> getAllPlayers() {
        List<UUID> allPlayers = new ArrayList<>();
        allPlayers.addAll(redTeam);
        allPlayers.addAll(blueTeam);
        return allPlayers;
    }
    
    public int getRedTeamSize() {
        return redTeam.size();
    }
    
    public int getBlueTeamSize() {
        return blueTeam.size();
    }
    
    public int getRedAliveCount() {
        return redAlive.size();
    }
    
    public int getBlueAliveCount() {
        return blueAlive.size();
    }
}