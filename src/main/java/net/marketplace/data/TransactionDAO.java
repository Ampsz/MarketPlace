package net.marketplace.data;

import net.marketplace.models.Transaction;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class TransactionDAO {

    private final SQLiteConnector sqliteConnector;
    private final Logger logger = Logger.getLogger(TransactionDAO.class.getName());

    public TransactionDAO(SQLiteConnector sqliteConnector) {
        this.sqliteConnector = sqliteConnector;
        createTableIfNotExists(); // Ensure the table exists
    }

    // Ensure the transactions table exists
    private void createTableIfNotExists() {
        String sql = "CREATE TABLE IF NOT EXISTS transactions (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "player_uuid TEXT, " +
                "item_name TEXT, " +
                "price REAL, " +
                "transaction_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP)";

        try (Connection conn = sqliteConnector.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.executeUpdate();
            logger.info("Transactions table verified.");
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Error creating transactions table", e);
        }
    }

    // Fetch transaction history for a specific player
    public List<Transaction> getTransactions(String playerUUID) {
        List<Transaction> transactions = new ArrayList<>();
        String sql = "SELECT item_name, price, transaction_date FROM transactions WHERE player_uuid = ?";

        try (Connection conn = sqliteConnector.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, playerUUID);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                String itemName = rs.getString("item_name");
                double price = rs.getDouble("price");
                String date = rs.getString("transaction_date");

                transactions.add(new Transaction(itemName, price, date));
            }

            logger.info("Fetched " + transactions.size() + " transactions for player: " + playerUUID);

        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Error fetching transactions for player: " + playerUUID, e);
        }

        return transactions;
    }

    // Fetch the latest 9 transactions for a player
    public List<Transaction> getLatestTransactions(String playerUUID, int limit) {
        List<Transaction> transactions = new ArrayList<>();
        String sql = "SELECT item_name, price, transaction_date FROM transactions WHERE player_uuid = ? ORDER BY transaction_date DESC LIMIT ?";

        try (Connection conn = sqliteConnector.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, playerUUID);
            stmt.setInt(2, limit);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                String itemName = rs.getString("item_name");
                double price = rs.getDouble("price");
                String date = rs.getString("transaction_date");

                transactions.add(new Transaction(itemName, price, date));
            }

            logger.info("Fetched " + transactions.size() + " latest transactions for player: " + playerUUID);

        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Error fetching latest transactions for player: " + playerUUID, e);
        }

        return transactions;
    }

    // Add a new transaction
    public void addTransaction(String playerUUID, String itemName, double price) {
        String sql = "INSERT INTO transactions (player_uuid, item_name, price, transaction_date) VALUES (?, ?, ?, CURRENT_TIMESTAMP)";

        try (Connection conn = sqliteConnector.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, playerUUID);
            stmt.setString(2, itemName);
            stmt.setDouble(3, price);
            stmt.executeUpdate();

            logger.info("Added transaction for player: " + playerUUID + ", item: " + itemName + ", price: " + price);

        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Error adding transaction for player: " + playerUUID + ", item: " + itemName, e);
        }
    }

    public void clearAllTransactions() {
        String sql = "DELETE FROM transactions";

        try (Connection conn = sqliteConnector.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            int rowsAffected = stmt.executeUpdate();
            logger.info("Cleared " + rowsAffected + " transactions from the database.");
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Error clearing transaction history:", e);
        }
    }
}

