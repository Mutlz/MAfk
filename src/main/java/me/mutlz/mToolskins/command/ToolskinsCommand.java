package me.mutlz.mToolskins.command;

import me.mutlz.mToolskins.MToolskins;
import me.mutlz.mToolskins.menu.ToolskinGui;
import me.mutlz.mToolskins.model.SkinEntry;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.logging.Level;

public class ToolskinsCommand implements CommandExecutor {

    private static final String SKIN_FOLDER = "skins";

    private final MToolskins plugin;

    public ToolskinsCommand(MToolskins plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage(Component.text("Bu komutu sadece oyuncular kullanabilir.", NamedTextColor.RED));
            return true;
        }

        ItemStack mainHand = player.getInventory().getItemInMainHand();
        if (mainHand == null || mainHand.getType().isAir()) {
            player.sendMessage(plugin.getMessageManager().getMessage("errors.no-item"));
            return true;
        }

        File skinsDirectory = new File(plugin.getDataFolder(), SKIN_FOLDER);
        if (!skinsDirectory.exists()) {
            skinsDirectory.mkdirs();
        }

        String typeName = mainHand.getType().name().toLowerCase(Locale.ROOT);
        File skinFile = new File(skinsDirectory, typeName + ".yml");

        if (!skinFile.exists()) {
            player.sendMessage(plugin.getMessageManager().getMessage("errors.skin-not-found"));
            return true;
        }

        YamlConfiguration configuration = YamlConfiguration.loadConfiguration(skinFile);
        ConfigurationSection skinsSection = configuration.getConfigurationSection("skins");
        if (skinsSection == null || skinsSection.getKeys(false).isEmpty()) {
            player.sendMessage(plugin.getMessageManager().getMessage("errors.skin-not-found"));
            return true;
        }

        List<SkinEntry> entries = new ArrayList<>();
        for (String key : skinsSection.getKeys(false)) {
            String path = "skins." + key + ".";
            String displayName = configuration.getString(path + "name", key);
            String materialName = configuration.getString(path + "material", "PAPER");
            int customModelData = configuration.getInt(path + "custom-model-data", -1);
            String permission = configuration.getString(path + "permission");

            if (customModelData < 0 || permission == null || permission.isEmpty()) {
                plugin.getLogger().log(Level.WARNING,
                        "Skin {0} in {1} is missing custom-model-data or permission. Skipping.", new Object[]{key, skinFile.getName()});
                continue;
            }

            Material material = Material.matchMaterial(materialName);
            if (material == null) {
                material = Material.PAPER;
            }

            entries.add(new SkinEntry(key, displayName, material, customModelData, permission));
        }

        if (entries.isEmpty()) {
            player.sendMessage(plugin.getMessageManager().getMessage("errors.skin-not-found"));
            return true;
        }

        Component title = Component.text("ToolSkins - ", NamedTextColor.DARK_GREEN)
                .append(Component.text(mainHand.getType().name(), NamedTextColor.GOLD));
        ToolskinGui gui = new ToolskinGui(plugin, player, entries, title);
        gui.open();
        return true;
    }
}

