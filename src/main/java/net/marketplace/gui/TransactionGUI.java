package net.marketplace.gui;

import net.marketplace.MarketPlace;
import net.marketplace.data.TransactionDAO;
import net.marketplace.models.Transaction;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;

public class TransactionGUI implements Listener {

    private final MarketPlace plugin;

    public TransactionGUI(MarketPlace plugin) {
        this.plugin = plugin;
    }

    public void openTransactionGUI(Player player) {
        // Load configurable transaction GUI title and size from config.yml
        String menuTitle = plugin.getConfigManager().getConfig().getString("gui.transaction.title", "&eTransaction History");
        int menuSize = plugin.getConfigManager().getConfig().getInt("gui.transaction.size", 9);

        Inventory inventory = Bukkit.createInventory(null, menuSize, ChatColor.translateAlternateColorCodes('&', menuTitle));

        // Fetch the latest 9 transactions for the player
        TransactionDAO transactionDAO = new TransactionDAO(plugin.getSQLiteConnector());
        List<Transaction> transactions = transactionDAO.getLatestTransactions(player.getUniqueId().toString(), 9); // Fetch up to 9 latest transactions

        // Place transaction items into the inventory based on the number of transactions
        for (int i = 0; i < transactions.size(); i++) {
            Transaction transaction = transactions.get(i);

            // Create item stack representing the transaction
            ItemStack transactionItem = new ItemStack(Material.MAP); // Use MAP as a placeholder for transaction items
            ItemMeta meta = transactionItem.getItemMeta();
            if (meta != null) {
                meta.setDisplayName(ChatColor.GOLD + "Item: " + ChatColor.YELLOW + transaction.getItemName());

                // Create a modifiable list for the lore
                List<String> lore = new ArrayList<>();
                lore.add(ChatColor.GOLD + "Price: " + ChatColor.YELLOW + String.format("%.2f", transaction.getPrice()));
                lore.add(ChatColor.GOLD + "Date: " + ChatColor.YELLOW + transaction.getDate());

                meta.setLore(lore);
                transactionItem.setItemMeta(meta);
            }
            inventory.setItem(i, transactionItem); // Add to the inventory at the correct slot
        }

        // Open the inventory for the player
        player.openInventory(inventory);
    }

    @EventHandler
    public void handleInventoryClick(InventoryClickEvent event) {
        // Prevent interactions with the transaction history
        String menuTitle = plugin.getConfigManager().getConfig().getString("gui.transaction.title", "&eTransaction History");
        if (event.getView().getTitle().equals(ChatColor.translateAlternateColorCodes('&', menuTitle))) {
            event.setCancelled(true); // Prevent item interaction

            // Do not close the inventory, just block the interaction
            if (event.getCurrentItem() != null) {
                // Optionally, you can send a message to the player or do nothing
                // Player player = (Player) event.getWhoClicked();
                // player.sendMessage(ChatColor.RED + "You cannot move items from the transaction history.");
            }
        }
    }
}
