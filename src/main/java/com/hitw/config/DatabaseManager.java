package com.hitw.config;

import com.hitw.HoleInTheWallPlugin;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

import java.io.File;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.concurrent.CompletableFuture;
import java.util.logging.Level;

/**
 * Manages database connections and operations
 */
public class DatabaseManager {
    
    private final HoleInTheWallPlugin plugin;
    private HikariDataSource dataSource;
    private boolean mysql;
    
    public DatabaseManager(HoleInTheWallPlugin plugin) {
        this.plugin = plugin;
    }
    
    /**
     * Initialize database connection
     */
    public void initialize() {
        try {
            String dbType = plugin.getConfigManager().getString("database.type", "sqlite");
            mysql = dbType.equalsIgnoreCase("mysql");
            
            if (mysql) {
                setupMySQL();
            } else {
                setupSQLite();
            }
            
            createTables();
            
            plugin.getLogger().info("Database initialized successfully!");
            
        } catch (Exception e) {
            plugin.getLogger().log(Level.SEVERE, "Failed to initialize database!", e);
            throw new RuntimeException("Database initialization failed", e);
        }
    }
    
    /**
     * Setup MySQL connection
     */
    private void setupMySQL() {
        ConfigManager config = plugin.getConfigManager();
        
        HikariConfig hikariConfig = new HikariConfig();
        hikariConfig.setJdbcUrl(String.format("jdbc:mysql://%s:%d/%s",
            config.getString("database.mysql.host", "localhost"),
            config.getInt("database.mysql.port", 3306),
            config.getString("database.mysql.database", "hitw")
        ));
        hikariConfig.setUsername(config.getString("database.mysql.username", "root"));
        hikariConfig.setPassword(config.getString("database.mysql.password", ""));
        
        // Connection pool settings
        hikariConfig.setMaximumPoolSize(config.getInt("database.pool.maximum-pool-size", 10));
        hikariConfig.setMinimumIdle(config.getInt("database.pool.minimum-idle", 2));
        hikariConfig.setConnectionTimeout(config.getInt("database.pool.connection-timeout", 30000));
        hikariConfig.setIdleTimeout(config.getInt("database.pool.idle-timeout", 600000));
        hikariConfig.setMaxLifetime(config.getInt("database.pool.max-lifetime", 1800000));
        
        // MySQL specific settings
        hikariConfig.addDataSourceProperty("cachePrepStmts", "true");
        hikariConfig.addDataSourceProperty("prepStmtCacheSize", "250");
        hikariConfig.addDataSourceProperty("prepStmtCacheSqlLimit", "2048");
        hikariConfig.addDataSourceProperty("useServerPrepStmts", "true");
        hikariConfig.addDataSourceProperty("useLocalSessionState", "true");
        hikariConfig.addDataSourceProperty("rewriteBatchedStatements", "true");
        hikariConfig.addDataSourceProperty("cacheResultSetMetadata", "true");
        hikariConfig.addDataSourceProperty("cacheServerConfiguration", "true");
        hikariConfig.addDataSourceProperty("elideSetAutoCommits", "true");
        hikariConfig.addDataSourceProperty("maintainTimeStats", "false");
        
        dataSource = new HikariDataSource(hikariConfig);
    }
    
    /**
     * Setup SQLite connection
     */
    private void setupSQLite() {
        ConfigManager config = plugin.getConfigManager();
        String fileName = config.getString("database.sqlite.file", "hitw_data.db");
        File databaseFile = new File(plugin.getDataFolder(), fileName);
        
        // Create data folder if it doesn't exist
        if (!plugin.getDataFolder().exists()) {
            plugin.getDataFolder().mkdirs();
        }
        
        HikariConfig hikariConfig = new HikariConfig();
        hikariConfig.setJdbcUrl("jdbc:sqlite:" + databaseFile.getAbsolutePath());
        hikariConfig.setDriverClassName("org.sqlite.JDBC");
        
        // SQLite specific settings
        hikariConfig.addDataSourceProperty("journal_mode", "WAL");
        hikariConfig.addDataSourceProperty("synchronous", "NORMAL");
        hikariConfig.addDataSourceProperty("cache_size", "10000");
        hikariConfig.addDataSourceProperty("temp_store", "MEMORY");
        
        // Connection pool settings (SQLite doesn't need large pools)
        hikariConfig.setMaximumPoolSize(1);
        hikariConfig.setMinimumIdle(1);
        hikariConfig.setConnectionTimeout(30000);
        
        dataSource = new HikariDataSource(hikariConfig);
    }
    
    /**
     * Create database tables
     */
    private void createTables() throws SQLException {
        try (Connection conn = getConnection()) {
            // Players table
            execute(conn, """
                CREATE TABLE IF NOT EXISTS hitw_players (
                    uuid VARCHAR(36) PRIMARY KEY,
                    name VARCHAR(16) NOT NULL,
                    games_played INT DEFAULT 0,
                    games_won INT DEFAULT 0,
                    total_time_survived INT DEFAULT 0,
                    walls_dodged INT DEFAULT 0,
                    best_score INT DEFAULT 0,
                    current_win_streak INT DEFAULT 0,
                    best_win_streak INT DEFAULT 0,
                    achievements TEXT DEFAULT '',
                    first_played BIGINT DEFAULT 0,
                    last_played BIGINT DEFAULT 0,
                    created_at BIGINT DEFAULT 0
                )
            """);
            
            // Games table
            execute(conn, """
                CREATE TABLE IF NOT EXISTS hitw_games (
                    id INT AUTO_INCREMENT PRIMARY KEY,
                    arena_name VARCHAR(32) NOT NULL,
                    game_mode VARCHAR(16) NOT NULL,
                    start_time BIGINT NOT NULL,
                    end_time BIGINT NOT NULL,
                    duration INT NOT NULL,
                    winner_uuid VARCHAR(36),
                    player_count INT NOT NULL,
                    walls_spawned INT DEFAULT 0,
                    max_round INT DEFAULT 0
                )
            """);
            
            // Game participants table
            execute(conn, """
                CREATE TABLE IF NOT EXISTS hitw_game_participants (
                    id INT AUTO_INCREMENT PRIMARY KEY,
                    game_id INT NOT NULL,
                    player_uuid VARCHAR(36) NOT NULL,
                    player_name VARCHAR(16) NOT NULL,
                    final_score INT DEFAULT 0,
                    survival_time INT DEFAULT 0,
                    walls_dodged INT DEFAULT 0,
                    placement INT DEFAULT 0,
                    eliminated_round INT DEFAULT 0,
                    FOREIGN KEY (game_id) REFERENCES hitw_games(id)
                )
            """);
            
            // Arena statistics table
            execute(conn, """
                CREATE TABLE IF NOT EXISTS hitw_arena_stats (
                    arena_name VARCHAR(32) PRIMARY KEY,
                    total_games INT DEFAULT 0,
                    total_players INT DEFAULT 0,
                    average_duration DOUBLE DEFAULT 0,
                    longest_game INT DEFAULT 0,
                    most_walls INT DEFAULT 0,
                    created_at BIGINT DEFAULT 0
                )
            """);
            
            // Daily statistics table
            execute(conn, """
                CREATE TABLE IF NOT EXISTS hitw_daily_stats (
                    date VARCHAR(10) NOT NULL,
                    player_uuid VARCHAR(36) NOT NULL,
                    games_played INT DEFAULT 0,
                    games_won INT DEFAULT 0,
                    total_score INT DEFAULT 0,
                    total_survival_time INT DEFAULT 0,
                    PRIMARY KEY (date, player_uuid)
                )
            """);
            
            // Leaderboards cache table
            execute(conn, """
                CREATE TABLE IF NOT EXISTS hitw_leaderboards (
                    type VARCHAR(32) PRIMARY KEY,
                    data TEXT NOT NULL,
                    last_updated BIGINT NOT NULL
                )
            """);
            
            // Fix AUTO_INCREMENT for SQLite
            if (!mysql) {
                execute(conn, "CREATE TABLE IF NOT EXISTS hitw_games_temp AS SELECT * FROM hitw_games");
                execute(conn, "DROP TABLE hitw_games");
                execute(conn, """
                    CREATE TABLE hitw_games (
                        id INTEGER PRIMARY KEY AUTOINCREMENT,
                        arena_name VARCHAR(32) NOT NULL,
                        game_mode VARCHAR(16) NOT NULL,
                        start_time BIGINT NOT NULL,
                        end_time BIGINT NOT NULL,
                        duration INT NOT NULL,
                        winner_uuid VARCHAR(36),
                        player_count INT NOT NULL,
                        walls_spawned INT DEFAULT 0,
                        max_round INT DEFAULT 0
                    )
                """);
                
                execute(conn, "CREATE TABLE IF NOT EXISTS hitw_game_participants_temp AS SELECT * FROM hitw_game_participants");
                execute(conn, "DROP TABLE hitw_game_participants");
                execute(conn, """
                    CREATE TABLE hitw_game_participants (
                        id INTEGER PRIMARY KEY AUTOINCREMENT,
                        game_id INT NOT NULL,
                        player_uuid VARCHAR(36) NOT NULL,
                        player_name VARCHAR(16) NOT NULL,
                        final_score INT DEFAULT 0,
                        survival_time INT DEFAULT 0,
                        walls_dodged INT DEFAULT 0,
                        placement INT DEFAULT 0,
                        eliminated_round INT DEFAULT 0
                    )
                """);
            }
            
            plugin.getLogger().info("Database tables created successfully!");
        }
    }
    
    /**
     * Execute SQL statement
     */
    private void execute(Connection conn, String sql) throws SQLException {
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.execute();
        }
    }
    
    /**
     * Get database connection
     */
    public Connection getConnection() throws SQLException {
        if (dataSource == null) {
            throw new SQLException("Database not initialized");
        }
        return dataSource.getConnection();
    }
    
    /**
     * Execute query asynchronously
     */
    public CompletableFuture<Void> executeAsync(String sql, Object... params) {
        return CompletableFuture.runAsync(() -> {
            try (Connection conn = getConnection();
                 PreparedStatement stmt = conn.prepareStatement(sql)) {
                
                for (int i = 0; i < params.length; i++) {
                    stmt.setObject(i + 1, params[i]);
                }
                
                stmt.execute();
                
            } catch (SQLException e) {
                plugin.getLogger().log(Level.SEVERE, "Database error: " + sql, e);
            }
        });
    }
    
    /**
     * Check if database is MySQL
     */
    public boolean isMySQL() {
        return mysql;
    }
    
    /**
     * Close database connection
     */
    public void close() {
        if (dataSource != null && !dataSource.isClosed()) {
            dataSource.close();
            plugin.getLogger().info("Database connections closed");
        }
    }
}