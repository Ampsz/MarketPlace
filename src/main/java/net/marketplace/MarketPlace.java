package net.marketplace;

import net.marketplace.commands.*;
import net.marketplace.data.RedisCache;
import net.marketplace.data.SQLiteConnector;
import net.marketplace.gui.ConfirmPurchaseGUI;
import net.marketplace.gui.MarketplaceGUI;
import net.marketplace.gui.TransactionGUI;
import net.marketplace.listeners.PlayerJoinListener;
import net.marketplace.utils.ConfigManager;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;

public final class MarketPlace extends JavaPlugin {

    private SQLiteConnector sqLiteConnector;
    private RedisCache redisCache;
    private Economy economy;
    private ConfigManager configManager;

    @Override
    public void onEnable() {
        // Initialize SQLiteConnector with the plugin instance
        this.sqLiteConnector = new SQLiteConnector(this);
        this.sqLiteConnector.connect(); //Making sure the db is connected

        this.redisCache = new RedisCache();
        this.configManager = new ConfigManager(this);

        // Check if Vault economy is set up correctly
        if (!setupEconomy()) {
            getLogger().warning("Vault economy is not setup correctly!");
            getServer().getPluginManager().disablePlugin(this);
            return;
        }

        // Register commands (as requested, unchanged)
        getCommand("sell").setExecutor(new SellCommand(this));
        getCommand("marketplace").setExecutor(new MarketplaceCommand(this));
        getCommand("transactions").setExecutor(new TransactionsCommand(this));
        getCommand("wipedatabase").setExecutor(new WipeDatabaseCommand(this));
        getCommand("reloadmarketplace").setExecutor(new ReloadCommand(this));

        // Register events (as requested, unchanged)
        getServer().getPluginManager().registerEvents(new MarketplaceGUI(this), this);
        getServer().getPluginManager().registerEvents(new TransactionGUI(this), this);
        getServer().getPluginManager().registerEvents(new ConfirmPurchaseGUI(this), this);
        getServer().getPluginManager().registerEvents(new PlayerJoinListener(this), this);
    }

    @Override
    public void onDisable() {
        this.sqLiteConnector.close();
        this.redisCache.close();
    }

    public SQLiteConnector getSQLiteConnector() {
        return sqLiteConnector;
    }

    public RedisCache getRedisCache() {
        return redisCache;
    }

    public Economy getEconomy() {
        return economy;
    }

    public ConfigManager getConfigManager() {
        return configManager;
    }

    private boolean setupEconomy() {
        RegisteredServiceProvider<Economy> rsp = getServer().getServicesManager().getRegistration(Economy.class);
        if (rsp != null) {
            economy = rsp.getProvider();
        }
        return economy != null;
    }
}
