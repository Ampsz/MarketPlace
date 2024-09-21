package net.marketplace.listeners;

import net.marketplace.MarketPlace;
import net.marketplace.data.PendingTransactionDAO;
import net.marketplace.models.PendingTransaction;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

import java.util.List;

public class PlayerJoinListener implements Listener {

    private final MarketPlace plugin;
    private final Economy economy;
    private final PendingTransactionDAO pendingTransactionDAO;

    public PlayerJoinListener(MarketPlace plugin) {
        this.plugin = plugin;
        this.economy = plugin.getEconomy();
        this.pendingTransactionDAO = new PendingTransactionDAO(plugin.getSQLiteConnector());
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();

        // Check for pending transactions
        List<PendingTransaction> pendingTransactions = pendingTransactionDAO.getPendingTransactions(player.getUniqueId());
        for (PendingTransaction transaction : pendingTransactions) {
            double amount = transaction.getAmount();

            // Deliver the payment
            economy.depositPlayer(player, amount);
            player.sendMessage(ChatColor.YELLOW + "[MarketPlace]" + ChatColor.GREEN + " You received $" + ChatColor.GOLD + amount + ChatColor.GREEN +  " from a previous sale!");

            // Remove the processed transaction
            pendingTransactionDAO.removePendingTransaction(player.getUniqueId(), amount);
        }
    }
}
