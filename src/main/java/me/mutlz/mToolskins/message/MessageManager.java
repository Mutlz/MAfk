package me.mutlz.mToolskins.message;

import org.bukkit.ChatColor;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;

public class MessageManager {

    private final JavaPlugin plugin;
    private FileConfiguration messageConfig;

    public MessageManager(JavaPlugin plugin) {
        this.plugin = plugin;
        saveDefaultMessages();
        reload();
    }

    public void reload() {
        File file = getMessageFile();
        messageConfig = YamlConfiguration.loadConfiguration(file);
    }

    public String getMessage(String path) {
        String value = messageConfig.getString(path);
        if (value == null) {
            value = "&cMissing message: " + path;
            messageConfig.set(path, value);
            save();
        }
        return ChatColor.translateAlternateColorCodes('&', value);
    }

    private void saveDefaultMessages() {
        if (!plugin.getDataFolder().exists()) {
            //noinspection ResultOfMethodCallIgnored
            plugin.getDataFolder().mkdirs();
        }
        File messageFile = getMessageFile();
        if (!messageFile.exists()) {
            plugin.saveResource("message.yml", false);
        }
    }

    private File getMessageFile() {
        return new File(plugin.getDataFolder(), "message.yml");
    }

    private void save() {
        try {
            messageConfig.save(getMessageFile());
        } catch (IOException ex) {
            plugin.getLogger().severe("Unable to save message.yml: " + ex.getMessage());
        }
    }
}

