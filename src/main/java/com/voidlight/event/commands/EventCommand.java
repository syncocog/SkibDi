package com.voidlight.event.commands;

import com.voidlight.event.VoidlightEventPlugin;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Main command handler for /event commands
 */
public class EventCommand implements CommandExecutor, TabCompleter {
    
    private final VoidlightEventPlugin plugin;
    
    public EventCommand(VoidlightEventPlugin plugin) {
        this.plugin = plugin;
    }
    
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length == 0) {
            sendUsage(sender);
            return true;
        }
        
        String subCommand = args[0].toLowerCase();
        
        switch (subCommand) {
            case "start" -> handleStartCommand(sender, args);
            case "cancel" -> handleCancelCommand(sender);
            case "spectate" -> handleSpectateCommand(sender, args);
            default -> sendUsage(sender);
        }
        
        return true;
    }
    
    /**
     * Handle the /event start command
     */
    private void handleStartCommand(CommandSender sender, String[] args) {
        // Check permission
        if (!sender.hasPermission("voidlight.event.admin")) {
            plugin.getMessageUtil().sendConfigMessage(sender, "no-permission");
            return;
        }
        
        // Check if match is already active
        if (plugin.getMatchManager().isMatchActive()) {
            plugin.getMessageUtil().sendConfigMessage(sender, "match-in-progress");
            return;
        }
        
        // Validate arguments (need at least 3 args: "start", player1, player2)
        if (args.length < 3) {
            plugin.getMessageUtil().sendConfigMessage(sender, "invalid-args");
            return;
        }
        
        // Get player names (exclude "start" command)
        List<String> playerNames = Arrays.asList(args).subList(1, args.length);
        
        // Validate player count
        int minPlayers = plugin.getConfigUtil().getMinPlayers();
        int maxPlayers = plugin.getConfigUtil().getMaxPlayers();
        
        if (playerNames.size() < minPlayers || playerNames.size() > maxPlayers) {
            plugin.getMessageUtil().sendPrefixedMessage(sender, 
                "<red>Invalid player count! Must be between " + minPlayers + " and " + maxPlayers + " players.");
            return;
        }
        
        // Validate all players exist and are online
        List<Player> players = new ArrayList<>();
        for (String playerName : playerNames) {
            Player player = Bukkit.getPlayerExact(playerName);
            if (player == null || !player.isOnline()) {
                plugin.getMessageUtil().sendConfigMessage(sender, "player-not-found", 
                    "player", playerName);
                return;
            }
            players.add(player);
        }
        
        // Start the match
        boolean success = plugin.getMatchManager().startMatch(players);
        
        if (success) {
            plugin.getMessageUtil().sendPrefixedMessage(sender, 
                "<green>Match started with " + players.size() + " players!");
            
            // Log to console
            plugin.getLogger().info("Match started by " + sender.getName() + 
                " with players: " + String.join(", ", playerNames));
        } else {
            plugin.getMessageUtil().sendPrefixedMessage(sender, 
                "<red>Failed to start match! Please try again.");
        }
    }
    
    /**
     * Handle the /event cancel command
     */
    private void handleCancelCommand(CommandSender sender) {
        // Check permission
        if (!sender.hasPermission("voidlight.event.admin")) {
            plugin.getMessageUtil().sendConfigMessage(sender, "no-permission");
            return;
        }
        
        // Check if match is active
        if (!plugin.getMatchManager().isMatchActive()) {
            plugin.getMessageUtil().sendConfigMessage(sender, "no-match-active");
            return;
        }
        
        // Cancel the match
        plugin.getMatchManager().cancelMatch();
        
        plugin.getMessageUtil().sendPrefixedMessage(sender, 
            "<yellow>Match has been cancelled!");
        
        // Log to console
        plugin.getLogger().info("Match cancelled by " + sender.getName());
    }
    
    /**
     * Handle the /event spectate command
     */
    private void handleSpectateCommand(CommandSender sender, String[] args) {
        // Must be a player
        if (!(sender instanceof Player player)) {
            sender.sendMessage("This command can only be used by players!");
            return;
        }
        
        // Check if match is active
        if (!plugin.getMatchManager().isMatchActive()) {
            plugin.getMessageUtil().sendConfigMessage(sender, "no-match-active");
            return;
        }
        
        // Check if player is a spectator
        if (!plugin.getSpectatorManager().isSpectator(player)) {
            plugin.getMessageUtil().sendPrefixedMessage(sender, 
                "<red>You must be a spectator to use this command!");
            return;
        }
        
        if (args.length >= 2) {
            // Spectate specific player
            String targetName = args[1];
            Player target = Bukkit.getPlayerExact(targetName);
            
            if (target == null || !target.isOnline()) {
                plugin.getMessageUtil().sendConfigMessage(sender, "player-not-found", 
                    "player", targetName);
                return;
            }
            
            plugin.getSpectatorManager().spectatePlayer(player, target);
        } else {
            // Spectate random fighter
            plugin.getSpectatorManager().spectateRandomFighter(player);
        }
    }
    
    /**
     * Send command usage to sender
     */
    private void sendUsage(CommandSender sender) {
        plugin.getMessageUtil().sendPrefixedMessage(sender, "<yellow>Event Commands:");
        plugin.getMessageUtil().sendMessage(sender, "<gray>• <yellow>/event start <player1> <player2> [player3] ... [player8] <gray>- Start a match");
        plugin.getMessageUtil().sendMessage(sender, "<gray>• <yellow>/event cancel <gray>- Cancel the current match");
        plugin.getMessageUtil().sendMessage(sender, "<gray>• <yellow>/event spectate [player] <gray>- Spectate a fighter");
    }
    
    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        List<String> completions = new ArrayList<>();
        
        if (args.length == 1) {
            // First argument - subcommands
            List<String> subCommands = Arrays.asList("start", "cancel", "spectate");
            completions.addAll(subCommands.stream()
                .filter(sub -> sub.toLowerCase().startsWith(args[0].toLowerCase()))
                .collect(Collectors.toList()));
                
        } else if (args.length >= 2) {
            String subCommand = args[0].toLowerCase();
            
            if ("start".equals(subCommand)) {
                // For start command, suggest online players
                completions.addAll(Bukkit.getOnlinePlayers().stream()
                    .map(Player::getName)
                    .filter(name -> name.toLowerCase().startsWith(args[args.length - 1].toLowerCase()))
                    .collect(Collectors.toList()));
                    
            } else if ("spectate".equals(subCommand) && args.length == 2) {
                // For spectate command, suggest fighters if match is active
                if (plugin.getMatchManager().isMatchActive()) {
                    completions.addAll(plugin.getMatchManager().getCurrentMatch().getAllPlayers().stream()
                        .map(uuid -> Bukkit.getPlayer(uuid))
                        .filter(player -> player != null && player.isOnline())
                        .map(Player::getName)
                        .filter(name -> name.toLowerCase().startsWith(args[1].toLowerCase()))
                        .collect(Collectors.toList()));
                }
            }
        }
        
        return completions;
    }
}