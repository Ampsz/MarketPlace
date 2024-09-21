package net.marketplace.commands;

import net.marketplace.MarketPlace;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.ChatColor;

public class ReloadCommand implements CommandExecutor {

    private final MarketPlace plugin;

    public ReloadCommand(MarketPlace plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (sender instanceof Player) {
            Player player = (Player) sender;
            if (!player.hasPermission("marketplace.admin")) {
                // Load "no permission" message from config
                String noPermissionMessage = plugin.getConfigManager().getConfig().getString("messages.no_permission", "&cYou cannot do that!");
                player.sendMessage(ChatColor.translateAlternateColorCodes('&', noPermissionMessage));
                return false;
            }
        }

        // Reload the configuration
        plugin.getConfigManager().reloadConfig();

        // Load "config reloaded" message from config
        String reloadMessage = plugin.getConfigManager().getConfig().getString("messages.config_reloaded", "&aMarketplace configuration reloaded!");
        sender.sendMessage(ChatColor.translateAlternateColorCodes('&', reloadMessage));

        return true;
    }
}
