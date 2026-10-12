package dev.donutspawners.plugin.gui;

import dev.donutspawners.plugin.DonutSpawnersPlugin;
import dev.donutspawners.plugin.data.SpawnerData;
import org.bukkit.*;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.*;

/**
 * GUI care se deschide la right-click pe spawner.
 * Arata stocul acumulat si permite colectarea.
 */
public class SpawnerGUI implements Listener {

    private final DonutSpawnersPlugin plugin;
    private static final String TITLE_PREFIX = "§8[ §6⚙ ";

    // Map player -> spawner data pentru care e deschis GUI-ul
    private final Map<UUID, SpawnerData> openGUIs = new HashMap<>();

    public SpawnerGUI(DonutSpawnersPlugin plugin) {
        this.plugin = plugin;
    }

    // ─────────────────────────────────────────────
    //  Deschide GUI
    // ─────────────────────────────────────────────
    public void openFor(Player player, SpawnerData data) {
        ConfigurationSection typeCfg = plugin.getConfig()
                .getConfigurationSection("spawner-types." + data.getSpawnerType());

        String displayName = typeCfg != null
                ? typeCfg.getString("display-name", data.getSpawnerType())
                : data.getSpawnerType();
        String tier = typeCfg != null ? typeCfg.getString("tier", "?") : "?";

        String title = color(TITLE_PREFIX + displayName + " §8]");
        Inventory inv = Bukkit.createInventory(null, 27, title);

        // Border
        ItemStack border = makeItem(Material.BLACK_STAINED_GLASS_PANE, "&0", Collections.emptyList());
        for (int i = 0; i < 9; i++) inv.setItem(i, border);
        for (int i = 18; i < 27; i++) inv.setItem(i, border);
        inv.setItem(9, border); inv.setItem(17, border);

        // Info spawner - slot 0
        int maxStock = plugin.getConfig().getInt("max-stock", 1000);
        int totalItems = data.getTotalItems();
        int stackSize = data.getStackSize();
        String ownerName = Bukkit.getOfflinePlayer(data.getOwnerUUID()).getName();

        inv.setItem(0, makeItem(Material.SPAWNER, color(displayName),
                Arrays.asList(
                        color("&8▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬"),
                        color("  &7Tier: &e" + tier),
                        color("  &7Stack: &e" + stackSize + "x"),
                        color("  &7Proprietar: &f" + (ownerName != null ? ownerName : "?")),
                        color("&8▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬"),
                        color("  &7Stoc: &e" + totalItems + "&8/&e" + maxStock),
                        color("  &7Interval drop: &e" + plugin.getConfig().getInt("tick-interval", 30) + "s"),
                        color("&8▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬")
                )));

        // Drop-urile acumulate - sloturi 10-16
        int slot = 10;
        Map<Material, Integer> stock = data.getStock();

        if (stock.isEmpty()) {
            // Nimic acumulat inca
            inv.setItem(13, makeItem(Material.GRAY_DYE, "&7Nimic acumulat inca...",
                    Arrays.asList(
                            color("&7Spawnerul genereaza drops"),
                            color("&7la fiecare &e" + plugin.getConfig().getInt("tick-interval", 30) + " &7secunde."),
                            color("&7Revino mai tarziu!")
                    )));
        } else {
            for (Map.Entry<Material, Integer> entry : stock.entrySet()) {
                if (slot > 16) break;
                int amount = entry.getValue();
                String matName = formatMaterialName(entry.getKey().name());

                inv.setItem(slot, makeItem(entry.getKey(), "&f" + matName,
                        Arrays.asList(
                                color("&8▬▬▬▬▬▬▬▬▬▬"),
                                color("  &7Cantitate: &e" + amount),
                                color("&8▬▬▬▬▬▬▬▬▬▬")
                        )));
                slot++;
            }
        }

        // Buton colecteaza tot - slot 22
        if (!stock.isEmpty()) {
            inv.setItem(22, makeItem(Material.CHEST, "&a&l✔ COLECTEAZA TOT",
                    Arrays.asList(
                            color("&7Click pentru a lua"),
                            color("&7toate drop-urile in inventar."),
                            color(""),
                            color("&eTotal: &a" + totalItems + " &7iteme")
                    )));
        } else {
            inv.setItem(22, makeItem(Material.BARRIER, "&c&l✗ Stoc gol",
                    Collections.singletonList(color("&7Nu e nimic de colectat."))));
        }

        // Buton inchide - slot 26
        inv.setItem(26, makeItem(Material.ARROW, "&c&lINTCHIDE",
                Collections.singletonList(color("&7Inchide meniul."))));

        openGUIs.put(player.getUniqueId(), data);
        player.openInventory(inv);
        player.playSound(player.getLocation(), Sound.BLOCK_CHEST_OPEN, 0.6f, 1.1f);
    }

    // ─────────────────────────────────────────────
    //  Event
    // ─────────────────────────────────────────────
    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player)) return;
        Player player = (Player) event.getWhoClicked();

        String title = event.getView().getTitle();
        if (!title.startsWith(ChatColor.DARK_GRAY + "[ " + ChatColor.GOLD + "⚙")) return;

        event.setCancelled(true);
        SpawnerData data = openGUIs.get(player.getUniqueId());
        if (data == null) return;

        int slot = event.getRawSlot();

        if (slot == 22) {
            // Colecteaza tot
            if (!data.isEmpty()) {
                player.closeInventory();
                plugin.getSpawnerManager().collectDrops(player, data);
            }
        } else if (slot == 26) {
            player.closeInventory();
            player.playSound(player.getLocation(), Sound.BLOCK_CHEST_CLOSE, 0.6f, 1.1f);
        }
    }

    public void removeViewer(UUID uuid) {
        openGUIs.remove(uuid);
    }

    // ─────────────────────────────────────────────
    //  Utilitare
    // ─────────────────────────────────────────────
    private String formatMaterialName(String name) {
        String[] parts = name.toLowerCase().split("_");
        StringBuilder sb = new StringBuilder();
        for (String p : parts) sb.append(Character.toUpperCase(p.charAt(0))).append(p.substring(1)).append(" ");
        return sb.toString().trim();
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
