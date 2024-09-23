package net.marketplace.data;

import net.marketplace.MarketPlace;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.io.BukkitObjectInputStream;
import org.bukkit.util.io.BukkitObjectOutputStream;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.sql.*;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

public class BlackMarketItemDAO {

    private final SQLiteConnector sqliteConnector;  // Uses SQLiteConnector (which now supports both MySQL and SQLite)
    private final Logger logger = Logger.getLogger(BlackMarketItemDAO.class.getName());
    private final MarketPlace plugin;

    public BlackMarketItemDAO(SQLiteConnector sqliteConnector, MarketPlace plugin) {
        this.sqliteConnector = sqliteConnector;
        this.plugin = plugin;
    }

    // List an item in the black market
    public void listItem(Player player, ItemStack item, double price) {
        String sql = "INSERT INTO black_market_items (player_uuid, item_data, price) VALUES (?, ?, ?)";

        try (Connection conn = sqliteConnector.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, player.getUniqueId().toString());
            stmt.setString(2, itemToBase64(item));
            stmt.setDouble(3, price);
            stmt.executeUpdate();

            // Log configurable message for listing item
            String listMessage = plugin.getConfigManager().getConfig().getString("logs.black_market_item_listed",
                    "Listed item for player {player} in the Black Market with price {price}");
            listMessage = listMessage.replace("{player}", player.getName()).replace("{price}", String.valueOf(price));
            logger.info(listMessage);

        } catch (SQLException | IOException e) {
            String errorMessage = plugin.getConfigManager().getConfig().getString("logs.error_listing_black_market_item",
                    "Error listing item for player {player} in the Black Market");
            errorMessage = errorMessage.replace("{player}", player.getName());
            logger.log(Level.SEVERE, errorMessage, e);
        }
    }

    // Retrieve all items from the black market
    public List<ItemStack> getAllItems() {
        List<ItemStack> items = new ArrayList<>();
        String sql = "SELECT item_data FROM black_market_items";

        try (Connection conn = sqliteConnector.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                ItemStack item = itemFromBase64(rs.getString("item_data"));
                items.add(item);
            }

            // Log configurable message for retrieving items
            String retrievedMessage = plugin.getConfigManager().getConfig().getString("logs.retrieved_black_market_items",
                    "Retrieved {count} items from the Black Market.");
            retrievedMessage = retrievedMessage.replace("{count}", String.valueOf(items.size()));
            logger.info(retrievedMessage);

        } catch (SQLException | IOException | ClassNotFoundException e) {
            String errorMessage = plugin.getConfigManager().getConfig().getString("logs.error_retrieving_black_market_items",
                    "Error retrieving items from the Black Market.");
            logger.log(Level.SEVERE, errorMessage, e);
        }

        return items;
    }

    // Remove an item from the black market
    public boolean removeItem(ItemStack item) {
        String sql = "DELETE FROM black_market_items WHERE item_data = ?";

        try (Connection conn = sqliteConnector.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, itemToBase64(item));
            int rowsAffected = stmt.executeUpdate();

            if (rowsAffected > 0) {
                String removeMessage = plugin.getConfigManager().getConfig().getString("logs.removed_black_market_item",
                        "Removed item from the Black Market.");
                logger.info(removeMessage);
                return true;
            } else {
                String warningMessage = plugin.getConfigManager().getConfig().getString("logs.remove_black_market_item_warning",
                        "Attempted to remove item from the Black Market, but no rows were affected.");
                logger.warning(warningMessage);
            }

        } catch (SQLException | IOException e) {
            String errorMessage = plugin.getConfigManager().getConfig().getString("logs.error_removing_black_market_item",
                    "Error removing item from the Black Market.");
            logger.log(Level.SEVERE, errorMessage, e);
        }
        return false;
    }

    // Get the seller's UUID for an item
    public UUID getSellerUUID(ItemStack item) {
        String sql = "SELECT player_uuid FROM black_market_items WHERE item_data = ?";

        try (Connection conn = sqliteConnector.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, itemToBase64(item));
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                UUID sellerUUID = UUID.fromString(rs.getString("player_uuid"));
                String retrievedMessage = plugin.getConfigManager().getConfig().getString("logs.retrieved_black_market_seller_uuid",
                        "Retrieved seller UUID: {uuid} for the Black Market.");
                retrievedMessage = retrievedMessage.replace("{uuid}", sellerUUID.toString());
                logger.info(retrievedMessage);
                return sellerUUID;
            } else {
                String warningMessage = plugin.getConfigManager().getConfig().getString("logs.black_market_seller_uuid_not_found",
                        "Seller UUID not found for item in the Black Market.");
                logger.warning(warningMessage);
            }

        } catch (SQLException | IOException e) {
            String errorMessage = plugin.getConfigManager().getConfig().getString("logs.error_getting_black_market_seller_uuid",
                    "Error getting seller UUID for item in the Black Market.");
            logger.log(Level.SEVERE, errorMessage, e);
        }

        return null;
    }

    // Helper functions to convert between ItemStack and Base64 (same as in ItemDAO)
    private String itemToBase64(ItemStack item) throws IOException {
        try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
             BukkitObjectOutputStream dataOutput = new BukkitObjectOutputStream(outputStream)) {

            dataOutput.writeObject(item.serialize());
            return Base64.getEncoder().encodeToString(outputStream.toByteArray());
        }
    }

    private ItemStack itemFromBase64(String data) throws IOException, ClassNotFoundException {
        try (ByteArrayInputStream inputStream = new ByteArrayInputStream(Base64.getDecoder().decode(data));
             BukkitObjectInputStream dataInput = new BukkitObjectInputStream(inputStream)) {

            return ItemStack.deserialize((Map<String, Object>) dataInput.readObject());
        }
    }

    // Clear all items from the black market
    public void clearAllItems() throws SQLException {
        try (Connection connection = sqliteConnector.getConnection();
             Statement statement = connection.createStatement()) {
            String sql = "DELETE FROM black_market_items";
            statement.executeUpdate(sql);

            String clearMessage = plugin.getConfigManager().getConfig().getString("logs.cleared_black_market_items",
                    "All items have been removed from the Black Market.");
            logger.info(clearMessage);
        }
    }
}

