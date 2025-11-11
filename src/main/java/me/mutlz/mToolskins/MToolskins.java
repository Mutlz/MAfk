package me.mutlz.mToolskins;

import me.mutlz.mToolskins.command.ToolskinsCommand;
import me.mutlz.mToolskins.listener.ToolskinListener;
import me.mutlz.mToolskins.message.MessageManager;
import org.bukkit.entity.Player;
import org.bukkit.permissions.PermissionAttachment;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public final class MToolskins extends JavaPlugin {

    private MessageManager messageManager;
    private final Map<UUID, PermissionAttachment> permissionAttachments = new HashMap<>();

    @Override
    public void onEnable() {
        messageManager = new MessageManager(this);

        ToolskinListener toolskinListener = new ToolskinListener(this);
        getServer().getPluginManager().registerEvents(toolskinListener, this);

        if (getCommand("toolskins") != null) {
            getCommand("toolskins").setExecutor(new ToolskinsCommand(this));
        } else {
            getLogger().severe("Command /toolskins is not defined in plugin.yml");
        }
    }

    @Override
    public void onDisable() {
        permissionAttachments.values().forEach(attachment -> {
            try {
                attachment.remove();
            } catch (Exception ignored) {
                // Ignore any issues while cleaning up attachments
            }
        });
        permissionAttachments.clear();
    }

    public MessageManager getMessageManager() {
        return messageManager;
    }

    public PermissionAttachment getOrCreateAttachment(Player player) {
        return permissionAttachments.computeIfAbsent(player.getUniqueId(), uuid -> player.addAttachment(this));
    }

    public void clearAttachment(Player player) {
        PermissionAttachment attachment = permissionAttachments.remove(player.getUniqueId());
        if (attachment != null) {
            try {
                player.removeAttachment(attachment);
            } catch (Exception ignored) {
                // Ignore if attachment is already invalid
            }
        }
    }
}
