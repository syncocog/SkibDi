package com.voidlight.event.managers;

import com.voidlight.event.VoidlightEventPlugin;
import com.voidlight.event.models.Match;
import com.voidlight.event.models.MatchState;
import com.voidlight.event.models.Team;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scoreboard.*;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Manages real-time scoreboards for all players during events
 */
public class ScoreboardManager {
    
    private final VoidlightEventPlugin plugin;
    private final Map<UUID, Scoreboard> playerScoreboards;
    private BukkitRunnable updateTask;
    
    public ScoreboardManager(VoidlightEventPlugin plugin) {
        this.plugin = plugin;
        this.playerScoreboards = new HashMap<>();
    }
    
    /**
     * Start the scoreboard update task
     */
    public void startUpdateTask() {
        if (updateTask != null) {
            updateTask.cancel();
        }
        
        updateTask = new BukkitRunnable() {
            @Override
            public void run() {
                updateAllScoreboards();
            }
        };
        
        updateTask.runTaskTimer(plugin, 0L, 20L); // Update every second
    }
    
    /**
     * Stop the scoreboard update task
     */
    public void stopUpdateTask() {
        if (updateTask != null) {
            updateTask.cancel();
            updateTask = null;
        }
    }
    
    /**
     * Update all player scoreboards
     */
    public void updateAllScoreboards() {
        for (Player player : Bukkit.getOnlinePlayers()) {
            updateScoreboard(player);
        }
    }
    
    /**
     * Create scoreboard for a specific player
     */
    public void createScoreboard(Player player) {
        org.bukkit.scoreboard.ScoreboardManager bukkitManager = Bukkit.getScoreboardManager();
        if (bukkitManager == null) return;
        
        Scoreboard scoreboard = bukkitManager.getNewScoreboard();
        
        // Create title with color support
        String title = plugin.getMessageUtil().translateColors("&c&lVoidlight &7&lEvent");
        
        Objective objective = scoreboard.registerNewObjective(
            "voidlight", 
            "dummy", 
            title
        );
        objective.setDisplaySlot(DisplaySlot.SIDEBAR);
        
        // Set initial scores
        updateScoreboardContent(player, scoreboard, objective);
        
        // Store and apply
        playerScoreboards.put(player.getUniqueId(), scoreboard);
        player.setScoreboard(scoreboard);
    }
    
    /**
     * Update scoreboard for a specific player
     */
    public void updateScoreboard(Player player) {
        Scoreboard scoreboard = playerScoreboards.get(player.getUniqueId());
        if (scoreboard == null) {
            createScoreboard(player);
            return;
        }
        
        Objective objective = scoreboard.getObjective("voidlight");
        if (objective == null) {
            createScoreboard(player);
            return;
        }
        
        updateScoreboardContent(player, scoreboard, objective);
    }
    
    /**
     * Update the content of a scoreboard
     */
    private void updateScoreboardContent(Player player, Scoreboard scoreboard, Objective objective) {
        // Clear existing scores
        for (String entry : scoreboard.getEntries()) {
            scoreboard.resetScores(entry);
        }
        
        Match match = plugin.getMatchManager().getCurrentMatch();
        int line = 15;
        
        // Empty line
        setScore(objective, " ", line--);
        
        if (match == null) {
            // No active match
            setScore(objective, plugin.getMessageUtil().translateColors("&7Status: &aWaiting"), line--);
            setScore(objective, plugin.getMessageUtil().translateColors("&7Players in queue: &e" + 
                plugin.getQueueManager().getQueueSize()), line--);
        } else {
            // Active match information
            MatchState state = match.getState();
            setScore(objective, plugin.getMessageUtil().translateColors("&7Status: &e" + state.getDisplayName()), line--);
            setScore(objective, " ", line--);
            
            // Team information
            setScore(objective, plugin.getMessageUtil().translateColors("&c&lRed Team:"), line--);
            setScore(objective, plugin.getMessageUtil().translateColors("&7Alive: &c" + 
                match.getRedAlive().size() + "&7/&c" + match.getRedTeam().size()), line--);
            setScore(objective, " ", line--);
            
            setScore(objective, plugin.getMessageUtil().translateColors("&9&lBlue Team:"), line--);
            setScore(objective, plugin.getMessageUtil().translateColors("&7Alive: &9" + 
                match.getBlueAlive().size() + "&7/&9" + match.getBlueTeam().size()), line--);
            setScore(objective, "  ", line--);
            
            // Player role
            UUID playerUUID = player.getUniqueId();
            String role;
            if (match.getRedTeam().contains(playerUUID) || match.getBlueTeam().contains(playerUUID)) {
                if (match.getRedTeam().contains(playerUUID)) {
                    role = "&c&lFighter &7(Red)";
                } else {
                    role = "&9&lFighter &7(Blue)";
                }
            } else {
                role = "&7&lSpectator";
            }
            setScore(objective, plugin.getMessageUtil().translateColors("&7Role: " + role), line--);
        }
        
        // Empty line
        setScore(objective, "   ", line--);
        
        // Server info
        setScore(objective, plugin.getMessageUtil().translateColors("&7Online: &a" + 
            Bukkit.getOnlinePlayers().size()), line--);
    }
    
    /**
     * Set a score for an objective
     */
    private void setScore(Objective objective, String text, int score) {
        Score scoreObj = objective.getScore(text);
        scoreObj.setScore(score);
    }
    
    /**
     * Remove scoreboard for a player
     */
    public void removeScoreboard(Player player) {
        playerScoreboards.remove(player.getUniqueId());
        
        // Reset to main scoreboard
        org.bukkit.scoreboard.ScoreboardManager bukkitManager = Bukkit.getScoreboardManager();
        if (bukkitManager != null) {
            player.setScoreboard(bukkitManager.getMainScoreboard());
        }
    }
    
    /**
     * Remove all scoreboards
     */
    public void removeAllScoreboards() {
        org.bukkit.scoreboard.ScoreboardManager bukkitManager = Bukkit.getScoreboardManager();
        if (bukkitManager == null) return;
        
        Scoreboard mainScoreboard = bukkitManager.getMainScoreboard();
        
        for (Player player : Bukkit.getOnlinePlayers()) {
            player.setScoreboard(mainScoreboard);
        }
        
        playerScoreboards.clear();
    }
    
    /**
     * Show victory message on all scoreboards
     */
    public void showVictory(Team winningTeam) {
        for (Player player : Bukkit.getOnlinePlayers()) {
            String message = winningTeam == Team.RED ? 
                "&c&l🎉 RED TEAM WINS! 🎉" : 
                "&9&l🎉 BLUE TEAM WINS! 🎉";
            
            plugin.getMessageUtil().sendTitle(player, 
                plugin.getMessageUtil().translateColors(message), 
                "", 10, 60, 10);
        }
    }
    
    /**
     * Show countdown on all scoreboards
     */
    public void showCountdown(int seconds) {
        for (Player player : Bukkit.getOnlinePlayers()) {
            plugin.getMessageUtil().sendTitle(player,
                plugin.getMessageUtil().translateColors("&e&lMatch Starting"),
                plugin.getMessageUtil().translateColors("&6&l" + seconds),
                5, 15, 5);
        }
    }
}