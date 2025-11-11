package me.mutlz.mToolskins.listener;

import me.mutlz.mToolskins.MToolskins;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;

public class ToolskinListener implements Listener {

    private final MToolskins plugin;

    public ToolskinListener(MToolskins plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        plugin.clearAttachment(event.getPlayer());
    }
}

