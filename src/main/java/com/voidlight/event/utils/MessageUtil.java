package com.voidlight.event.utils;

import com.voidlight.event.VoidlightEventPlugin;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Collection;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Utility class for handling messages with color support
 */
public class MessageUtil {
    
    private final VoidlightEventPlugin plugin;
    private static final Pattern HEX_PATTERN = Pattern.compile("#([A-Fa-f0-9]{6})");
    
    public MessageUtil(VoidlightEventPlugin plugin) {
        this.plugin = plugin;
    }
    
    /**
     * Send a message to a player with color parsing
     */
    public void sendMessage(Player player, String message) {
        player.sendMessage(translateColors(message));
    }
    
    /**
     * Send a message to a command sender with color parsing
     */
    public void sendMessage(CommandSender sender, String message) {
        sender.sendMessage(translateColors(message));
    }
    
    /**
     * Send a prefixed message
     */
    public void sendPrefixedMessage(CommandSender sender, String message) {
        String prefixedMessage = plugin.getConfigUtil().getPrefix() + message;
        sendMessage(sender, prefixedMessage);
    }
    
    /**
     * Send a configuration message
     */
    public void sendConfigMessage(CommandSender sender, String key) {
        String message = plugin.getConfigUtil().getMessage(key);
        sendPrefixedMessage(sender, message);
    }
    
    /**
     * Send a configuration message with placeholder replacement
     */
    public void sendConfigMessage(CommandSender sender, String key, String placeholder, String replacement) {
        String message = plugin.getConfigUtil().getMessage(key);
        message = message.replace("{" + placeholder + "}", replacement);
        sendPrefixedMessage(sender, message);
    }
    
    /**
     * Broadcast a message to all online players
     */
    public void broadcastMessage(String message) {
        plugin.getServer().broadcastMessage(translateColors(message));
    }
    
    /**
     * Broadcast a prefixed message to all online players
     */
    public void broadcastPrefixedMessage(String message) {
        String prefixedMessage = plugin.getConfigUtil().getPrefix() + message;
        broadcastMessage(prefixedMessage);
    }
    
    /**
     * Broadcast a configuration message
     */
    public void broadcastConfigMessage(String key) {
        String message = plugin.getConfigUtil().getMessage(key);
        broadcastPrefixedMessage(message);
    }
    
    /**
     * Broadcast a configuration message with placeholder replacement
     */
    public void broadcastConfigMessage(String key, String placeholder, String replacement) {
        String message = plugin.getConfigUtil().getMessage(key);
        message = message.replace("{" + placeholder + "}", replacement);
        broadcastPrefixedMessage(message);
    }
    
    /**
     * Broadcast to multiple players
     */
    public void broadcastToPlayers(Collection<Player> players, String message) {
        String colored = translateColors(message);
        for (Player player : players) {
            player.sendMessage(colored);
        }
    }
    
    /**
     * Send title to player (legacy method)
     */
    public void sendTitle(Player player, String title, String subtitle, int fadeIn, int stay, int fadeOut) {
        player.sendTitle(translateColors(title), translateColors(subtitle), fadeIn, stay, fadeOut);
    }
    
    /**
     * Send action bar to player (legacy method)
     */
    public void sendActionBar(Player player, String message) {
        // Use spigot method for action bar
        player.spigot().sendMessage(net.md_5.bungee.api.ChatMessageType.ACTION_BAR, 
            net.md_5.bungee.api.chat.TextComponent.fromLegacyText(translateColors(message)));
    }
    
    /**
     * Broadcast action bar to multiple players
     */
    public void broadcastActionBar(Collection<Player> players, String message) {
        for (Player player : players) {
            sendActionBar(player, message);
        }
    }
    
    /**
     * Translate color codes including hex colors to legacy format
     */
    public String translateColors(String message) {
        if (message == null) return "";
        
        // Handle hex colors
        Matcher matcher = HEX_PATTERN.matcher(message);
        StringBuffer sb = new StringBuffer();
        while (matcher.find()) {
            String hex = matcher.group(1);
            matcher.appendReplacement(sb, net.md_5.bungee.api.ChatColor.of("#" + hex).toString());
        }
        matcher.appendTail(sb);
        
        // Handle standard color codes
        return ChatColor.translateAlternateColorCodes('&', 
               translateMiniMessageColors(sb.toString()));
    }
    
    /**
     * Strip colors from text
     */
    public String stripColors(String message) {
        return ChatColor.stripColor(translateColors(message));
    }
    
    /**
     * Convert MiniMessage style colors to standard color codes
     */
    private String translateMiniMessageColors(String message) {
        return message
            .replace("<red>", "&c")
            .replace("<green>", "&a")
            .replace("<blue>", "&9")
            .replace("<yellow>", "&e")
            .replace("<gold>", "&6")
            .replace("<gray>", "&7")
            .replace("<white>", "&f")
            .replace("<black>", "&0")
            .replace("<dark_red>", "&4")
            .replace("<dark_green>", "&2")
            .replace("<dark_blue>", "&1")
            .replace("<dark_purple>", "&5")
            .replace("<dark_aqua>", "&3")
            .replace("<dark_gray>", "&8")
            .replace("<light_purple>", "&d")
            .replace("<aqua>", "&b");
    }
}