package me.mutlz.mToolskins.model;

import org.bukkit.Material;

public class SkinEntry {

    private final String id;
    private final String displayName;
    private final Material displayMaterial;
    private final int customModelData;
    private final String permission;

    public SkinEntry(String id, String displayName, Material displayMaterial, int customModelData, String permission) {
        this.id = id;
        this.displayName = displayName;
        this.displayMaterial = displayMaterial;
        this.customModelData = customModelData;
        this.permission = permission;
    }

    public String getId() {
        return id;
    }

    public String getDisplayName() {
        return displayName;
    }

    public Material getDisplayMaterial() {
        return displayMaterial;
    }

    public int getCustomModelData() {
        return customModelData;
    }

    public String getPermission() {
        return permission;
    }
}

