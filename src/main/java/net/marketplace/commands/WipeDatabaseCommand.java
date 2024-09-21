package net.marketplace.commands;

import net.marketplace.MarketPlace;
import net.marketplace.data.ItemDAO;
import net.marketplace.data.TransactionDAO;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.logging.Level;

public class WipeDatabaseCommand implements CommandExecutor {

    private final MarketPlace plugin;

    public WipeDatabaseCommand(MarketPlace plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("Only players can use this command.");
            return false;
        }

        Player player = (Player) sender;

        if (!player.hasPermission("marketplace.admin")) {
            player.sendMessage(ChatColor.RED + "You cannot do that!");
            return false;
        }

        try {
            // Clear all marketplace items
            ItemDAO itemDAO = new ItemDAO(plugin.getSQLiteConnector(), plugin);
            itemDAO.clearAllItems();

            // Clear all transaction history
            TransactionDAO transactionDAO = new TransactionDAO(plugin.getSQLiteConnector());
            transactionDAO.clearAllTransactions();

            // Notify player and log success
            player.sendMessage(ChatColor.YELLOW + "[MarketPlace]" + ChatColor.GREEN + " Database and transaction history have been wiped successfully!");
            plugin.getLogger().info("Database and transaction history wiped successfully by " + player.getName());

        } catch (Exception e) {
            // Handle errors and log them
            plugin.getLogger().log(Level.SEVERE, "Error wiping the database and transaction history:", e);
            player.sendMessage("An error occurred while wiping the database and transaction history. Check the server logs for details.");
        }

        return true;
    }
}
