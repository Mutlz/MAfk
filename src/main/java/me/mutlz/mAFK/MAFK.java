package me.mutlz.mAFK;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.*;
import java.util.stream.Collectors;

public class MAFK extends JavaPlugin implements Listener {

    private final Map<UUID, Inventory> activeGUIs = new HashMap<>();
    private Set<Material> monitoredBlocks;
    private List<Material> guiItems;
    private double triggerChance;
    private String correctMessage;
    private String kickMessage;
    private boolean warnBeforeKick;
    private int guiSize;
    private int itemCount;
    private final Random random = new Random();

    @Override
    public void onEnable() {
        saveDefaultConfig();
        loadConfig();
        getServer().getPluginManager().registerEvents(this, this);
        getLogger().info("AntiAFKPlugin enabled with GUI size: " + guiSize + ", Item count: " + itemCount);
    }

    private void loadConfig() {
        FileConfiguration config = getConfig();
        triggerChance = config.getDouble("trigger-chance", 1.0);
        correctMessage = config.getString("messages.correct", "§aDoğru seçimi yaptınız!");
        kickMessage = config.getString("messages.kick", "§cYanlış seçim yaptınız! AFK kontrolünü geçemediniz.");
        warnBeforeKick = config.getBoolean("warn-before-kick", false);
        guiSize = config.getInt("gui-size", 54);
        itemCount = config.getInt("item-count", 12);

        monitoredBlocks = config.getStringList("monitored-blocks").stream()
                .map(name -> {
                    try {
                        return Material.valueOf(name.toUpperCase());
                    } catch (IllegalArgumentException e) {
                        getLogger().warning("Geçersiz blok: " + name);
                        return null;
                    }
                })
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());

        guiItems = config.getStringList("gui-items").stream()
                .map(name -> {
                    try {
                        return Material.valueOf(name.toUpperCase());
                    } catch (IllegalArgumentException e) {
                        getLogger().warning("Geçersiz eşya: " + name);
                        return null;
                    }
                })
                .filter(Objects::nonNull)
                .collect(Collectors.toList());

        if (guiItems.isEmpty()) {
            getLogger().warning("GUI eşya listesi boş! Varsayılan eşyalar kullanılıyor.");
            guiItems = Arrays.asList(Material.STONE, Material.IRON_INGOT, Material.GOLD_INGOT, Material.COAL, Material.DIAMOND, Material.EMERALD);
        }
        if (itemCount > guiSize) {
            itemCount = guiSize;
            getLogger().warning("item-count, gui-size'dan büyük olamaz! item-count: " + itemCount + " olarak ayarlandı.");
        }
    }

    @EventHandler
    public void onBlockBreak(BlockBreakEvent event) {
        Player player = event.getPlayer();
        if (player.hasPermission("antiafk.bypass") || activeGUIs.containsKey(player.getUniqueId())) return;

        if (monitoredBlocks.contains(event.getBlock().getType()) && random.nextDouble() < triggerChance) {
            openAFKCheckGUI(player);
        }
    }

    private void openAFKCheckGUI(Player player) {
        getLogger().info("Opening AFK GUI for " + player.getName());
        Inventory gui = Bukkit.createInventory(null, guiSize, "AFK Kontrolü");
        List<Integer> slots = new ArrayList<>();
        for (int i = 0; i < guiSize; i++) slots.add(i);
        Collections.shuffle(slots, random);

        int correctSlot = slots.get(0);
        for (int i = 0; i < itemCount; i++) {
            Material itemType = guiItems.get(random.nextInt(guiItems.size()));
            ItemStack item = new ItemStack(itemType);
            ItemMeta meta = item.getItemMeta();
            if (meta != null) {
                meta.setDisplayName(i == 0 ? "§aDoğru" : "§cYanlış");
                item.setItemMeta(meta);
            }
            gui.setItem(slots.get(i), item);
        }

        activeGUIs.put(player.getUniqueId(), gui);
        player.openInventory(gui);
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        Player player = (Player) event.getWhoClicked();
        Inventory activeGUI = activeGUIs.get(player.getUniqueId());
        if (event.getClickedInventory() == null || !event.getClickedInventory().equals(activeGUI)) return;

        event.setCancelled(true);
        ItemStack clickedItem = event.getCurrentItem();
        if (clickedItem == null || !clickedItem.hasItemMeta()) return;

        ItemMeta meta = clickedItem.getItemMeta();
        if (meta == null || !meta.hasDisplayName()) return;

        if (meta.getDisplayName().equals("§aDoğru")) {
            player.sendMessage(correctMessage);
            activeGUIs.remove(player.getUniqueId());
            player.closeInventory();
        } else {
            if (warnBeforeKick) {
                player.sendMessage("§cYanlış seçim! Lütfen doğru eşyayı seçin.");
            } else {
                player.kickPlayer(kickMessage);
            }
        }
    }

    @EventHandler
    public void onInventoryClose(InventoryCloseEvent event) {
        Player player = (Player) event.getPlayer();
        Inventory activeGUI = activeGUIs.get(player.getUniqueId());
        if (activeGUI != null && event.getInventory().equals(activeGUI)) {
            Bukkit.getScheduler().runTask(this, () -> player.openInventory(activeGUI));
        }
    }
}