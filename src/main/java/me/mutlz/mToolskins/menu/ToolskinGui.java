package me.mutlz.mToolskins.menu;

import dev.triumphteam.gui.builder.gui.Gui;
import dev.triumphteam.gui.builder.item.ItemBuilder;
import dev.triumphteam.gui.guis.PaginatedGui;
import dev.triumphteam.gui.guis.GuiItem;
import me.mutlz.mToolskins.MToolskins;
import me.mutlz.mToolskins.model.SkinEntry;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;

public class ToolskinGui {

    private static final int TOTAL_ROWS = 6;
    private static final int PAGE_SIZE = 45;
    private static final int BOTTOM_ROW = 6;
    private static final int PREVIOUS_COLUMN = 1;
    private static final int NEXT_COLUMN = 9;

    private final MToolskins plugin;
    private final Player player;
    private final PaginatedGui gui;
    private final List<SkinEntry> entries;

    public ToolskinGui(MToolskins plugin, Player player, List<SkinEntry> entries, Component title) {
        this.plugin = plugin;
        this.player = player;
        this.entries = entries;

        this.gui = Gui.paginated()
                .rows(TOTAL_ROWS)
                .pageSize(PAGE_SIZE)
                .title(title)
                .create();

        gui.setDefaultClickAction(event -> event.setCancelled(true));
        fillBottomRow();
        populateSkins();
        refreshNavigationItems();
    }

    public void open() {
        gui.open(player);
    }

    private void populateSkins() {
        for (SkinEntry entry : entries) {
            gui.addItem(createSkinItem(entry));
        }
    }

    private GuiItem createSkinItem(SkinEntry entry) {
        boolean hasPermission = player.hasPermission(entry.getPermission());

        List<Component> lore = new ArrayList<>();
        lore.add(Component.text()
                .color(NamedTextColor.GRAY)
                .append(Component.text("CustomModelData: "))
                .append(Component.text(String.valueOf(entry.getCustomModelData()), NamedTextColor.WHITE))
                .build());
        lore.add(Component.text()
                .color(NamedTextColor.GRAY)
                .append(Component.text("Gereken Permission: "))
                .append(Component.text(entry.getPermission(), NamedTextColor.WHITE))
                .build());
        lore.add(Component.text(hasPermission ? "Kullanılabilir" : "Kilitli")
                .color(hasPermission ? NamedTextColor.GREEN : NamedTextColor.RED));

        return ItemBuilder.from(entry.getDisplayMaterial())
                .name(Component.text(entry.getDisplayName(), NamedTextColor.YELLOW))
                .lore(lore)
                .flags(org.bukkit.inventory.ItemFlag.HIDE_ATTRIBUTES)
                .asGuiItem(event -> {
                    event.setCancelled(true);
                    if (!(event.getWhoClicked() instanceof Player clicked) || !clicked.getUniqueId().equals(player.getUniqueId())) {
                        return;
                    }

                    if (!hasPermission(clicked, entry)) {
                        clicked.sendMessage(plugin.getMessageManager().getMessage("errors.no-permission"));
                        return;
                    }

                    ItemStack mainHand = clicked.getInventory().getItemInMainHand();
                    if (mainHand == null || mainHand.getType().isAir()) {
                        clicked.sendMessage(plugin.getMessageManager().getMessage("errors.no-item"));
                        gui.close(clicked);
                        return;
                    }

                    ItemMeta meta = mainHand.getItemMeta();
                    if (meta == null) {
                        meta = clicked.getServer().getItemFactory().getItemMeta(mainHand.getType());
                    }

                    if (meta == null) {
                        clicked.sendMessage(plugin.getMessageManager().getMessage("errors.unexpected"));
                        return;
                    }

                    meta.setCustomModelData(entry.getCustomModelData());
                    mainHand.setItemMeta(meta);
                    clicked.getInventory().setItemInMainHand(mainHand);
                    clicked.updateInventory();

                    plugin.getOrCreateAttachment(clicked).setPermission(entry.getPermission(), false);

                    clicked.sendMessage(plugin.getMessageManager().getMessage("success.skin-applied"));
                    gui.close(clicked);
                });
    }

    private boolean hasPermission(Player player, SkinEntry entry) {
        return player.hasPermission(entry.getPermission());
    }

    private void fillBottomRow() {
        GuiItem filler = ItemBuilder.from(Material.GRAY_STAINED_GLASS_PANE)
                .name(Component.space())
                .asGuiItem(event -> event.setCancelled(true));
        gui.getFiller().fillBottom(filler);
    }

    private void refreshNavigationItems() {
        boolean hasPrevious = gui.getCurrentPageNum() > 0;
        boolean hasNext = gui.getPagesNum() - 1 > gui.getCurrentPageNum();

        gui.setItem(BOTTOM_ROW, PREVIOUS_COLUMN, hasPrevious ? createPreviousButton() : createDisabledButton("◀ Geri"));
        gui.setItem(BOTTOM_ROW, NEXT_COLUMN, hasNext ? createNextButton() : createDisabledButton("İleri ▶"));
    }

    private GuiItem createPreviousButton() {
        return ItemBuilder.from(Material.ARROW)
                .name(Component.text("◀ Geri", NamedTextColor.YELLOW))
                .asGuiItem(event -> {
                    event.setCancelled(true);
                    if (gui.previous()) {
                        refreshNavigationItems();
                    }
                });
    }

    private GuiItem createNextButton() {
        return ItemBuilder.from(Material.ARROW)
                .name(Component.text("İleri ▶", NamedTextColor.YELLOW))
                .asGuiItem(event -> {
                    event.setCancelled(true);
                    if (gui.next()) {
                        refreshNavigationItems();
                    }
                });
    }

    private GuiItem createDisabledButton(String name) {
        return ItemBuilder.from(Material.GRAY_STAINED_GLASS_PANE)
                .name(Component.text(name, NamedTextColor.DARK_GRAY))
                .asGuiItem(event -> event.setCancelled(true));
    }
}

