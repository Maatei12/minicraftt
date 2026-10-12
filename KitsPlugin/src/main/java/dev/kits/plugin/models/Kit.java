package dev.kits.plugin.models;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import java.util.List;

public class Kit {

    private final String id;
    private final String displayName;
    private final String permission;
    private final long cooldownSeconds;
    private final Material icon;
    private final List<String> description;
    private final List<ItemStack> items;

    public Kit(String id, String displayName, String permission,
               long cooldownSeconds, Material icon,
               List<String> description, List<ItemStack> items) {
        this.id = id;
        this.displayName = displayName;
        this.permission = permission;
        this.cooldownSeconds = cooldownSeconds;
        this.icon = icon;
        this.description = description;
        this.items = items;
    }

    public String getId() { return id; }
    public String getDisplayName() { return displayName; }
    public String getPermission() { return permission; }
    public long getCooldownSeconds() { return cooldownSeconds; }
    public Material getIcon() { return icon; }
    public List<String> getDescription() { return description; }
    public List<ItemStack> getItems() { return items; }
}
