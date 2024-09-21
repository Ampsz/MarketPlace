package net.marketplace.data;

import net.marketplace.MarketPlace;

import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.logging.Level;
import java.util.logging.Logger;

public class SQLiteConnector {

    private Connection connection;
    private final MarketPlace plugin;  // Reference to the main plugin class
    private final Logger logger = Logger.getLogger(SQLiteConnector.class.getName());

    public SQLiteConnector(MarketPlace plugin) {
        this.plugin = plugin;
    }

    // Method to establish the connection based on the config
    public void connect() {
        String dbType = plugin.getConfig().getString("database.type", "sqlite");

        try {
            if (dbType.equalsIgnoreCase("mysql")) {
                // MySQL connection details from config
                String host = plugin.getConfig().getString("database.mysql.host", "localhost");
                int port = plugin.getConfig().getInt("database.mysql.port", 3306);
                String database = plugin.getConfig().getString("database.mysql.database", "marketplace");
                String username = plugin.getConfig().getString("database.mysql.username", "root");
                String password = plugin.getConfig().getString("database.mysql.password", "");

                // MySQL connection string
                String url = "jdbc:mysql://" + host + ":" + port + "/" + database + "?useSSL=false&autoReconnect=true";
                connection = DriverManager.getConnection(url, username, password);
                logger.info("Connected to MySQL database.");

            } else {
                // SQLite connection (using plugin's data folder)
                File dataFolder = plugin.getDataFolder();
                if (!dataFolder.exists()) {
                    dataFolder.mkdirs();  // Create the plugin's data folder if it doesn't exist
                }

                String sqliteFilePath = new File(dataFolder, plugin.getConfig().getString("database.sqlite.file", "marketplace.db")).getPath();
                connection = DriverManager.getConnection("jdbc:sqlite:" + sqliteFilePath);
                logger.info("Connected to SQLite database at: " + sqliteFilePath);
            }

            // Call createTables after successful connection
            createTables();

        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Failed to connect to the database!", e);
        }
    }

    // Create tables if they do not exist
    private void createTables() {
        try (Statement stmt = connection.createStatement()) {
            String dbType = plugin.getConfig().getString("database.type", "sqlite");
            String createTableSQL;

            if (dbType.equalsIgnoreCase("mysql")) {
                // MySQL table creation
                createTableSQL = "CREATE TABLE IF NOT EXISTS items (" +
                        "id INT AUTO_INCREMENT PRIMARY KEY, " +
                        "player_uuid VARCHAR(36), " +
                        "item_data TEXT, " +
                        "price DOUBLE)";
            } else {
                // SQLite table creation
                createTableSQL = "CREATE TABLE IF NOT EXISTS items (" +
                        "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                        "player_uuid TEXT, " +
                        "item_data TEXT, " +
                        "price REAL)";
            }

            stmt.executeUpdate(createTableSQL);
            logger.info("Tables created successfully (if they didn't already exist).");

        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Error creating tables.", e);
        }
    }

    // Get database connection
    public Connection getConnection() {
        try {
            if (connection == null || connection.isClosed()) {
                logger.warning("Connection is closed or null. Attempting to reconnect...");
                connect();
            }
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Failed to reconnect to the database.", e);
        }
        return connection;
    }

    // Close database connection
    public void close() {
        try {
            if (connection != null && !connection.isClosed()) {
                connection.close();
                logger.info("Database connection closed.");
            }
        } catch (SQLException e) {
            logger.log(Level.WARNING, "Error closing the database connection!", e);
        }
    }
}
