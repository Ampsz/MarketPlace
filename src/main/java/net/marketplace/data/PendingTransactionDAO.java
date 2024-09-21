package net.marketplace.data;

import net.marketplace.models.PendingTransaction;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;

public class PendingTransactionDAO {

    private final SQLiteConnector sqliteConnector;
    private final Logger logger = Logger.getLogger(PendingTransactionDAO.class.getName());

    public PendingTransactionDAO(SQLiteConnector sqliteConnector) {
        this.sqliteConnector = sqliteConnector;
        createTableIfNotExists(); // Ensure the table is created when the DAO is initialized
    }

    // Ensure the pending_transactions table exists
    private void createTableIfNotExists() {
        String sql = "CREATE TABLE IF NOT EXISTS pending_transactions (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "player_uuid TEXT, " +
                "amount REAL)";
        try (Connection conn = sqliteConnector.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.executeUpdate();
            logger.info("Pending transactions table verified or created successfully.");
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Error creating or verifying pending transactions table", e);
        }
    }

    // Add a new pending transaction
    public void addPendingTransaction(UUID playerUUID, double amount) {
        String sql = "INSERT INTO pending_transactions (player_uuid, amount) VALUES (?, ?)";

        try (Connection conn = sqliteConnector.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, playerUUID.toString());
            stmt.setDouble(2, amount);
            stmt.executeUpdate();

            logger.info("Added pending transaction for player: " + playerUUID + ", amount: " + amount);

        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Error adding pending transaction for player: " + playerUUID, e);
        }
    }

    // Get all pending transactions for a player
    public List<PendingTransaction> getPendingTransactions(UUID playerUUID) {
        List<PendingTransaction> transactions = new ArrayList<>();
        String sql = "SELECT amount FROM pending_transactions WHERE player_uuid = ?";

        try (Connection conn = sqliteConnector.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, playerUUID.toString());
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                double amount = rs.getDouble("amount");
                transactions.add(new PendingTransaction(playerUUID, amount));
            }

            logger.info("Retrieved " + transactions.size() + " pending transactions for player: " + playerUUID);

        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Error retrieving pending transactions for player: " + playerUUID, e);
        }

        return transactions;
    }

    // Remove a pending transaction after it's processed
    public void removePendingTransaction(UUID playerUUID, double amount) {
        String sql = "DELETE FROM pending_transactions WHERE player_uuid = ? AND amount = ?";

        try (Connection conn = sqliteConnector.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, playerUUID.toString());
            stmt.setDouble(2, amount);
            stmt.executeUpdate();

            logger.info("Removed pending transaction for player: " + playerUUID + ", amount: " + amount);

        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Error removing pending transaction for player: " + playerUUID, e);
        }
    }

    // Clear all pending transactions
    public void clearAllPendingTransactions() throws SQLException {
        String sql = "DELETE FROM pending_transactions";

        try (Connection connection = sqliteConnector.getConnection();
             Statement statement = connection.createStatement()) {

            statement.executeUpdate(sql);
            logger.info("All pending transactions have been removed from the database.");

        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Error clearing all pending transactions from the database.", e);
        }
    }
}
