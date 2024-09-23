package net.marketplace.commands;

import net.marketplace.MarketPlace;
import net.marketplace.data.BlackMarketItemDAO;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.logging.Logger;

public class BlackMarketSellCommand implements CommandExecutor {

    private final MarketPlace plugin;
    private final Logger logger = Logger.getLogger(BlackMarketSellCommand.class.getName());

    public BlackMarketSellCommand(MarketPlace plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(ChatColor.translateAlternateColorCodes('&', plugin.getConfigManager().getConfig().getString("messages.not_a_player")));
            logger.warning(plugin.getConfigManager().getConfig().getString("logs.non_player_use"));
            return false;
        }

        Player player = (Player) sender;

        if (args.length != 1) {
            player.sendMessage(ChatColor.translateAlternateColorCodes('&', plugin.getConfigManager().getConfig().getString("messages.sell_usage")));
            return false;
        }

        ItemStack itemInHand = player.getInventory().getItemInMainHand();
        if (itemInHand == null || itemInHand.getType().isAir()) {
            player.sendMessage(ChatColor.translateAlternateColorCodes('&', plugin.getConfigManager().getConfig().getString("messages.no_item_in_hand")));
            return false;
        }

        double price;
        try {
            price = Double.parseDouble(args[0]);
            if (price <= 0) {
                player.sendMessage(ChatColor.translateAlternateColorCodes('&', plugin.getConfigManager().getConfig().getString("messages.invalid_price")));
                return false;
            }
        } catch (NumberFormatException e) {
            player.sendMessage(ChatColor.translateAlternateColorCodes('&', plugin.getConfigManager().getConfig().getString("messages.non_numeric_price")));
            return false;
        }

        try {
            // Clone the item to avoid modifying the original one
            ItemStack itemToSell = itemInHand.clone();
            ItemMeta meta = itemToSell.getItemMeta();

            // Reduce price by 50% for Black Market
            double discountedPrice = price * 0.5;

            if (meta != null) {
                // Format the current date
                String currentDate = new SimpleDateFormat("yyyy-MM-dd").format(new Date());

                // Set the discounted price, seller, and date as lore
                meta.setLore(Arrays.asList(
                        ChatColor.GOLD + "----------------------------",
                        ChatColor.GOLD + "" + "Black Market Price: " + ChatColor.YELLOW + String.format("%.2f", discountedPrice),
                        ChatColor.GOLD + "" + "Seller: " + ChatColor.YELLOW + player.getName(),
                        ChatColor.GOLD + "" + "Date: " + ChatColor.YELLOW + currentDate,
                        ChatColor.GOLD + "----------------------------"
                ));
                itemToSell.setItemMeta(meta);
            }

            BlackMarketItemDAO itemDAO = new BlackMarketItemDAO(plugin.getSQLiteConnector(), plugin);
            itemDAO.listItem(player, itemToSell, discountedPrice);

            // Remove item from player's hand after listing
            player.getInventory().setItemInMainHand(null);
            player.sendMessage(ChatColor.translateAlternateColorCodes('&', plugin.getConfigManager().getConfig().getString("messages.item_listed")));

            return true;
        } catch (Exception e) {
            player.sendMessage(ChatColor.translateAlternateColorCodes('&', plugin.getConfigManager().getConfig().getString("messages.error_listing_item")));
            logger.severe(plugin.getConfigManager().getConfig().getString("logs.error_listing_item").replace("{player}", player.getName()));
            return false;
        }
    }
}
