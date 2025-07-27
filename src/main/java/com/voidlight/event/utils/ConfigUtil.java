package com.voidlight.event.utils;

import com.voidlight.event.VoidlightEventPlugin;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

/**
 * Utility class for handling configuration values
 */
public class ConfigUtil {
    
    private final VoidlightEventPlugin plugin;
    private FileConfiguration messagesConfig;
    
    public ConfigUtil(VoidlightEventPlugin plugin) {
        this.plugin = plugin;
        loadMessagesConfig();
    }
    
    private void loadMessagesConfig() {
        File messagesFile = new File(plugin.getDataFolder(), "messages.yml");
        if (!messagesFile.exists()) {
            plugin.saveResource("messages.yml", false);
        }
        messagesConfig = YamlConfiguration.loadConfiguration(messagesFile);
        
        // Load defaults from jar
        InputStream defConfigStream = plugin.getResource("messages.yml");
        if (defConfigStream != null) {
            YamlConfiguration defConfig = YamlConfiguration.loadConfiguration(new InputStreamReader(defConfigStream));
            messagesConfig.setDefaults(defConfig);
        }
    }
    
    // Database configuration
    public String getDatabaseHost() {
        return plugin.getConfig().getString("database.host", "localhost");
    }
    
    public int getDatabasePort() {
        return plugin.getConfig().getInt("database.port", 3306);
    }
    
    public String getDatabaseName() {
        return plugin.getConfig().getString("database.database", "voidlight_event");
    }
    
    public String getDatabaseUsername() {
        return plugin.getConfig().getString("database.username", "root");
    }
    
    public String getDatabasePassword() {
        return plugin.getConfig().getString("database.password", "password");
    }
    
    public int getDatabasePoolSize() {
        return plugin.getConfig().getInt("database.pool-size", 10);
    }
    
    // Event configuration
    public int getCountdownDuration() {
        return plugin.getConfig().getInt("event.countdown-duration", 10);
    }
    
    public int getMaxPlayers() {
        return plugin.getConfig().getInt("event.max-players", 8);
    }
    
    public int getMinPlayers() {
        return plugin.getConfig().getInt("event.min-players", 2);
    }
    
    // Spawn locations
    public Location getLobbySpawn() {
        return getLocationFromConfig("spawn.lobby");
    }
    
    public List<Location> getRedSpawns() {
        return getLocationListFromConfig("spawn.red-spawns");
    }
    
    public List<Location> getBlueSpawns() {
        return getLocationListFromConfig("spawn.blue-spawns");
    }
    
    // Helper methods for location parsing
    private Location getLocationFromConfig(String path) {
        ConfigurationSection section = plugin.getConfig().getConfigurationSection(path);
        if (section == null) return null;
        
        String worldName = section.getString("world");
        World world = Bukkit.getWorld(worldName);
        if (world == null) return null;
        
        double x = section.getDouble("x");
        double y = section.getDouble("y");
        double z = section.getDouble("z");
        float yaw = (float) section.getDouble("yaw");
        float pitch = (float) section.getDouble("pitch");
        
        return new Location(world, x, y, z, yaw, pitch);
    }
    
    private List<Location> getLocationListFromConfig(String path) {
        List<Location> locations = new ArrayList<>();
        List<?> configList = plugin.getConfig().getList(path);
        
        if (configList == null) return locations;
        
        for (Object obj : configList) {
            if (obj instanceof ConfigurationSection section) {
                String worldName = section.getString("world");
                World world = Bukkit.getWorld(worldName);
                if (world == null) continue;
                
                double x = section.getDouble("x");
                double y = section.getDouble("y");
                double z = section.getDouble("z");
                float yaw = (float) section.getDouble("yaw");
                float pitch = (float) section.getDouble("pitch");
                
                locations.add(new Location(world, x, y, z, yaw, pitch));
            }
        }
        
        return locations;
    }
    
    // Messages
    public String getMessage(String key) {
        return messagesConfig.getString(key, key);
    }
    
    public String getPrefix() {
        return "<#FF5555>Voidlight <#AAAAAA>Event <#888888>» ";
    }
}