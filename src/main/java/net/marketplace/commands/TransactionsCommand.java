package net.marketplace.commands;

import net.marketplace.MarketPlace;
import net.marketplace.gui.TransactionGUI;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class TransactionsCommand implements CommandExecutor {

    private final MarketPlace plugin;

    public TransactionsCommand(MarketPlace plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("This command can only be used by players.");
            return false;
        }

        Player player = (Player) sender;

        // Open Transaction GUI
        TransactionGUI transactionGUI = new TransactionGUI(plugin);
        transactionGUI.openTransactionGUI(player);

        return true;
    }
}
