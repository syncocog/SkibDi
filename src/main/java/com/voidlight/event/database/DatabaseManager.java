package com.voidlight.event.database;

import com.voidlight.event.VoidlightEventPlugin;
import com.voidlight.event.models.Match;
import com.voidlight.event.models.Team;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.bukkit.Bukkit;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

/**
 * Manages database connections and operations using HikariCP
 */
public class DatabaseManager {
    
    private final VoidlightEventPlugin plugin;
    private HikariDataSource dataSource;
    
    public DatabaseManager(VoidlightEventPlugin plugin) {
        this.plugin = plugin;
    }
    
    /**
     * Initialize the database connection pool
     */
    public boolean initialize() {
        try {
            HikariConfig config = new HikariConfig();
            config.setDriverClassName("com.mysql.cj.jdbc.Driver");
            
            String host = plugin.getConfigUtil().getDatabaseHost();
            int port = plugin.getConfigUtil().getDatabasePort();
            String database = plugin.getConfigUtil().getDatabaseName();
            String username = plugin.getConfigUtil().getDatabaseUsername();
            String password = plugin.getConfigUtil().getDatabasePassword();
            int poolSize = plugin.getConfigUtil().getDatabasePoolSize();
            
            config.setJdbcUrl("jdbc:mysql://" + host + ":" + port + "/" + database + "?useSSL=false&allowPublicKeyRetrieval=true");
            config.setUsername(username);
            config.setPassword(password);
            config.setMaximumPoolSize(poolSize);
            config.setMinimumIdle(1);
            config.setConnectionTimeout(30000);
            config.setIdleTimeout(600000);
            config.setMaxLifetime(1800000);
            
            dataSource = new HikariDataSource(config);
            
            // Create tables if they don't exist
            createTables();
            
            plugin.getLogger().info("Database connection established successfully!");
            return true;
            
        } catch (Exception e) {
            plugin.getLogger().severe("Failed to initialize database: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
    
    /**
     * Create necessary database tables
     */
    private void createTables() {
        String createMatchesTable = """
            CREATE TABLE IF NOT EXISTS voidlight_matches (
                id INT AUTO_INCREMENT PRIMARY KEY,
                start_time TIMESTAMP NOT NULL,
                end_time TIMESTAMP,
                red_team_players TEXT NOT NULL,
                blue_team_players TEXT NOT NULL,
                winning_team VARCHAR(10),
                duration_seconds INT,
                created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
            )
            """;
        
        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(createMatchesTable)) {
            
            statement.executeUpdate();
            plugin.getLogger().info("Database tables created/verified successfully!");
            
        } catch (SQLException e) {
            plugin.getLogger().severe("Failed to create database tables: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * Log a completed match to the database asynchronously
     */
    public CompletableFuture<Void> logMatch(Match match) {
        return CompletableFuture.runAsync(() -> {
            String insertQuery = """
                INSERT INTO voidlight_matches 
                (start_time, end_time, red_team_players, blue_team_players, winning_team, duration_seconds)
                VALUES (?, ?, ?, ?, ?, ?)
                """;
            
            try (Connection connection = dataSource.getConnection();
                 PreparedStatement statement = connection.prepareStatement(insertQuery)) {
                
                // Convert LocalDateTime to Timestamp
                Timestamp startTime = Timestamp.valueOf(match.getStartTime());
                Timestamp endTime = match.getEndTime() != null ? 
                    Timestamp.valueOf(match.getEndTime()) : null;
                
                // Convert UUID lists to comma-separated strings
                String redPlayers = uuidListToString(match.getRedTeam());
                String bluePlayers = uuidListToString(match.getBlueTeam());
                
                // Calculate duration in seconds
                int duration = 0;
                if (match.getEndTime() != null) {
                    duration = (int) java.time.Duration.between(
                        match.getStartTime(), 
                        match.getEndTime()
                    ).getSeconds();
                }
                
                statement.setTimestamp(1, startTime);
                statement.setTimestamp(2, endTime);
                statement.setString(3, redPlayers);
                statement.setString(4, bluePlayers);
                statement.setString(5, match.getWinningTeam() != null ? 
                    match.getWinningTeam().name() : null);
                statement.setInt(6, duration);
                
                statement.executeUpdate();
                
                // Log success on main thread
                Bukkit.getScheduler().runTask(plugin, () -> 
                    plugin.getLogger().info("Match logged to database successfully!"));
                
            } catch (SQLException e) {
                // Log error on main thread
                Bukkit.getScheduler().runTask(plugin, () -> {
                    plugin.getLogger().severe("Failed to log match to database: " + e.getMessage());
                    e.printStackTrace();
                });
            }
        });
    }
    
    /**
     * Convert a list of UUIDs to a comma-separated string
     */
    private String uuidListToString(List<UUID> uuids) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < uuids.size(); i++) {
            if (i > 0) sb.append(",");
            sb.append(uuids.get(i).toString());
        }
        return sb.toString();
    }
    
    /**
     * Shutdown the database connection pool
     */
    public void shutdown() {
        if (dataSource != null && !dataSource.isClosed()) {
            dataSource.close();
            plugin.getLogger().info("Database connection pool closed.");
        }
    }
    
    /**
     * Get a connection from the pool (for advanced usage)
     */
    public Connection getConnection() throws SQLException {
        return dataSource.getConnection();
    }
}