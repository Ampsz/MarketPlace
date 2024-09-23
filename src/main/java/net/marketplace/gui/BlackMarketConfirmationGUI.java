package net.marketplace.gui;

import net.marketplace.MarketPlace;
import net.marketplace.data.BlackMarketItemDAO;
import net.marketplace.data.PendingTransactionDAO;
import net.marketplace.data.TransactionDAO;
import net.marketplace.utils.DiscordWebhook;
import net.milkbowl.vault.economy.Economy;
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

import java.util.List;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class BlackMarketConfirmationGUI implements Listener {

    private final MarketPlace plugin;
    private final Economy economy;
    private final DiscordWebhook discordWebhook;  // For Discord webhook logging
    private int clickedSlot;  // Store the clicked slot to remove the item after purchase

    public BlackMarketConfirmationGUI(MarketPlace plugin) {
        this.plugin = plugin;
        this.economy = plugin.getEconomy();
        // Initialize Discord webhook for logging
        String webhookUrl = plugin.getConfig().getString("discord.webhook_url");
        this.discordWebhook = new DiscordWebhook(webhookUrl);
    }

    public void openConfirmation(Player player, ItemStack item, int slot) {
        // Save the clicked slot to remove the item later
        this.clickedSlot = slot;

        // Load configurable menu title and size from config.yml
        String menuTitle = plugin.getConfigManager().getConfig().getString("gui.blackmarket_confirm_purchase.title", "Confirm Black Market Purchase");
        int menuSize = plugin.getConfigManager().getConfig().getInt("gui.blackmarket_confirm_purchase.size", 9);

        Inventory inventory = Bukkit.createInventory(null, menuSize, ChatColor.translateAlternateColorCodes('&', menuTitle));

        // Load configurable items for confirm and cancel buttons
        Material confirmMaterial = Material.valueOf(plugin.getConfigManager().getConfig().getString("gui.blackmarket_confirm_purchase.confirm_item", "GREEN_CONCRETE"));
        String confirmName = plugin.getConfigManager().getConfig().getString("gui.blackmarket_confirm_purchase.confirm_name", "&aConfirm Purchase");

        Material cancelMaterial = Material.valueOf(plugin.getConfigManager().getConfig().getString("gui.blackmarket_confirm_purchase.cancel_item", "RED_CONCRETE"));
        String cancelName = plugin.getConfigManager().getConfig().getString("gui.blackmarket_confirm_purchase.cancel_name", "&cCancel");

        // Create confirm item
        ItemStack confirm = new ItemStack(confirmMaterial);
        ItemMeta confirmMeta = confirm.getItemMeta();
        confirmMeta.setDisplayName(ChatColor.translateAlternateColorCodes('&', confirmName));
        confirm.setItemMeta(confirmMeta);

        // Create cancel item
        ItemStack cancel = new ItemStack(cancelMaterial);
        ItemMeta cancelMeta = cancel.getItemMeta();
        cancelMeta.setDisplayName(ChatColor.translateAlternateColorCodes('&', cancelName));
        cancel.setItemMeta(cancelMeta);

        // Add confirm and cancel items to the menu
        inventory.setItem(3, confirm);
        inventory.setItem(5, cancel);

        // Set the item being purchased
        inventory.setItem(4, item);

        // Open the inventory for the player
        player.openInventory(inventory);
    }

    @EventHandler
    public void handleInventoryClick(InventoryClickEvent event) {
        String menuTitle = plugin.getConfigManager().getConfig().getString("gui.blackmarket_confirm_purchase.title", "Confirm Black Market Purchase");

        if (event.getView().getTitle().equals(ChatColor.translateAlternateColorCodes('&', menuTitle))) {
            event.setCancelled(true); // Prevent item interaction

            Player buyer = (Player) event.getWhoClicked();
            ItemStack clickedItem = event.getCurrentItem();

            if (clickedItem == null || clickedItem.getType() == Material.AIR) {
                plugin.getLogger().warning("Clicked item is null or AIR.");
                return;
            }

            // If the player clicks the "Confirm" button
            if (clickedItem.getType() == Material.valueOf(plugin.getConfigManager().getConfig().getString("gui.blackmarket_confirm_purchase.confirm_item", "GREEN_CONCRETE"))) {
                ItemStack purchasedItem = event.getInventory().getItem(4);
                processPurchase(buyer, purchasedItem, event.getView().getTopInventory()); // Pass the top inventory (blackmarket) for removal
                buyer.closeInventory();
            }
            // If the player clicks the "Cancel" button
            else if (clickedItem.getType() == Material.valueOf(plugin.getConfigManager().getConfig().getString("gui.blackmarket_confirm_purchase.cancel_item", "RED_CONCRETE"))) {
                buyer.closeInventory();  // Close the inventory if canceled
            }
        }
    }

    private void processPurchase(Player buyer, ItemStack purchasedItem, Inventory blackmarketInventory) {
        ItemMeta meta = purchasedItem.getItemMeta();
        List<String> lore = meta != null ? meta.getLore() : null;

        if (lore != null && lore.size() > 1) {
            for (String loreLine : lore) {
                String strippedLine = stripColorCodes(loreLine);
                if (strippedLine.startsWith("Black Market Price:")) {
                    Pattern pattern = Pattern.compile("Black Market Price:\\s*(\\d+(?:\\.\\d{1,2})?)");
                    Matcher matcher = pattern.matcher(strippedLine);
                    if (matcher.find()) {
                        String priceString = matcher.group(1);
                        double price = Double.parseDouble(priceString);

                        // Fetch the seller UUID from the database
                        BlackMarketItemDAO itemDAO = new BlackMarketItemDAO(plugin.getSQLiteConnector(), plugin);
                        UUID sellerUUID = itemDAO.getSellerUUID(purchasedItem);

                        // Check if the buyer is the seller
                        if (buyer.getUniqueId().equals(sellerUUID)) {
                            // Return the item to the seller's inventory
                            buyer.getInventory().addItem(purchasedItem.clone());
                            buyer.sendMessage(ChatColor.YELLOW + "You have removed your item from the Black Market.");

                            // Remove the item from the GUI and database
                            blackmarketInventory.setItem(clickedSlot, null); // Remove the item from the GUI
                            itemDAO.removeItem(purchasedItem);  // Remove the item from the database
                            return;  // Exit the method without any financial transaction
                        }

                        // If buyer is not the seller, proceed with the normal transaction
                        double balance = economy.getBalance(buyer);

                        if (balance >= price) {
                            economy.withdrawPlayer(buyer, price);

                            // Reimburse the seller 2x the Black Market price
                            double reimbursement = price * 2;

                            Player seller = Bukkit.getPlayer(sellerUUID);
                            if (seller != null) {
                                economy.depositPlayer(seller, reimbursement);
                                seller.sendMessage(ChatColor.GOLD + "Your item was sold in the Black Market! You have been reimbursed " + reimbursement);
                            } else {
                                PendingTransactionDAO pendingTransactionDAO = new PendingTransactionDAO(plugin.getSQLiteConnector());
                                pendingTransactionDAO.addPendingTransaction(sellerUUID, reimbursement);
                            }

                            // Remove the item from the Black Market GUI
                            blackmarketInventory.setItem(clickedSlot, null);

                            // Clone and prepare the item for the buyer
                            ItemStack itemForPlayer = purchasedItem.clone();

                            // Ensure lore is cleared before giving the item to the buyer
                            ItemMeta playerMeta = itemForPlayer.getItemMeta();
                            if (playerMeta != null) {
                                playerMeta.setLore(null);  // Remove lore explicitly
                                itemForPlayer.setItemMeta(playerMeta);  // Reapply the modified meta to the item
                            }

                            // Add the item to the buyer's inventory
                            buyer.getInventory().addItem(itemForPlayer);

                            // Remove the item from the database
                            itemDAO.removeItem(purchasedItem);

                            // Log the transaction
                            TransactionDAO transactionDAO = new TransactionDAO(plugin.getSQLiteConnector());
                            transactionDAO.addTransaction(buyer.getUniqueId().toString(), purchasedItem.getType().name(), price);

                            // Send purchase details to the Discord webhook
                            discordWebhook.sendPurchaseLog(buyer.getName(), purchasedItem.getType().name(), price);

                            buyer.sendMessage(ChatColor.GREEN + "Purchase successful on the Black Market!");
                        } else {
                            buyer.sendMessage(ChatColor.RED + "You don't have enough money to complete the purchase.");
                        }
                    } else {
                        buyer.sendMessage(ChatColor.RED + "Invalid price format in item lore.");
                    }
                    break;
                }
            }
        } else {
            buyer.sendMessage(ChatColor.RED + "Invalid item data. Purchase failed.");
        }
    }

    // Helper method to strip color codes from strings
    private String stripColorCodes(String input) {
        return ChatColor.stripColor(input);
    }
}
