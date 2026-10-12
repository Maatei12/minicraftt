package dev.mobcoins.plugin.gui;

import dev.mobcoins.plugin.MobCoinsPlugin;
import org.bukkit.*;
import org.bukkit.block.CreatureSpawner;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BlockStateMeta;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.*;

/**
 * Shop principal MobCoins cu 3 categorii:
 *   - Spawnere (compatibile DonutSpawnersPlugin)
 *   - Fly (temp fly cu /fly add)
 *   - Altele (viitor)
 *
 * Layout meniu principal (54 sloturi):
 * R0: border
 * R1: [border][SPAWNER_CAT][spacer][FLY_CAT][spacer][ALTELE_CAT][border] + balanta sus
 * R2-R4: spatiu gol / decoratii
 * R5: border
 */
public class ShopGUI implements Listener {

    private final MobCoinsPlugin plugin;

    // Titluri fixe pentru detectare click
    private static final String TITLE_MAIN     = color("&8[ &6🍯 &eShop Miere &6🍯 &8]");
    private static final String TITLE_SPAWNERS = color("&8[ &6⚙ &eSpawnere &6⚙ &8]");
    private static final String TITLE_FLY      = color("&8[ &b✈ &eFly &b✈ &8]");

    public ShopGUI(MobCoinsPlugin plugin) {
        this.plugin = plugin;
    }

    // ─────────────────────────────────────────────
    //  MENIU PRINCIPAL
    // ─────────────────────────────────────────────
    public void openShop(Player player) {
        Inventory inv = Bukkit.createInventory(null, 54, TITLE_MAIN);
        long bal = plugin.getCoinManager().getBalance(player.getUniqueId());
        String sym = sym();

        // Border
        border(inv, 54);

        // Spacer interior
        ItemStack spacer = makeItem(Material.GRAY_STAINED_GLASS_PANE, "&0", Collections.emptyList());
        for (int s : new int[]{10,12,14,16,19,20,21,22,23,24,25,28,29,30,31,32,33,34,37,38,39,40,41,42,43}) inv.setItem(s, spacer);

        // Balanta centru sus
        inv.setItem(4, makeItem(Material.HONEYCOMB, "&e&l" + sym + " " + bal + " Miere",
                Arrays.asList(
                        color("&8▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬"),
                        color("  &7Balanta ta curenta."),
                        color("  &7Omoara mobi pentru mai multa miere!"),
                        color("&8▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬")
                )));

        // ── Categorie Spawnere ──
        List<Map<?, ?>> spawnerItems = plugin.getConfig().getMapList("spawner-shop");
        int spawnerCount = spawnerItems.size();
        inv.setItem(11, makeItem(Material.SPAWNER, "&6&l⚙ SPAWNERE",
                Arrays.asList(
                        color("&8▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬"),
                        color("  &7" + spawnerCount + " tipuri de spawnere"),
                        color("  &7Compatibil cu sistemul"),
                        color("  &7de drops pasive!"),
                        color("&8▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬"),
                        color("  &a» &fClick pentru a vedea")
                )));

        // ── Categorie Fly ──
        List<Map<?, ?>> flyItems = plugin.getConfig().getMapList("fly-shop");
        int flyCount = flyItems.size();
        inv.setItem(13, makeItem(Material.FEATHER, "&b&l✈ TEMP FLY",
                Arrays.asList(
                        color("&8▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬"),
                        color("  &7" + flyCount + " optiuni de zbor"),
                        color("  &7Cumpara timp de fly"),
                        color("  &7cu Miere!"),
                        color("&8▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬"),
                        color("  &a» &fClick pentru a vedea")
                )));

        // ── Categorie Altele (placeholder) ──
        inv.setItem(15, makeItem(Material.NETHER_STAR, "&d&l★ CURAND...",
                Arrays.asList(
                        color("&8▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬"),
                        color("  &7Mai multe produse"),
                        color("  &7vin in curand!"),
                        color("&8▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬")
                )));

        // Info jos centru
        inv.setItem(49, makeItem(Material.PAPER, "&e&lINFO",
                Arrays.asList(
                        color("&7Cumpara produse cu Miere."),
                        color("&7Obtii Miere omorand mobi!"),
                        color(""),
                        color("&e/mobcoins &7pentru balanta")
                )));

        player.openInventory(inv);
        player.playSound(player.getLocation(), Sound.BLOCK_CHEST_OPEN, 0.5f, 1.2f);
    }

    // ─────────────────────────────────────────────
    //  MENIU SPAWNERE
    // ─────────────────────────────────────────────
    public void openSpawnerShop(Player player) {
        List<Map<?, ?>> items = plugin.getConfig().getMapList("spawner-shop");
        int rows = Math.min(6, Math.max(3, (int) Math.ceil((items.size() + 2) / 7.0) + 2));
        int size = rows * 9;

        Inventory inv = Bukkit.createInventory(null, size, TITLE_SPAWNERS);
        long bal = plugin.getCoinManager().getBalance(player.getUniqueId());
        String sym = sym();

        border(inv, size);

        // Spacer interior
        ItemStack spacer = makeItem(Material.GRAY_STAINED_GLASS_PANE, "&0", Collections.emptyList());
        for (int r = 1; r < rows - 1; r++) {
            for (int c = 1; c <= 7; c++) inv.setItem(r * 9 + c, spacer);
        }

        // Balanta
        inv.setItem(4, makeItem(Material.HONEYCOMB, "&e&l" + sym + " " + bal + " Miere",
                Collections.singletonList(color("  &7Balanta ta curenta."))));

        // Spawner items
        int slot = 10;
        for (Map<?, ?> itemMap : items) {
            // Sari border
            while (slot % 9 == 0 || slot % 9 == 8) slot++;
            if (slot >= size - 9) break;

            String name        = String.valueOf(itemMap.get("name"));
            long price         = ((Number) itemMap.get("price")).longValue();
            boolean canAfford  = plugin.getCoinManager().hasCoins(player.getUniqueId(), price);

            List<String> lore = new ArrayList<>();
            lore.add(color("&8▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬"));
            Object loreObj = itemMap.get("lore");
            if (loreObj instanceof List) {
                for (Object line : (List<?>) loreObj) lore.add(color("  " + line));
            }
            lore.add(color("&8▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬"));
            if (canAfford) {
                lore.add(color("  &a✔ &7Poti cumpara!"));
                lore.add(color("  &a» &fClick pentru a cumpara"));
            } else {
                lore.add(color("  &c✗ &7Nu ai destula miere!"));
                lore.add(color("  &7Mai ai nevoie de: &c" + (price - bal) + " " + sym));
            }

            ItemStack shopItem = makeItem(Material.SPAWNER,
                    color((canAfford ? "&a✔ " : "&c✗ ") + name), lore);
            inv.setItem(slot, shopItem);
            slot++;
        }

        // Buton inapoi
        inv.setItem(size - 5, makeItem(Material.ARROW, "&c&l« INAPOI",
                Collections.singletonList(color("&7Inapoi la meniul principal."))));

        player.openInventory(inv);
        player.playSound(player.getLocation(), Sound.BLOCK_CHEST_OPEN, 0.5f, 1.1f);
    }

    // ─────────────────────────────────────────────
    //  MENIU FLY
    // ─────────────────────────────────────────────
    public void openFlyShop(Player player) {
        Inventory inv = Bukkit.createInventory(null, 54, TITLE_FLY);
        long bal = plugin.getCoinManager().getBalance(player.getUniqueId());
        String sym = sym();

        border(inv, 54);

        // Spacer
        ItemStack spacer = makeItem(Material.GRAY_STAINED_GLASS_PANE, "&0", Collections.emptyList());
        for (int s : new int[]{10,12,14,16,19,21,23,25,28,30,32,34,37,38,39,40,41,42,43}) inv.setItem(s, spacer);

        // Balanta
        inv.setItem(4, makeItem(Material.HONEYCOMB, "&e&l" + sym + " " + bal + " Miere",
                Collections.singletonList(color("  &7Balanta ta curenta."))));

        // Fly options
        int[] flySlots = {11, 13, 15, 20, 22, 24};
        List<Map<?, ?>> flyOptions = plugin.getConfig().getMapList("fly-shop");

        for (int i = 0; i < flyOptions.size() && i < flySlots.length; i++) {
            Map<?, ?> opt = flyOptions.get(i);
            String name    = String.valueOf(opt.get("name"));
            long price     = ((Number) opt.get("price")).longValue();
            long duration  = ((Number) opt.get("duration")).longValue();
            boolean canAfford = plugin.getCoinManager().hasCoins(player.getUniqueId(), price);

            List<String> lore = new ArrayList<>();
            lore.add(color("&8▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬"));
            Object loreObj = opt.get("lore");
            if (loreObj instanceof List) {
                for (Object l : (List<?>) loreObj) lore.add(color("  " + l));
            }
            lore.add(color("&8▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬"));
            if (canAfford) {
                lore.add(color("  &a✔ &7Poti cumpara!"));
                lore.add(color("  &a» &fClick pentru a cumpara"));
            } else {
                lore.add(color("  &c✗ &7Nu ai destula miere!"));
                lore.add(color("  &7Mai ai nevoie de: &c" + (price - bal) + " " + sym));
            }

            Material mat = canAfford ? Material.FEATHER : Material.DEAD_BUSH;
            inv.setItem(flySlots[i], makeItem(mat,
                    color((canAfford ? "&a✔ " : "&c✗ ") + name), lore));
        }

        // Info
        inv.setItem(31, makeItem(Material.PAPER, "&e&lINFO",
                Arrays.asList(
                        color("&7Timpul se adauga la"),
                        color("&7cel existent!"),
                        color("&7Poti cumpara de mai multe ori.")
                )));

        // Buton inapoi
        inv.setItem(49, makeItem(Material.ARROW, "&c&l« INAPOI",
                Collections.singletonList(color("&7Inapoi la meniul principal."))));

        player.openInventory(inv);
        player.playSound(player.getLocation(), Sound.ENTITY_BAT_TAKEOFF, 0.8f, 1.2f);
    }

    // ─────────────────────────────────────────────
    //  EVENT HANDLER
    // ─────────────────────────────────────────────
    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player)) return;
        Player player = (Player) event.getWhoClicked();
        String title = event.getView().getTitle();

        boolean isMain     = title.equals(TITLE_MAIN);
        boolean isSpawners = title.equals(TITLE_SPAWNERS);
        boolean isFly      = title.equals(TITLE_FLY);

        if (!isMain && !isSpawners && !isFly) return;
        event.setCancelled(true);

        ItemStack clicked = event.getCurrentItem();
        if (clicked == null || clicked.getType() == Material.AIR) return;

        int slot = event.getRawSlot();

        if (isMain) {
            if (slot == 11) {
                player.closeInventory();
                Bukkit.getScheduler().runTaskLater(plugin, () -> openSpawnerShop(player), 2L);
            } else if (slot == 13) {
                player.closeInventory();
                Bukkit.getScheduler().runTaskLater(plugin, () -> openFlyShop(player), 2L);
            }
            return;
        }

        if (isSpawners) {
            // Buton inapoi
            if (clicked.getType() == Material.ARROW) {
                player.closeInventory();
                Bukkit.getScheduler().runTaskLater(plugin, () -> openShop(player), 2L);
                return;
            }
            if (clicked.getType() != Material.SPAWNER) return;
            if (!clicked.hasItemMeta() || !clicked.getItemMeta().hasDisplayName()) return;

            // Identifica spawnerul din config
            String clickedName = ChatColor.stripColor(clicked.getItemMeta().getDisplayName())
                    .replaceAll("[✔✗] ", "").trim();
            for (Map<?, ?> itemMap : plugin.getConfig().getMapList("spawner-shop")) {
                String name = ChatColor.stripColor(color(String.valueOf(itemMap.get("name")))).trim();
                if (!name.equalsIgnoreCase(clickedName)) continue;
                long price = ((Number) itemMap.get("price")).longValue();
                String type = String.valueOf(itemMap.get("spawner-type"));
                handleSpawnerPurchase(player, price, type);
                return;
            }
            return;
        }

        if (isFly) {
            // Buton inapoi
            if (clicked.getType() == Material.ARROW) {
                player.closeInventory();
                Bukkit.getScheduler().runTaskLater(plugin, () -> openShop(player), 2L);
                return;
            }
            if (clicked.getType() != Material.FEATHER && clicked.getType() != Material.DEAD_BUSH) return;

            int[] flySlots = {11, 13, 15, 20, 22, 24};
            List<Map<?, ?>> flyOptions = plugin.getConfig().getMapList("fly-shop");
            for (int i = 0; i < flySlots.length && i < flyOptions.size(); i++) {
                if (flySlots[i] == slot) {
                    Map<?, ?> opt = flyOptions.get(i);
                    long price    = ((Number) opt.get("price")).longValue();
                    long duration = ((Number) opt.get("duration")).longValue();
                    String name   = String.valueOf(opt.get("name"));
                    handleFlyPurchase(player, price, duration, name);
                    return;
                }
            }
        }
    }

    // ─────────────────────────────────────────────
    //  Cumparare Spawner
    // ─────────────────────────────────────────────
    private void handleSpawnerPurchase(Player player, long price, String spawnerTypeStr) {
        String sym = sym();
        if (!plugin.getCoinManager().hasCoins(player.getUniqueId(), price)) {
            player.sendMessage(color("&c&l" + sym + " SHOP &8» &cNu ai destula miere!"));
            player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 1f, 1f);
            return;
        }
        if (player.getInventory().firstEmpty() == -1) {
            player.sendMessage(color("&c&l" + sym + " SHOP &8» &cInventarul tau este plin!"));
            return;
        }

        EntityType eType;
        try { eType = EntityType.valueOf(spawnerTypeStr.toUpperCase()); }
        catch (Exception e) { player.sendMessage(color("&cEroare tip spawner!")); return; }

        plugin.getCoinManager().removeCoins(player.getUniqueId(), price);

        // Creeaza spawner compatibil cu DonutSpawnersPlugin
        ItemStack spawnerItem = new ItemStack(Material.SPAWNER);
        BlockStateMeta bsm = (BlockStateMeta) spawnerItem.getItemMeta();
        if (bsm != null) {
            CreatureSpawner cs = (CreatureSpawner) bsm.getBlockState();
            cs.setSpawnedType(eType);
            bsm.setBlockState(cs);
            bsm.setDisplayName(color("&e" + formatName(spawnerTypeStr) + " Spawner"));
            bsm.setLore(Arrays.asList(
                    color("&8▬▬▬▬▬▬▬▬▬▬▬▬▬"),
                    color("  &7Cumparat din &6Miere Shop"),
                    color("  &7Pret: &e" + price + " " + sym),
                    color("&8▬▬▬▬▬▬▬▬▬▬▬▬▬"),
                    color("  &eRight-Click &7sa colectezi drops"),
                    color("  &eShift+Right-Click &7sa stacked"),
                    color("&8▬▬▬▬▬▬▬▬▬▬▬▬▬"),
                    color("  &8DonutSpawners Plugin")
            ));
            spawnerItem.setItemMeta(bsm);
        }

        player.getInventory().addItem(spawnerItem);
        long newBal = plugin.getCoinManager().getBalance(player.getUniqueId());
        player.sendMessage(color("&a&l" + sym + " SHOP &8» &7Ai cumparat &a" + formatName(spawnerTypeStr) + " Spawner&7!"));
        player.sendMessage(color("  &7Balanta ramasa: &e" + newBal + " " + sym));
        player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1f, 1.2f);

        Bukkit.getScheduler().runTaskLater(plugin, () -> openSpawnerShop(player), 2L);
    }

    // ─────────────────────────────────────────────
    //  Cumparare Fly
    // ─────────────────────────────────────────────
    private void handleFlyPurchase(Player player, long price, long duration, String name) {
        String sym = sym();
        if (!plugin.getCoinManager().hasCoins(player.getUniqueId(), price)) {
            player.sendMessage(color("&c&l✈ FLY &8» &cNu ai destula miere!"));
            player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 1f, 1f);
            return;
        }

        plugin.getCoinManager().removeCoins(player.getUniqueId(), price);
        Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "fly add " + player.getName() + " " + duration);

        long newBal = plugin.getCoinManager().getBalance(player.getUniqueId());
        String cleanName = ChatColor.stripColor(color(name));
        player.sendMessage(color("&b&l✈ FLY &8» &7Ai cumparat &b" + cleanName + "&7!"));
        player.sendMessage(color("  &7Timp adaugat: &b" + formatDuration(duration)));
        player.sendMessage(color("  &7Balanta ramasa: &e" + newBal + " " + sym));
        player.playSound(player.getLocation(), Sound.ENTITY_BAT_TAKEOFF, 1f, 1.5f);
        player.sendTitle(color("&b✈ FLY ACTIVAT"), color("&7" + formatDuration(duration) + " de zbor!"), 5, 40, 10);

        Bukkit.getScheduler().runTaskLater(plugin, () -> openFlyShop(player), 2L);
    }

    // ─────────────────────────────────────────────
    //  Utilitare
    // ─────────────────────────────────────────────
    private void border(Inventory inv, int size) {
        ItemStack b = makeItem(Material.BLACK_STAINED_GLASS_PANE, "&0", Collections.emptyList());
        for (int i = 0; i < 9; i++) inv.setItem(i, b);
        for (int i = size - 9; i < size; i++) inv.setItem(i, b);
        for (int r = 1; r < size / 9 - 1; r++) { inv.setItem(r * 9, b); inv.setItem(r * 9 + 8, b); }
    }

    private String formatName(String s) {
        StringBuilder sb = new StringBuilder();
        for (String p : s.toLowerCase().split("_"))
            sb.append(Character.toUpperCase(p.charAt(0))).append(p.substring(1)).append(" ");
        return sb.toString().trim();
    }

    private String formatDuration(long seconds) {
        if (seconds >= 86400) return (seconds / 86400) + " zi";
        if (seconds >= 3600)  return (seconds / 3600) + " ora" + (seconds / 3600 > 1 ? "e" : "");
        if (seconds >= 60)    return (seconds / 60) + " minute";
        return seconds + " secunde";
    }

    private String sym() {
        return plugin.getConfig().getString("coin-symbol", "🍯");
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
