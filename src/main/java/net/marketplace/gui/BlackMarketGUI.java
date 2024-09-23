package net.marketplace.gui;

import net.marketplace.MarketPlace;
import net.marketplace.data.BlackMarketItemDAO;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryView;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class BlackMarketGUI implements Listener {

    private static final Logger LOGGER = Logger.getLogger(BlackMarketGUI.class.getName());
    private final MarketPlace plugin;

    public BlackMarketGUI(MarketPlace plugin) {
        this.plugin = plugin;
    }

    public void openBlackMarket(Player player, int page) {
        BlackMarketItemDAO itemDAO = new BlackMarketItemDAO(plugin.getSQLiteConnector(), plugin); // Use BlackMarketItemDAO
        List<ItemStack> items = itemDAO.getAllItems();

        // Load configurable black market settings
        String menuTitle = plugin.getConfigManager().getConfig().getString("gui.blackmarket.title", "&cBlack Market");
        int menuSize = plugin.getConfigManager().getConfig().getInt("gui.blackmarket.size", 54);
        int itemsPerPage = plugin.getConfigManager().getConfig().getInt("gui.blackmarket.items_per_page", 45); // Excluding navigation bar (9 slots)

        // Append the current page number to the menu title
        menuTitle += ChatColor.GRAY + " (Page " + page + ")";

        Inventory inventory = Bukkit.createInventory(null, menuSize, ChatColor.translateAlternateColorCodes('&', menuTitle));

        // Calculate pagination
        int totalPages = (int) Math.ceil((double) items.size() / itemsPerPage);
        int startIndex = (page - 1) * itemsPerPage;

        // Place items for the current page
        for (int i = 0; i < itemsPerPage && (startIndex + i) < items.size(); i++) {
            ItemStack item = items.get(startIndex + i);
            inventory.setItem(i, item);
        }

        // Add "Previous Page" navigation button if not on the first page
        if (page > 1) {
            ItemStack previousButton = createNavigationButton("Previous Page", ChatColor.RED, Material.REDSTONE_BLOCK);
            inventory.setItem(menuSize - 9, previousButton); // Place "Previous Page" button at slot 45 (bottom-left)
        }

        // Always add "Next Page" navigation button
        ItemStack nextButton = createNavigationButton("Next Page", ChatColor.GREEN, Material.ARROW);
        inventory.setItem(menuSize - 1, nextButton); // Place "Next Page" button at slot 53 (bottom-right)

        // Load and apply the background item if configured
        if (plugin.getConfigManager().getConfig().getBoolean("gui.blackmarket.background.enabled", false)) {
            Material backgroundMaterial = Material.valueOf(plugin.getConfigManager().getConfig().getString("gui.blackmarket.background.item", "BLACK_STAINED_GLASS_PANE"));
            String backgroundName = plugin.getConfigManager().getConfig().getString("gui.blackmarket.background.name", "&7");

            ItemStack background = new ItemStack(backgroundMaterial);
            ItemMeta backgroundMeta = background.getItemMeta();
            backgroundMeta.setDisplayName(ChatColor.translateAlternateColorCodes('&', backgroundName));
            background.setItemMeta(backgroundMeta);

            // Fill empty slots with the background item
            for (int i = 0; i < menuSize - 9; i++) {
                if (inventory.getItem(i) == null) {
                    inventory.setItem(i, background);
                }
            }
        }

        // Open the inventory for the player
        player.openInventory(inventory);
    }

    @EventHandler
    public void handleInventoryClick(InventoryClickEvent event) {
        Player player = (Player) event.getWhoClicked();
        String menuTitle = plugin.getConfigManager().getConfig().getString("gui.blackmarket.title", "&cBlack Market");

        if (event.getView().getTitle().startsWith(ChatColor.translateAlternateColorCodes('&', menuTitle))) {
            event.setCancelled(true); // Prevent taking items directly

            ItemStack clickedItem = event.getCurrentItem();
            if (clickedItem == null || clickedItem.getType() == Material.AIR) {
                return; // No item clicked
            }

            // Load the background item type from the config (e.g., BLACK_STAINED_GLASS_PANE)
            Material backgroundMaterial = Material.valueOf(plugin.getConfigManager().getConfig().getString("gui.blackmarket.background.item", "BLACK_STAINED_GLASS_PANE"));

            // Check if the clicked item is the background item
            if (clickedItem.getType() == backgroundMaterial) {
                return; // Ignore clicks on background items
            }

            // Check if the clicked item is in the player's inventory (ignores items already in player's inventory)
            if (event.getClickedInventory() == player.getInventory()) {
                player.sendMessage(ChatColor.RED + "You cannot sell items from your own inventory.");
                return;
            }

            // Check for "Previous Page" navigation button
            ItemMeta meta = clickedItem.getItemMeta();
            if (meta != null && meta.hasDisplayName()) {
                String displayName = ChatColor.stripColor(meta.getDisplayName());

                if (displayName.equalsIgnoreCase("Previous Page")) {
                    int currentPage = getCurrentPage(event.getView());
                    openBlackMarket(player, currentPage - 1); // Go to the previous page
                    return;
                }

                // Check for "Next Page" navigation button
                if (displayName.equalsIgnoreCase("Next Page")) {
                    int currentPage = getCurrentPage(event.getView());
                    openBlackMarket(player, currentPage + 1); // Go to the next page
                    return;
                }
            }

            // Open confirmation GUI for the clicked item and pass the slot
            BlackMarketConfirmationGUI confirmGUI = new BlackMarketConfirmationGUI(plugin);
            int clickedSlot = event.getSlot(); // Get the slot where the item was clicked
            confirmGUI.openConfirmation(player, clickedItem, clickedSlot); // Pass the slot to the confirmation GUI
        }
    }

    // Method to create a navigation button with an enchanted ItemStack but hidden enchantments
    private ItemStack createNavigationButton(String name, ChatColor color, Material material) {
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();

        if (meta != null) {
            meta.setDisplayName(color + name);
            meta.addEnchant(Enchantment.DURABILITY, 1, true); // Add an enchantment to give an "enchanted" look
            meta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
            item.setItemMeta(meta);
        } else {
            LOGGER.log(Level.SEVERE, "ItemMeta is null");
        }

        return item;
    }

    // Method to extract the current page from the inventory title
    private int getCurrentPage(InventoryView inventoryView) {
        String title = ChatColor.stripColor(inventoryView.getTitle()); // Use getTitle from InventoryView
        String[] parts = title.split(" ");
        try {
            return Integer.parseInt(parts[parts.length - 1].replace("Page", "").replace("(", "").replace(")", "").trim());
        } catch (NumberFormatException e) {
            return 1; // Default to page 1 if parsing fails
        }
    }
}
