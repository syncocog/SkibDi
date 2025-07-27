package com.voidlight.event.listeners;

import com.voidlight.event.VoidlightEventPlugin;
import org.bukkit.GameMode;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.*;

/**
 * Handles all player-related events for the plugin
 */
public class PlayerListener implements Listener {
    
    private final VoidlightEventPlugin plugin;
    
    public PlayerListener(VoidlightEventPlugin plugin) {
        this.plugin = plugin;
    }
    
    /**
     * Auto-add players to queue when they join
     */
    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        
        // Add to queue
        plugin.getQueueManager().addToQueue(player);
        
        // If match is active, create scoreboard for the new player
        if (plugin.getMatchManager().isMatchActive()) {
            plugin.getScoreboardManager().createScoreboard(player);
            
            // Set as spectator if not participating
            if (!plugin.getMatchManager().getCurrentMatch().getAllPlayers().contains(player.getUniqueId())) {
                plugin.getSpectatorManager().setSpectatorMode(player);
            }
        }
    }
    
    /**
     * Remove players from queue and clean up when they leave
     */
    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        
        // Remove from queue
        plugin.getQueueManager().removeFromQueue(player);
        
        // Handle scoreboard cleanup
        plugin.getScoreboardManager().removeScoreboard(player);
        
        // If player was in active match, eliminate them
        if (plugin.getMatchManager().isMatchActive() && 
            plugin.getMatchManager().getCurrentMatch().getAllPlayers().contains(player.getUniqueId())) {
            plugin.getMatchManager().eliminatePlayer(player);
        }
        
        // Restore spectator if needed
        if (plugin.getSpectatorManager().isSpectator(player)) {
            plugin.getSpectatorManager().restorePlayer(player);
        }
    }
    
    /**
     * Handle player deaths during matches
     */
    @EventHandler(priority = EventPriority.HIGH)
    public void onPlayerDeath(PlayerDeathEvent event) {
        Player player = event.getEntity();
        
        // Check if player is in active match
        if (plugin.getMatchManager().isMatchActive() && 
            plugin.getMatchManager().getCurrentMatch().getAllPlayers().contains(player.getUniqueId())) {
            
            // Prevent normal death behavior
            event.setKeepInventory(true);
            event.setKeepLevel(true);
            event.getDrops().clear();
            event.setDroppedExp(0);
            
            // Clear death message
            event.setDeathMessage(null);
            
            // Eliminate the player
            plugin.getMatchManager().eliminatePlayer(player);
        }
    }
    
    /**
     * Prevent players from dropping items during matches
     */
    @EventHandler(priority = EventPriority.HIGH)
    public void onPlayerDropItem(PlayerDropItemEvent event) {
        Player player = event.getPlayer();
        
        // Prevent dropping items if player has kit
        if (plugin.getKitManager().hasKit(player)) {
            event.setCancelled(true);
            plugin.getMessageUtil().sendMessage(player, 
                "<red>You cannot drop items during a match!");
        }
    }
    
    /**
     * Prevent inventory manipulation during matches
     */
    @EventHandler(priority = EventPriority.HIGH)
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player player)) {
            return;
        }
        
        // Prevent inventory manipulation if player has kit
        if (plugin.getKitManager().hasKit(player)) {
            // Allow clicking on hotbar slots but prevent moving items
            if (event.getSlot() >= 0 && event.getSlot() <= 8) {
                // Allow switching hotbar items but prevent moving them
                if (event.isShiftClick() || event.getClick().isCreativeAction()) {
                    event.setCancelled(true);
                }
            } else {
                // Cancel all other inventory interactions
                event.setCancelled(true);
            }
        }
    }
    
    /**
     * Prevent spectators from interacting with the world
     */
    @EventHandler(priority = EventPriority.HIGH)
    public void onPlayerInteract(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        
        // Prevent spectators from interacting
        if (plugin.getSpectatorManager().isSpectator(player)) {
            event.setCancelled(true);
        }
    }
    
    /**
     * Handle player respawn during matches
     */
    @EventHandler(priority = EventPriority.HIGH)
    public void onPlayerRespawn(PlayerRespawnEvent event) {
        Player player = event.getPlayer();
        
        // If player was eliminated during match, they should already be handled
        // Set respawn location to lobby
        if (plugin.getConfigUtil().getLobbySpawn() != null) {
            event.setRespawnLocation(plugin.getConfigUtil().getLobbySpawn());
        }
    }
    
    /**
     * Prevent command usage during matches (except spectate)
     */
    @EventHandler(priority = EventPriority.HIGH)
    public void onPlayerCommandPreprocess(PlayerCommandPreprocessEvent event) {
        Player player = event.getPlayer();
        String command = event.getMessage().toLowerCase();
        
        // Check if player is in active match
        if (plugin.getMatchManager().isMatchActive() && 
            plugin.getMatchManager().getCurrentMatch().getAllPlayers().contains(player.getUniqueId()) &&
            plugin.getMatchManager().getCurrentMatch().isPlayerAlive(player.getUniqueId())) {
            
            // Allow only essential commands and event spectate
            if (!command.startsWith("/event spectate") && 
                !command.startsWith("/suicide") &&
                !command.startsWith("/kill") &&
                !player.hasPermission("voidlight.event.admin")) {
                
                event.setCancelled(true);
                plugin.getMessageUtil().sendMessage(player, 
                    "<red>Commands are disabled during matches!");
            }
        }
    }
    
    /**
     * Prevent spectators from being damaged
     */
    @EventHandler(priority = EventPriority.HIGH)
    public void onPlayerDamage(org.bukkit.event.entity.EntityDamageEvent event) {
        if (!(event.getEntity() instanceof Player player)) {
            return;
        }
        
        // Cancel damage for spectators
        if (plugin.getSpectatorManager().isSpectator(player)) {
            event.setCancelled(true);
        }
    }
    
    /**
     * Handle player teleportation (prevent teleportation during matches)
     */
    @EventHandler(priority = EventPriority.HIGH)
    public void onPlayerTeleport(PlayerTeleportEvent event) {
        Player player = event.getPlayer();
        
        // Prevent unauthorized teleportation during matches
        if (plugin.getMatchManager().isMatchActive() && 
            plugin.getMatchManager().getCurrentMatch().getAllPlayers().contains(player.getUniqueId()) &&
            plugin.getMatchManager().getCurrentMatch().isPlayerAlive(player.getUniqueId())) {
            
            // Only allow plugin-initiated teleports and admin teleports
            if (event.getCause() != PlayerTeleportEvent.TeleportCause.PLUGIN &&
                !player.hasPermission("voidlight.event.admin")) {
                
                event.setCancelled(true);
                plugin.getMessageUtil().sendMessage(player, 
                    "<red>Teleportation is disabled during matches!");
            }
        }
    }
    
    /**
     * Handle game mode changes (prevent unauthorized changes)
     */
    @EventHandler(priority = EventPriority.HIGH)
    public void onPlayerGameModeChange(PlayerGameModeChangeEvent event) {
        Player player = event.getPlayer();
        
        // Check if player is in active match
        if (plugin.getMatchManager().isMatchActive() && 
            plugin.getMatchManager().getCurrentMatch().getAllPlayers().contains(player.getUniqueId()) &&
            plugin.getMatchManager().getCurrentMatch().isPlayerAlive(player.getUniqueId())) {
            
            // Only allow survival mode during matches
            if (event.getNewGameMode() != GameMode.SURVIVAL && 
                !player.hasPermission("voidlight.event.admin")) {
                
                event.setCancelled(true);
            }
        }
    }
}