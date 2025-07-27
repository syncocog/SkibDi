package com.voidlight.event.managers;

import com.voidlight.event.VoidlightEventPlugin;
import org.bukkit.entity.Player;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

/**
 * Manages the player queue system
 * All players are automatically added to queue on join
 */
public class QueueManager {
    
    private final VoidlightEventPlugin plugin;
    private final Set<UUID> queuedPlayers;
    
    public QueueManager(VoidlightEventPlugin plugin) {
        this.plugin = plugin;
        this.queuedPlayers = new HashSet<>();
    }
    
    /**
     * Add a player to the queue
     */
    public void addToQueue(Player player) {
        queuedPlayers.add(player.getUniqueId());
        plugin.getMessageUtil().sendConfigMessage(player, "player-joined-queue");
    }
    
    /**
     * Remove a player from the queue
     */
    public void removeFromQueue(Player player) {
        queuedPlayers.remove(player.getUniqueId());
    }
    
    /**
     * Remove a player from the queue by UUID
     */
    public void removeFromQueue(UUID playerId) {
        queuedPlayers.remove(playerId);
    }
    
    /**
     * Check if a player is in the queue
     */
    public boolean isInQueue(Player player) {
        return queuedPlayers.contains(player.getUniqueId());
    }
    
    /**
     * Check if a player is in the queue by UUID
     */
    public boolean isInQueue(UUID playerId) {
        return queuedPlayers.contains(playerId);
    }
    
    /**
     * Get all queued players
     */
    public Set<UUID> getQueuedPlayers() {
        return new HashSet<>(queuedPlayers);
    }
    
    /**
     * Get the number of players in queue
     */
    public int getQueueSize() {
        return queuedPlayers.size();
    }
    
    /**
     * Clear all players from the queue
     */
    public void clearQueue() {
        queuedPlayers.clear();
    }
}