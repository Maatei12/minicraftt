package dev.kits.plugin.gui;

import dev.kits.plugin.KitsPlugin;
import dev.kits.plugin.models.Kit;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Meniu kits - 54 sloturi
 * Border pe margini, kits centrate in interior
 * Fiecare kit are un item reprezentativ cu lore complet
 */
public class KitsGUI implements Listener {

    private final KitsPlugin plugin;
    private static final String MENU_IDENTIFIER = "⚔ KITURI ⚔";

    public KitsGUI(KitsPlugin plugin) {
        this.plugin = plugin;
    }

    public void openMenu(Player player) {
        String title = color(plugin.getConfig().getString("menu-title", "&8[ &6⚔ &eKITURI &6⚔ &8]"));
        Inventory inv = Bukkit.createInventory(null, 54, title);

        // Border negru pe toate marginile
        ItemStack border = makeItem(Material.BLACK_STAINED_GLASS_PANE, "&0", Collections.emptyList());
        // Rand top si bottom
        for (int i = 0; i < 9; i++) inv.setItem(i, border);
        for (int i = 45; i < 54; i++) inv.setItem(i, border);
        // Laterale
        for (int i = 1; i < 5; i++) {
            inv.setItem(i * 9, border);
            inv.setItem(i * 9 + 8, border);
        }

        // Fundal interior gri
        ItemStack bg = makeItem(Material.GRAY_STAINED_GLASS_PANE, "&8", Collections.emptyList());
        for (int row = 1; row <= 4; row++) {
            for (int col = 1; col <= 7; col++) {
                inv.setItem(row * 9 + col, bg);
            }
        }

        // Plaseaza kiturile
        List<Kit> kits = plugin.getKitManager().getKits();

        // Pozitii centrate pentru kits (max 7 pe rand, incepem la randul 2)
        // Pentru 2 kits: sloturi 21 si 23 (centrate pe rand 2)
        // Formula: centram in randul 2 (sloturi 10-16)
        int[] kitSlots = getKitSlots(kits.size());

        for (int i = 0; i < kits.size() && i < kitSlots.length; i++) {
            Kit kit = kits.get(i);
            inv.setItem(kitSlots[i], buildKitItem(player, kit));
        }

        // Buton inchide jos centru
        inv.setItem(49, makeItem(Material.BARRIER, "&c&lINTCHIDE",
                Collections.singletonList(color("&7Click pentru a inchide meniul."))));

        player.openInventory(inv);
        player.playSound(player.getLocation(), org.bukkit.Sound.BLOCK_CHEST_OPEN, 0.6f, 1.2f);
    }

    /**
     * Returneaza sloturile in care sa puna kiturile, centrate in GUI
     */
    private int[] getKitSlots(int count) {
        // Centram pe randul 2 (index 1) si 3 (index 2)
        // Randul 2 = sloturi 10-16, randul 3 = sloturi 19-25
        if (count <= 3) {
            // Centrat pe randul 2: 12, 13, 14 sau 21, 22 (rand mijloc)
            int start = 9 + (9 - count * 2 + 1) / 2;
            int[] slots = new int[count];
            for (int i = 0; i < count; i++) slots[i] = 22 - (count / 2) + i * 2;
            // Doua kits: 21, 23 -> centru
            if (count == 1) return new int[]{22};
            if (count == 2) return new int[]{21, 23};
            return new int[]{20, 22, 24};
        }
        if (count <= 6) {
            // Doua randuri
            int[] slots = new int[count];
            int[] row1 = {20, 22, 24};
            int[] row2 = {29, 31, 33};
            for (int i = 0; i < count; i++) {
                slots[i] = i < 3 ? row1[i] : row2[i - 3];
            }
            return slots;
        }
        // Mai mult de 6 - umplem normal
        int[] slots = new int[count];
        int col = 0; int row = 1;
        for (int i = 0; i < count; i++) {
            slots[i] = row * 9 + col + 1;
            col++;
            if (col > 6) { col = 0; row++; }
        }
        return slots;
    }

    private ItemStack buildKitItem(Player player, Kit kit) {
        boolean hasAccess = player.hasPermission(kit.getPermission());
        boolean onCooldown = plugin.getKitManager().isOnCooldown(player, kit) && !player.hasPermission("kits.cooldown.bypass");
        long remaining = plugin.getKitManager().getRemainingCooldown(player, kit);

        Material displayMat;
        if (!hasAccess) {
            displayMat = Material.RED_STAINED_GLASS; // Nu ai acces
        } else if (onCooldown) {
            displayMat = Material.ORANGE_STAINED_GLASS; // Cooldown
        } else {
            displayMat = kit.getIcon(); // Disponibil
        }

        String displayName;
        if (!hasAccess) {
            displayName = "&c&l✗ " + ChatColor.stripColor(kit.getDisplayName()) + " &8(Blocat)";
        } else if (onCooldown) {
            displayName = "&e&l⌛ " + ChatColor.stripColor(kit.getDisplayName()) + " &8(Cooldown)";
        } else {
            displayName = "&a&l✔ " + ChatColor.stripColor(kit.getDisplayName());
        }

        List<String> lore = new ArrayList<>();
        lore.add(color("&8▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬"));

        // Descriere kit
        for (String line : kit.getDescription()) lore.add("  " + line);
        lore.add(color("&8▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬"));

        // Status
        if (!hasAccess) {
            lore.add(color("  &c&l✗ &cNu ai acces la acest kit!"));
            lore.add(color("  &7Necesita rank: &6Donator"));
            lore.add(color("  &7Doneaza &a5 euro &7pentru acces!"));
        } else if (onCooldown) {
            lore.add(color("  &e⌛ &7Cooldown: &e" + plugin.getKitManager().formatTime(remaining)));
            lore.add(color("  &7Revino mai tarziu!"));
        } else {
            lore.add(color("  &a✔ &7Disponibil acum!"));
            lore.add(color("  &7Cooldown dupa folosire: &e" + plugin.getKitManager().formatTime(kit.getCooldownSeconds())));
        }

        lore.add(color("&8▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬"));

        if (!hasAccess) {
            lore.add(color("  &c&l» Nu poti folosi acest kit «"));
        } else if (onCooldown) {
            lore.add(color("  &e&l» Inca in cooldown «"));
        } else {
            lore.add(color("  &a&l» Click pentru a lua kitul! «"));
        }

        ItemStack item = new ItemStack(displayMat);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(color(displayName));
            meta.setLore(lore);
            item.setItemMeta(meta);
        }
        return item;
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player)) return;
        Player player = (Player) event.getWhoClicked();

        String title = event.getView().getTitle();
        if (!title.contains("KITURI") && !title.contains("KITS")) return;

        event.setCancelled(true);
        if (event.getCurrentItem() == null || event.getCurrentItem().getType() == Material.AIR) return;

        int slot = event.getRawSlot();

        // Buton inchide
        if (event.getCurrentItem().getType() == Material.BARRIER) {
            player.closeInventory();
            return;
        }

        // Click pe un kit - identificam dupa slot
        List<Kit> kits = plugin.getKitManager().getKits();
        int[] kitSlots = getKitSlots(kits.size());

        for (int i = 0; i < kitSlots.length && i < kits.size(); i++) {
            if (kitSlots[i] == slot) {
                Kit kit = kits.get(i);
                player.closeInventory();
                Bukkit.getScheduler().runTask(plugin, () ->
                        plugin.getKitManager().claimKit(player, kit));
                return;
            }
        }
    }

    private static ItemStack makeItem(Material mat, String name, List<String> lore) {
        ItemStack item = new ItemStack(mat);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(color(name));
            if (!lore.isEmpty()) meta.setLore(lore);
            item.setItemMeta(meta);
        }
        return item;
    }

    private static String color(String s) {
        return ChatColor.translateAlternateColorCodes('&', s);
    }
}
