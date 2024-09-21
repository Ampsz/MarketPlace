package net.marketplace.utils;

import net.marketplace.MarketPlace;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;

public class ConfigManager {

    private final MarketPlace plugin;
    private FileConfiguration config;

    public ConfigManager(MarketPlace plugin) {
        this.plugin = plugin;
        createConfig();
    }

    private void createConfig() {
        File configFile = new File(plugin.getDataFolder(), "config.yml");

        if (!configFile.exists()) {
            plugin.saveResource("config.yml", false);
        }

        config = YamlConfiguration.loadConfiguration(configFile);
    }

    public FileConfiguration getConfig() {
        return config;
    }

    public void reloadConfig() {
        File configFile = new File(plugin.getDataFolder(), "config.yml");
        config = YamlConfiguration.loadConfiguration(configFile);
        plugin.getLogger().info("Configuration file reloaded.");
    }

    public void saveConfig() {
        try {
            config.save(new File(plugin.getDataFolder(), "config.yml"));
        } catch (IOException e) {
            plugin.getLogger().severe("Could not save config file!");
        }
    }
}
