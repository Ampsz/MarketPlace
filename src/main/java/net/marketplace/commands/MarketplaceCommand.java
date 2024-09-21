package net.marketplace.commands;

import net.marketplace.MarketPlace;
import net.marketplace.gui.MarketplaceGUI;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class MarketplaceCommand implements CommandExecutor {

    private final MarketPlace plugin;

    public MarketplaceCommand(MarketPlace plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("This command can only be used by players.");
            return false;
        }

        Player player = (Player) sender;

        // Open the Black Market GUI
        MarketplaceGUI marketplaceGUI = new MarketplaceGUI(plugin);
        marketplaceGUI.openMarketplace(player,1);
        return true;
    }
}
