package dev.kits.plugin.managers;

import dev.kits.plugin.KitsPlugin;
import dev.kits.plugin.models.Kit;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.io.*;
import java.util.*;

public class KitManager {

    private final KitsPlugin plugin;
    private final List<Kit> kits = new ArrayList<>();

    // UUID -> kitId -> timestamp ultima folosire (epoch seconds)
    private final Map<UUID, Map<String, Long>> cooldowns = new HashMap<>();
    private File cooldownFile;
    private org.bukkit.configuration.file.YamlConfiguration cooldownConfig;

    public KitManager(KitsPlugin plugin) {
        this.plugin = plugin;
        loadCooldowns();
        loadKits();
    }

    // ─────────────────────────────────────────────
    //  Incarca kiturile din config
    // ─────────────────────────────────────────────
    public void loadKits() {
        kits.clear();
        ConfigurationSection kitsSection = plugin.getConfig().getConfigurationSection("kits");
        if (kitsSection == null) return;

        for (String kitId : kitsSection.getKeys(false)) {
            ConfigurationSection ks = kitsSection.getConfigurationSection(kitId);
            if (ks == null) continue;

            String displayName = color(ks.getString("display-name", kitId));
            String permission  = ks.getString("permission", "kits." + kitId);
            long cooldown      = ks.getLong("cooldown", 3600);
            String iconStr     = ks.getString("icon", "CHEST");
            Material icon      = parseMaterial(iconStr, Material.CHEST);

            List<String> desc = new ArrayList<>();
            for (String line : ks.getStringList("description")) desc.add(color(line));

            List<ItemStack> items = new ArrayList<>();
            for (String itemStr : ks.getStringList("items")) {
                ItemStack item = parseItem(itemStr);
                if (item != null) items.add(item);
            }

            kits.add(new Kit(kitId, displayName, permission, cooldown, icon, desc, items));
        }
    }

    // ─────────────────────────────────────────────
    //  Claim kit
    // ─────────────────────────────────────────────
    public boolean claimKit(Player player, Kit kit) {
        // Verifica permisiune
        if (!player.hasPermission(kit.getPermission())) {
            player.sendMessage(color("&c&lKIT &8» &cNu ai acces la kit-ul &e" + kit.getDisplayName() + "&c!"));
            player.sendMessage(color("  &7Acest kit necesita rank &6Donator&7."));
            player.sendMessage(color("  &7Doneaza &a5 euro &7si primesti accesul!"));
            return false;
        }

        // Verifica cooldown
        if (!player.hasPermission("kits.cooldown.bypass")) {
            long remaining = getRemainingCooldown(player, kit);
            if (remaining > 0) {
                player.sendMessage(color("&c&lKIT &8» &cKit-ul &e" + kit.getDisplayName()
                        + " &ceste in cooldown!"));
                player.sendMessage(color("  &7Mai ai de asteptat: &e" + formatTime(remaining)));
                return false;
            }
        }

        // Da itemele
        for (ItemStack item : kit.getItems()) {
            Map<Integer, ItemStack> leftover = player.getInventory().addItem(item.clone());
            for (ItemStack drop : leftover.values()) {
                player.getWorld().dropItemNaturally(player.getLocation(), drop);
            }
        }

        // Seteaza cooldown
        setCooldown(player, kit);

        player.sendMessage(color("&a&lKIT &8» &7Ai primit kit-ul &a" + kit.getDisplayName() + "&7!"));
        if (kit.getCooldownSeconds() > 0) {
            player.sendMessage(color("  &7Urmatoarea folosire in: &e" + formatTime(kit.getCooldownSeconds())));
        }
        player.playSound(player.getLocation(), org.bukkit.Sound.ENTITY_PLAYER_LEVELUP, 1f, 1.2f);
        return true;
    }

    // ─────────────────────────────────────────────
    //  Cooldown
    // ─────────────────────────────────────────────
    public long getRemainingCooldown(Player player, Kit kit) {
        Map<String, Long> playerCooldowns = cooldowns.get(player.getUniqueId());
        if (playerCooldowns == null) return 0;
        Long lastUsed = playerCooldowns.get(kit.getId());
        if (lastUsed == null) return 0;

        long now = System.currentTimeMillis() / 1000;
        long elapsed = now - lastUsed;
        long remaining = kit.getCooldownSeconds() - elapsed;
        return Math.max(0, remaining);
    }

    public boolean isOnCooldown(Player player, Kit kit) {
        return getRemainingCooldown(player, kit) > 0;
    }

    private void setCooldown(Player player, Kit kit) {
        cooldowns.computeIfAbsent(player.getUniqueId(), k -> new HashMap<>())
                .put(kit.getId(), System.currentTimeMillis() / 1000);
        saveCooldowns();
    }

    public void resetCooldown(Player player, Kit kit) {
        Map<String, Long> pc = cooldowns.get(player.getUniqueId());
        if (pc != null) {
            pc.remove(kit.getId());
            saveCooldowns();
        }
    }

    public void resetAllCooldowns(Player player) {
        cooldowns.remove(player.getUniqueId());
        saveCooldowns();
    }

    // ─────────────────────────────────────────────
    //  Persistenta cooldown (YAML)
    // ─────────────────────────────────────────────
    private void loadCooldowns() {
        cooldownFile = new File(plugin.getDataFolder(), "cooldowns.yml");
        if (!cooldownFile.exists()) {
            try { cooldownFile.createNewFile(); } catch (IOException e) { e.printStackTrace(); }
        }
        cooldownConfig = org.bukkit.configuration.file.YamlConfiguration.loadConfiguration(cooldownFile);

        for (String uuidStr : cooldownConfig.getKeys(false)) {
            try {
                UUID uuid = UUID.fromString(uuidStr);
                ConfigurationSection section = cooldownConfig.getConfigurationSection(uuidStr);
                if (section == null) continue;
                Map<String, Long> map = new HashMap<>();
                for (String kitId : section.getKeys(false)) {
                    map.put(kitId, section.getLong(kitId));
                }
                cooldowns.put(uuid, map);
            } catch (Exception ignored) {}
        }
    }

    private void saveCooldowns() {
        for (Map.Entry<UUID, Map<String, Long>> entry : cooldowns.entrySet()) {
            for (Map.Entry<String, Long> cd : entry.getValue().entrySet()) {
                cooldownConfig.set(entry.getKey().toString() + "." + cd.getKey(), cd.getValue());
            }
        }
        try { cooldownConfig.save(cooldownFile); } catch (IOException e) { e.printStackTrace(); }
    }

    // ─────────────────────────────────────────────
    //  Getters
    // ─────────────────────────────────────────────
    public List<Kit> getKits() { return kits; }

    public Kit getKit(String id) {
        return kits.stream().filter(k -> k.getId().equalsIgnoreCase(id)).findFirst().orElse(null);
    }

    // ─────────────────────────────────────────────
    //  Utilitare
    // ─────────────────────────────────────────────
    public String formatTime(long seconds) {
        if (seconds <= 0) return "0s";
        long h = seconds / 3600;
        long m = (seconds % 3600) / 60;
        long s = seconds % 60;
        StringBuilder sb = new StringBuilder();
        if (h > 0) sb.append(h).append("h ");
        if (m > 0) sb.append(m).append("m ");
        if (s > 0 || sb.length() == 0) sb.append(s).append("s");
        return sb.toString().trim();
    }

    private ItemStack parseItem(String s) {
        try {
            String[] parts = s.split(":");
            Material mat = Material.matchMaterial(parts[0].trim().toUpperCase());
            if (mat == null) return null;
            int amount = parts.length > 1 ? Integer.parseInt(parts[1].trim()) : 1;
            return new ItemStack(mat, amount);
        } catch (Exception e) {
            plugin.getLogger().warning("Item invalid in config: " + s);
            return null;
        }
    }

    private Material parseMaterial(String s, Material def) {
        Material m = Material.matchMaterial(s.toUpperCase());
        return m != null ? m : def;
    }

    private String color(String s) {
        return org.bukkit.ChatColor.translateAlternateColorCodes('&', s);
    }
}
