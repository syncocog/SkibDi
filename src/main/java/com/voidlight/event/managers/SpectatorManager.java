package com.voidlight.event.managers;

import com.voidlight.event.VoidlightEventPlugin;
import com.voidlight.event.models.Match;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.entity.Player;

import java.util.*;

/**
 * Manages spectator functionality for non-participating players
 */
public class SpectatorManager {
    
    private final VoidlightEventPlugin plugin;
    private final Map<UUID, GameMode> savedGameModes;
    private final Map<UUID, Location> savedLocations;
    private final Set<UUID> spectators;
    
    public SpectatorManager(VoidlightEventPlugin plugin) {
        this.plugin = plugin;
        this.savedGameModes = new HashMap<>();
        this.savedLocations = new HashMap<>();
        this.spectators = new HashSet<>();
    }
    
    /**
     * Set all non-participating players to spectator mode
     */
    public void setSpectatorsForMatch(Match match) {
        List<UUID> participants = match.getAllPlayers();
        
        for (Player player : plugin.getServer().getOnlinePlayers()) {
            UUID playerId = player.getUniqueId();
            
            if (!participants.contains(playerId)) {
                setSpectatorMode(player);
            }
        }
    }
    
    /**
     * Set a player to spectator mode
     */
    public void setSpectatorMode(Player player) {
        UUID playerId = player.getUniqueId();
        
        // Save current state
        savedGameModes.put(playerId, player.getGameMode());
        savedLocations.put(playerId, player.getLocation());
        
        // Set to spectator
        player.setGameMode(GameMode.SPECTATOR);
        spectators.add(playerId);
        
        // Teleport to arena center or first fighter
        teleportToArena(player);
        
        // Hide from fighters
        hideFromFighters(player);
    }
    
    /**
     * Restore all players from spectator mode
     */
    public void restoreAllSpectators() {
        Set<UUID> spectatorsCopy = new HashSet<>(spectators);
        
        for (UUID playerId : spectatorsCopy) {
            Player player = plugin.getServer().getPlayer(playerId);
            if (player != null) {
                restorePlayer(player);
            }
        }
        
        spectators.clear();
    }
    
    /**
     * Restore a specific player from spectator mode
     */
    public void restorePlayer(Player player) {
        UUID playerId = player.getUniqueId();
        
        // Restore game mode
        if (savedGameModes.containsKey(playerId)) {
            player.setGameMode(savedGameModes.get(playerId));
            savedGameModes.remove(playerId);
        } else {
            player.setGameMode(GameMode.SURVIVAL);
        }
        
        // Restore location (lobby spawn)
        Location lobbySpawn = plugin.getConfigUtil().getLobbySpawn();
        if (lobbySpawn != null) {
            player.teleport(lobbySpawn);
        } else if (savedLocations.containsKey(playerId)) {
            player.teleport(savedLocations.get(playerId));
            savedLocations.remove(playerId);
        }
        
        // Show to all players
        showToAllPlayers(player);
        
        spectators.remove(playerId);
    }
    
    /**
     * Teleport spectator to a random fighter
     */
    public void spectateRandomFighter(Player spectator) {
        Match currentMatch = plugin.getMatchManager().getCurrentMatch();
        if (currentMatch == null) return;
        
        List<UUID> allFighters = currentMatch.getAllPlayers();
        List<Player> onlineFighters = new ArrayList<>();
        
        for (UUID fighterId : allFighters) {
            Player fighter = plugin.getServer().getPlayer(fighterId);
            if (fighter != null && fighter.isOnline()) {
                onlineFighters.add(fighter);
            }
        }
        
        if (!onlineFighters.isEmpty()) {
            Player targetFighter = onlineFighters.get(
                new Random().nextInt(onlineFighters.size())
            );
            spectator.teleport(targetFighter.getLocation());
            
            plugin.getMessageUtil().sendPrefixedMessage(spectator, 
                "<green>Now spectating <yellow>" + targetFighter.getName());
        }
    }
    
    /**
     * Teleport spectator to a specific fighter
     */
    public void spectatePlayer(Player spectator, Player target) {
        Match currentMatch = plugin.getMatchManager().getCurrentMatch();
        if (currentMatch == null) return;
        
        if (currentMatch.getAllPlayers().contains(target.getUniqueId())) {
            spectator.teleport(target.getLocation());
            plugin.getMessageUtil().sendPrefixedMessage(spectator, 
                "<green>Now spectating <yellow>" + target.getName());
        } else {
            plugin.getMessageUtil().sendPrefixedMessage(spectator, 
                "<red>That player is not in the current match!");
        }
    }
    
    /**
     * Teleport spectator to arena center or first available fighter
     */
    private void teleportToArena(Player spectator) {
        Match currentMatch = plugin.getMatchManager().getCurrentMatch();
        if (currentMatch != null) {
            List<UUID> fighters = currentMatch.getAllPlayers();
            if (!fighters.isEmpty()) {
                UUID firstFighter = fighters.get(0);
                Player fighterPlayer = plugin.getServer().getPlayer(firstFighter);
                if (fighterPlayer != null) {
                    spectator.teleport(fighterPlayer.getLocation());
                }
            }
        }
    }
    
    /**
     * Hide spectator from all fighters
     */
    private void hideFromFighters(Player spectator) {
        Match currentMatch = plugin.getMatchManager().getCurrentMatch();
        if (currentMatch != null) {
            for (UUID fighterId : currentMatch.getAllPlayers()) {
                Player fighter = plugin.getServer().getPlayer(fighterId);
                if (fighter != null) {
                    fighter.hidePlayer(plugin, spectator);
                }
            }
        }
    }
    
    /**
     * Show player to all other players
     */
    private void showToAllPlayers(Player player) {
        for (Player onlinePlayer : plugin.getServer().getOnlinePlayers()) {
            if (!onlinePlayer.equals(player)) {
                onlinePlayer.showPlayer(plugin, player);
            }
        }
    }
    
    /**
     * Check if a player is currently a spectator
     */
    public boolean isSpectator(Player player) {
        return spectators.contains(player.getUniqueId());
    }
    
    /**
     * Get all current spectators
     */
    public Set<UUID> getSpectators() {
        return new HashSet<>(spectators);
    }
    
    /**
     * Get the number of spectators
     */
    public int getSpectatorCount() {
        return spectators.size();
    }
}