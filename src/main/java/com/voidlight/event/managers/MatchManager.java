package com.voidlight.event.managers;

import com.voidlight.event.VoidlightEventPlugin;
import com.voidlight.event.models.Match;
import com.voidlight.event.models.MatchState;
import com.voidlight.event.models.Team;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Main manager for handling match lifecycle, countdown, and victory detection
 */
public class MatchManager {
    
    private final VoidlightEventPlugin plugin;
    private Match currentMatch;
    private BukkitTask countdownTask;
    private BukkitTask victoryCheckTask;
    
    public MatchManager(VoidlightEventPlugin plugin) {
        this.plugin = plugin;
    }
    
    /**
     * Start a new match with the specified players
     */
    public boolean startMatch(List<Player> players) {
        if (currentMatch != null) {
            return false; // Match already in progress
        }
        
        // Validate player count
        int playerCount = players.size();
        if (playerCount < plugin.getConfigUtil().getMinPlayers() || 
            playerCount > plugin.getConfigUtil().getMaxPlayers()) {
            return false;
        }
        
        // Split players into teams
        List<Player> redTeam = new ArrayList<>();
        List<Player> blueTeam = new ArrayList<>();
        
        for (int i = 0; i < playerCount; i++) {
            if (i % 2 == 0) {
                redTeam.add(players.get(i));
            } else {
                blueTeam.add(players.get(i));
            }
        }
        
        // Create match
        currentMatch = new Match(redTeam, blueTeam);
        
        // Set up spectators
        plugin.getSpectatorManager().setSpectatorsForMatch(currentMatch);
        
        // Show scoreboards
        plugin.getScoreboardManager().startUpdateTask();
        
        // Create scoreboards for all players
        for (Player player : plugin.getServer().getOnlinePlayers()) {
            plugin.getScoreboardManager().createScoreboard(player);
        }
        
        // Start countdown
        startCountdown();
        
        return true;
    }
    
    /**
     * Cancel the current match
     */
    public void cancelMatch() {
        if (currentMatch == null) {
            return;
        }
        
        // Cancel countdown if running
        if (countdownTask != null) {
            countdownTask.cancel();
            countdownTask = null;
        }
        
        // Cancel victory check task
        if (victoryCheckTask != null) {
            victoryCheckTask.cancel();
            victoryCheckTask = null;
        }
        
        // Restore all players
        restoreAllPlayers();
        
        // Hide scoreboards
        plugin.getScoreboardManager().stopUpdateTask();
        plugin.getScoreboardManager().removeAllScoreboards();
        
        // Broadcast cancellation
        plugin.getMessageUtil().broadcastConfigMessage("match-cancelled");
        
        currentMatch = null;
    }
    
    /**
     * Start the match countdown
     */
    private void startCountdown() {
        int countdownDuration = plugin.getConfigUtil().getCountdownDuration();
        
        countdownTask = new BukkitRunnable() {
            int timeLeft = countdownDuration;
            
            @Override
            public void run() {
                if (timeLeft <= 0) {
                    // Start the actual match
                    startActualMatch();
                    cancel();
                    return;
                }
                
                // Broadcast countdown
                if (timeLeft <= 5 || timeLeft % 5 == 0) {
                    plugin.getMessageUtil().broadcastConfigMessage("match-starting", 
                        "countdown", String.valueOf(timeLeft));
                    
                    // Play sound
                    for (Player player : plugin.getServer().getOnlinePlayers()) {
                        player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_PLING, 1.0f, 1.0f);
                    }
                }
                
                // Show title to fighters
                if (timeLeft <= 5) {
                    for (UUID playerId : currentMatch.getAllPlayers()) {
                        Player player = plugin.getServer().getPlayer(playerId);
                        if (player != null) {
                            plugin.getMessageUtil().sendTitle(player, 
                                "<yellow>" + timeLeft, 
                                "<gray>Get ready to fight!", 5, 15, 5);
                        }
                    }
                }
                
                timeLeft--;
            }
        }.runTaskTimer(plugin, 0L, 20L);
    }
    
    /**
     * Start the actual match after countdown
     */
    private void startActualMatch() {
        if (currentMatch == null) return;
        
        // Teleport players to spawn points
        teleportPlayersToSpawns();
        
        // Give kits to all fighters
        giveKitsToFighters();
        
        // Set match state
        currentMatch.setState(MatchState.IN_PROGRESS);
        
        // Broadcast match start
        plugin.getMessageUtil().broadcastConfigMessage("match-started");
        
        // Show start title to fighters
        for (UUID playerId : currentMatch.getAllPlayers()) {
            Player player = plugin.getServer().getPlayer(playerId);
            if (player != null) {
                plugin.getMessageUtil().sendTitle(player, 
                    "<green>FIGHT!", 
                    "<gray>May the best team win!", 10, 40, 10);
                player.playSound(player.getLocation(), Sound.ENTITY_ENDER_DRAGON_GROWL, 1.0f, 1.0f);
            }
        }
        
        // Start victory check task
        startVictoryCheckTask();
    }
    
    /**
     * Teleport players to their team spawn points
     */
    private void teleportPlayersToSpawns() {
        if (currentMatch == null) return;
        
        List<Location> redSpawns = plugin.getConfigUtil().getRedSpawns();
        List<Location> blueSpawns = plugin.getConfigUtil().getBlueSpawns();
        
        // Teleport red team
        List<UUID> redTeam = currentMatch.getRedTeam();
        for (int i = 0; i < redTeam.size(); i++) {
            Player player = plugin.getServer().getPlayer(redTeam.get(i));
            if (player != null && i < redSpawns.size()) {
                player.teleport(redSpawns.get(i));
                player.setGameMode(GameMode.SURVIVAL);
            }
        }
        
        // Teleport blue team
        List<UUID> blueTeam = currentMatch.getBlueTeam();
        for (int i = 0; i < blueTeam.size(); i++) {
            Player player = plugin.getServer().getPlayer(blueTeam.get(i));
            if (player != null && i < blueSpawns.size()) {
                player.teleport(blueSpawns.get(i));
                player.setGameMode(GameMode.SURVIVAL);
            }
        }
    }
    
    /**
     * Give Beast kits to all fighters
     */
    private void giveKitsToFighters() {
        if (currentMatch == null) return;
        
        for (UUID playerId : currentMatch.getAllPlayers()) {
            Player player = plugin.getServer().getPlayer(playerId);
            if (player != null) {
                plugin.getKitManager().giveBeastKit(player);
            }
        }
    }
    
    /**
     * Start the victory check task
     */
    private void startVictoryCheckTask() {
        victoryCheckTask = new BukkitRunnable() {
            @Override
            public void run() {
                if (currentMatch == null) {
                    cancel();
                    return;
                }
                
                if (currentMatch.hasTeamWon()) {
                    endMatch(currentMatch.getWinner());
                    cancel();
                }
            }
        }.runTaskTimer(plugin, 20L, 20L); // Check every second
    }
    
    /**
     * Handle player elimination
     */
    public void eliminatePlayer(Player player) {
        if (currentMatch == null) return;
        
        UUID playerId = player.getUniqueId();
        
        if (currentMatch.getAllPlayers().contains(playerId)) {
            currentMatch.eliminatePlayer(playerId);
            
            // Teleport to lobby
            Location lobbySpawn = plugin.getConfigUtil().getLobbySpawn();
            if (lobbySpawn != null) {
                player.teleport(lobbySpawn);
            }
            
            // Set to spectator
            plugin.getSpectatorManager().setSpectatorMode(player);
            
            // Remove kit
            plugin.getKitManager().removeKit(player);
            
            // Broadcast elimination
            Team team = currentMatch.getPlayerTeam(playerId);
            if (team != null) {
                plugin.getMessageUtil().broadcastPrefixedMessage(
                    team.getColoredName() + " <white>" + player.getName() + 
                    " <gray>has been eliminated!"
                );
            }
            
            // Check for victory immediately
            if (currentMatch.hasTeamWon()) {
                endMatch(currentMatch.getWinner());
            }
        }
    }
    
    /**
     * End the match with a winning team
     */
    private void endMatch(Team winningTeam) {
        if (currentMatch == null) return;
        
        // Stop victory check task
        if (victoryCheckTask != null) {
            victoryCheckTask.cancel();
            victoryCheckTask = null;
        }
        
        // Set match as ended
        currentMatch.endMatch();
        
        // Broadcast victory
        if (winningTeam != null) {
            String victoryMessage = winningTeam == Team.RED ? 
                plugin.getConfigUtil().getMessage("victory-red") :
                plugin.getConfigUtil().getMessage("victory-blue");
            plugin.getMessageUtil().broadcastPrefixedMessage(victoryMessage);
            
            // Show victory title to all players
            for (Player player : plugin.getServer().getOnlinePlayers()) {
                plugin.getMessageUtil().sendTitle(player, 
                    winningTeam.getColoredName() + " <white>Wins!", 
                    "<gray>Congratulations!", 10, 60, 10);
                player.playSound(player.getLocation(), Sound.UI_TOAST_CHALLENGE_COMPLETE, 1.0f, 1.0f);
            }
        }
        
        // Log match to database
        plugin.getDatabaseManager().logMatch(currentMatch);
        
        // Delay restoration to let players see the results
        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            restoreAllPlayers();
            plugin.getScoreboardManager().stopUpdateTask();
            plugin.getScoreboardManager().removeAllScoreboards();
            currentMatch = null;
        }, 60L); // 3 second delay
    }
    
    /**
     * Restore all players to their original state
     */
    private void restoreAllPlayers() {
        if (currentMatch == null) return;
        
        // Restore fighters
        for (UUID playerId : currentMatch.getAllPlayers()) {
            Player player = plugin.getServer().getPlayer(playerId);
            if (player != null) {
                // Teleport to lobby
                Location lobbySpawn = plugin.getConfigUtil().getLobbySpawn();
                if (lobbySpawn != null) {
                    player.teleport(lobbySpawn);
                }
                
                // Remove kit and restore inventory
                plugin.getKitManager().removeKit(player);
                
                // Set to survival
                player.setGameMode(GameMode.SURVIVAL);
            }
        }
        
        // Restore spectators
        plugin.getSpectatorManager().restoreAllSpectators();
    }
    
    // Getters
    public Match getCurrentMatch() {
        return currentMatch;
    }
    
    public boolean isMatchActive() {
        return currentMatch != null;
    }
    
    public boolean isCountdownActive() {
        return countdownTask != null && !countdownTask.isCancelled();
    }
    
    public boolean isMatchInProgress() {
        return currentMatch != null && currentMatch.getState() == MatchState.IN_PROGRESS;
    }
}