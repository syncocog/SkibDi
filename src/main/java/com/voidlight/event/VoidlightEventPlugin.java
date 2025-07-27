package com.voidlight.event;

import com.voidlight.event.commands.EventCommand;
import com.voidlight.event.database.DatabaseManager;
import com.voidlight.event.listeners.PlayerListener;
import com.voidlight.event.managers.*;
import com.voidlight.event.utils.ConfigUtil;
import com.voidlight.event.utils.MessageUtil;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;

/**
 * Main plugin class for VoidlightEvent
 * Handles initialization of all managers and systems
 */
public class VoidlightEventPlugin extends JavaPlugin {
    
    private static VoidlightEventPlugin instance;
    
    // Managers
    private DatabaseManager databaseManager;
    private QueueManager queueManager;
    private MatchManager matchManager;
    private KitManager kitManager;
    private SpectatorManager spectatorManager;
    private ScoreboardManager scoreboardManager;
    private ConfigUtil configUtil;
    private MessageUtil messageUtil;
    
    @Override
    public void onEnable() {
        instance = this;
        
        // Save default config and messages
        saveDefaultConfig();
        if (!new File(getDataFolder(), "messages.yml").exists()) {
            saveResource("messages.yml", false);
        }
        
        // Initialize utilities
        configUtil = new ConfigUtil(this);
        messageUtil = new MessageUtil(this);
        
        // Initialize database
        databaseManager = new DatabaseManager(this);
        if (!databaseManager.initialize()) {
            getLogger().severe("Failed to initialize database! Disabling plugin...");
            getServer().getPluginManager().disablePlugin(this);
            return;
        }
        
        // Initialize managers
        queueManager = new QueueManager(this);
        kitManager = new KitManager(this);
        spectatorManager = new SpectatorManager(this);
        scoreboardManager = new ScoreboardManager(this);
        matchManager = new MatchManager(this);
        
        // Register command and tab completer
        EventCommand eventCommand = new EventCommand(this);
        getCommand("event").setExecutor(eventCommand);
        getCommand("event").setTabCompleter(eventCommand);
        
        // Register listeners
        getServer().getPluginManager().registerEvents(new PlayerListener(this), this);
        
        getLogger().info("VoidlightEvent has been enabled!");
    }
    
    @Override
    public void onDisable() {
        // Cancel any ongoing matches
        if (matchManager != null && matchManager.isMatchActive()) {
            matchManager.cancelMatch();
        }
        
        // Shutdown database
        if (databaseManager != null) {
            databaseManager.shutdown();
        }
        
        getLogger().info("VoidlightEvent has been disabled!");
    }
    
    // Getters for managers
    public static VoidlightEventPlugin getInstance() {
        return instance;
    }
    
    public DatabaseManager getDatabaseManager() {
        return databaseManager;
    }
    
    public QueueManager getQueueManager() {
        return queueManager;
    }
    
    public MatchManager getMatchManager() {
        return matchManager;
    }
    
    public KitManager getKitManager() {
        return kitManager;
    }
    
    public SpectatorManager getSpectatorManager() {
        return spectatorManager;
    }
    
    public ScoreboardManager getScoreboardManager() {
        return scoreboardManager;
    }
    
    public ConfigUtil getConfigUtil() {
        return configUtil;
    }
    
    public MessageUtil getMessageUtil() {
        return messageUtil;
    }
}