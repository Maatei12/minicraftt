package dev.trade.tradeplugin.gui;

import dev.trade.tradeplugin.TradePlugin;
import dev.trade.tradeplugin.models.TradeSession;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.*;

/**
 * Layout 54 sloturi (6 randuri x 9 coloane):
 *
 * Col:  0    1    2    3   [4=sep]  5    6    7    8
 * R0: [MY] [MY] [MY] [MY] [SEP] [HIS][HIS][HIS] [ACC]
 * R1: [MY] [MY] [MY] [MY] [SEP] [HIS][HIS][HIS] [DEN]
 * R2: [MY] [MY] [MY] [MY] [SEP] [HIS][HIS][HIS] [   ]
 * R3: [MY] [MY] [MY] [MY] [SEP] [HIS][HIS][HIS] [   ]
 * R4: [MY] [MY] [MY] [MY] [SEP] [HIS][HIS][HIS] [STA]
 * R5: [MY] [MY] [MY] [MY] [SEP] [HIS][HIS][HIS] [   ]
 *
 * Zona mea:   sloturi 0,1,2,3, 9,10,11,12, 18,19,20,21, 27,28,29,30, 36,37,38,39, 45,46,47,48  (4 col x 6 rand = 24, dar max 20 iteme)
 * Separator:  sloturi 4,13,22,31,40,49
 * Zona lui:   sloturi 5,6,7, 14,15,16, 23,24,25, 32,33,34, 41,42,43, 50,51,52
 * Butoane:    8=Accept, 17=Deny, 26=liber, 35=liber, 44=Status, 53=liber
 */
public class TradeGUI implements Listener {

    private static final Map<UUID, Inventory> openInventories = new HashMap<>();

    // Zona mea - 4 coloane (0-3) x 6 randuri = 24 sloturi, primele 20 sunt active
    private static final int[] MY_SLOTS    = {0,1,2,3, 9,10,11,12, 18,19,20,21, 27,28,29,30, 36,37,38,39, 45,46,47,48};
    // Separator coloana 4
    private static final int[] SEP_SLOTS   = {4,13,22,31,40,49};
    // Zona lui - coloane 5,6,7 x 6 randuri = 18 sloturi, primele 20 nu exced, max 18
    private static final int[] HIS_SLOTS   = {5,6,7, 14,15,16, 23,24,25, 32,33,34, 41,42,43, 50,51,52};
    // Coloana dreapta (col 8)
    private static final int ACCEPT_SLOT  = 8;
    private static final int DENY_SLOT    = 17;
    private static final int STATUS_SLOT  = 44;

    // Sloturi in care jucatorul NU poate interactiona (tot ce nu e MY_SLOTS)
    private static final Set<Integer> MY_SLOT_SET = new HashSet<>();
    private static final Set<Integer> BLOCKED_GUI_SLOTS = new HashSet<>();

    static {
        for (int s : MY_SLOTS) MY_SLOT_SET.add(s);
        // Blocam: separator, zona lui, coloana butoane, slot 26,35,53
        for (int s : SEP_SLOTS) BLOCKED_GUI_SLOTS.add(s);
        for (int s : HIS_SLOTS) BLOCKED_GUI_SLOTS.add(s);
        BLOCKED_GUI_SLOTS.add(ACCEPT_SLOT);
        BLOCKED_GUI_SLOTS.add(DENY_SLOT);
        BLOCKED_GUI_SLOTS.add(STATUS_SLOT);
        BLOCKED_GUI_SLOTS.add(26);
        BLOCKED_GUI_SLOTS.add(35);
        BLOCKED_GUI_SLOTS.add(53);
    }

    public static void openFor(Player player, TradeSession session, TradePlugin plugin) {
        Inventory inv = buildInventory(player, session);
        openInventories.put(player.getUniqueId(), inv);
        player.openInventory(inv);
    }

    public static void refresh(TradeSession session, TradePlugin plugin) {
        Player sender   = session.getSender();
        Player receiver = session.getReceiver();

        Inventory sInv = openInventories.get(sender.getUniqueId());
        Inventory rInv = openInventories.get(receiver.getUniqueId());

        if (sInv != null) {
            // In inventarul sender-ului, zona HIS = itemele receiver-ului
            syncHisZone(sInv, session.getReceiverItems());
            updateButtons(sInv, sender, session);
            sender.updateInventory();
        }
        if (rInv != null) {
            // In inventarul receiver-ului, zona HIS = itemele sender-ului
            syncHisZone(rInv, session.getSenderItems());
            updateButtons(rInv, receiver, session);
            receiver.updateInventory();
        }
    }

    private static void syncHisZone(Inventory inv, List<ItemStack> items) {
        for (int i = 0; i < HIS_SLOTS.length; i++) {
            inv.setItem(HIS_SLOTS[i], (i < items.size()) ? items.get(i).clone() : null);
        }
    }

    // ─────────────────────────────────────────────
    //  Build inventar initial
    // ─────────────────────────────────────────────
    private static Inventory buildInventory(Player player, TradeSession session) {
        Player other = session.getOther(player);
        String title = color("&8⚖ &eTRADE &8| &7" + player.getName() + " &8↔ &a" + other.getName());
        Inventory inv = Bukkit.createInventory(null, 54, title);

        // Separator portocaliu
        ItemStack sep = makeItem(Material.ORANGE_STAINED_GLASS_PANE, "&6", Collections.emptyList());
        for (int s : SEP_SLOTS) inv.setItem(s, sep);

        // Fundal coloana dreapta
        ItemStack dark = makeItem(Material.BLACK_STAINED_GLASS_PANE, "&0", Collections.emptyList());
        inv.setItem(26, dark);
        inv.setItem(35, dark);
        inv.setItem(53, dark);

        // Butoane
        updateButtons(inv, player, session);

        // Zona mea - primele 4 sloturi din fiecare rand sunt libere pentru iteme
        // Zona lui - goala initial
        syncHisZone(inv, session.getItemsOfOther(player));

        return inv;
    }

    private static void updateButtons(Inventory inv, Player player, TradeSession session) {
        boolean iConfirmed = isConfirmed(player, session);
        Player other = session.getOther(player);
        boolean otherConfirmed = isConfirmed(other, session);

        // Buton Accept
        if (session.getState() == TradeSession.State.CONFIRMING) {
            inv.setItem(ACCEPT_SLOT, makeItem(Material.LIME_CONCRETE, "&a&l⌛ SE FINALIZEAZA...",
                    Arrays.asList(color("&7Trade-ul se completeaza!"), color("&eAsteapta 5 secunde..."))));
        } else if (iConfirmed) {
            inv.setItem(ACCEPT_SLOT, makeItem(Material.LIME_CONCRETE, "&a&l✔ CONFIRMAT",
                    Arrays.asList(color("&7Ai confirmat!"), color("&eAsteapta ca &a" + other.getName()), color("&esa confirme si el."))));
        } else {
            inv.setItem(ACCEPT_SLOT, makeItem(Material.LIME_WOOL, "&a&l✔ ACCEPT",
                    Arrays.asList(color("&7Confirma trade-ul."), color(""),
                            color(otherConfirmed ? "&a" + other.getName() + " a confirmat!" : "&7" + other.getName() + " nu a confirmat"),
                            color(""), color("&8Amandoi trebuie sa confirme,"), color("&8apoi se asteapta &a5s&8."))));
        }

        // Buton Deny/Cancel
        inv.setItem(DENY_SLOT, makeItem(Material.RED_WOOL, "&c&l✗ ANULEAZA",
                Arrays.asList(color("&7Anuleaza trade-ul."), color("&7Itemele se returneaza."))));

        // Status
        String s1, s2;
        if (session.getState() == TradeSession.State.CONFIRMING) {
            s1 = "&a⌛ Finalizeaza in 5s...";
            s2 = "";
        } else {
            s1 = (iConfirmed ? "&a✔ " : "&7✗ ") + "&fTu";
            s2 = (otherConfirmed ? "&a✔ " : "&7✗ ") + "&f" + other.getName();
        }
        inv.setItem(STATUS_SLOT, makeItem(Material.PAPER, "&e&lSTATUS",
                Arrays.asList(color(s1), color(s2), color(""), color("&7Max &e20 &7iteme fiecare."))));
    }

    private static boolean isConfirmed(Player player, TradeSession session) {
        if (player == null) return false;
        return player.getUniqueId().equals(session.getSenderUUID())
                ? session.isSenderConfirmed()
                : session.isReceiverConfirmed();
    }

    // ─────────────────────────────────────────────
    //  Events
    // ─────────────────────────────────────────────

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player)) return;
        Player player = (Player) event.getWhoClicked();
        TradePlugin plugin = TradePlugin.getInstance();
        TradeSession session = plugin.getTradeManager().getSession(player);
        if (session == null) return;

        Inventory topInv = openInventories.get(player.getUniqueId());
        if (topInv == null) return;

        // Verifica ca evenimentul e pentru inventarul nostru
        if (!event.getView().getTopInventory().equals(topInv)) return;

        int rawSlot = event.getRawSlot();
        int topSize = topInv.getSize(); // 54

        // ── Click in inventarul de jos al playerului (slot >= 54) ──
        if (rawSlot >= topSize) {
            // Permite click normal in inventarul propriu
            // DAR daca e shift-click, redirectionam itemul in zona MY_SLOTS
            if (event.isShiftClick()) {
                event.setCancelled(true);
                if (session.getState() == TradeSession.State.CONFIRMING) return;

                ItemStack clicked = event.getCurrentItem();
                if (clicked == null || clicked.getType() == Material.AIR) return;

                // Gasim primul slot liber din MY_SLOTS (primele 20)
                int freeSlot = findFreeMySlot(topInv);
                if (freeSlot == -1) {
                    player.sendMessage(color("&c&lTRADE &8» &cZona ta e plina! (max 20 iteme)"));
                    return;
                }

                // Verifica limita 20
                int currentCount = countMyItems(topInv);
                if (currentCount >= 20) {
                    player.sendMessage(color("&c&lTRADE &8» &cPoti pune maxim &e20 &7de iteme!"));
                    return;
                }

                topInv.setItem(freeSlot, clicked.clone());
                event.getCurrentItem().setAmount(0);
                syncSessionFromGUI(player, session, topInv, plugin);
            }
            // Click normal in inventarul propriu - permis fara restrictii
            return;
        }

        // ── Click in GUI (slot < 54) ──

        // Buton Accept
        if (rawSlot == ACCEPT_SLOT) {
            event.setCancelled(true);
            if (session.getState() == TradeSession.State.CONFIRMING) return;
            plugin.getTradeManager().confirmTrade(player);
            return;
        }

        // Buton Deny
        if (rawSlot == DENY_SLOT) {
            event.setCancelled(true);
            plugin.getTradeManager().cancelTradeForPlayer(player);
            return;
        }

        // Sloturi blocate (separator, zona lui, coloana dreapta)
        if (BLOCKED_GUI_SLOTS.contains(rawSlot)) {
            event.setCancelled(true);
            return;
        }

        // Zona mea - permite interactiunea
        if (MY_SLOT_SET.contains(rawSlot)) {
            if (session.getState() == TradeSession.State.CONFIRMING) {
                event.setCancelled(true);
                return;
            }

            // Verifica limita daca adauga un item
            // Lasam click-ul sa se execute si sincronizam dupa
            Bukkit.getScheduler().runTask(plugin, () -> {
                // Limita 20
                enforceLimit(player, session, topInv, plugin);
                syncSessionFromGUI(player, session, topInv, plugin);
            });
            return;
        }

        // Orice alt slot blocat
        event.setCancelled(true);
    }

    @EventHandler
    public void onInventoryDrag(InventoryDragEvent event) {
        if (!(event.getWhoClicked() instanceof Player)) return;
        Player player = (Player) event.getWhoClicked();
        TradePlugin plugin = TradePlugin.getInstance();
        TradeSession session = plugin.getTradeManager().getSession(player);
        if (session == null) return;

        Inventory topInv = openInventories.get(player.getUniqueId());
        if (topInv == null) return;
        if (!event.getView().getTopInventory().equals(topInv)) return;

        if (session.getState() == TradeSession.State.CONFIRMING) {
            event.setCancelled(true);
            return;
        }

        // Daca drag-ul atinge sloturi blocate din GUI, anulam
        for (int slot : event.getRawSlots()) {
            if (slot < 54 && BLOCKED_GUI_SLOTS.contains(slot)) {
                event.setCancelled(true);
                return;
            }
        }

        Bukkit.getScheduler().runTask(plugin, () -> {
            enforceLimit(player, session, topInv, plugin);
            syncSessionFromGUI(player, session, topInv, plugin);
        });
    }

    @EventHandler
    public void onInventoryClose(InventoryCloseEvent event) {
        if (!(event.getPlayer() instanceof Player)) return;
        Player player = (Player) event.getPlayer();
        TradePlugin plugin = TradePlugin.getInstance();
        TradeSession session = plugin.getTradeManager().getSession(player);
        if (session == null) return;

        Inventory topInv = openInventories.get(player.getUniqueId());
        if (topInv == null) return;
        if (!event.getInventory().equals(topInv)) return;

        openInventories.remove(player.getUniqueId());

        if (session.getState() != TradeSession.State.COMPLETED
                && session.getState() != TradeSession.State.CANCELLED) {
            plugin.getTradeManager().cancelTrade(session,
                    "&c" + player.getName() + " &7a inchis trade-ul.");
        }
    }

    // ─────────────────────────────────────────────
    //  Sincronizare GUI -> Session
    // ─────────────────────────────────────────────

    private void syncSessionFromGUI(Player player, TradeSession session, Inventory inv, TradePlugin plugin) {
        List<ItemStack> myItems = session.getItemsOf(player);
        myItems.clear();

        for (int i = 0; i < MY_SLOTS.length && i < 20; i++) {
            ItemStack item = inv.getItem(MY_SLOTS[i]);
            if (item != null && item.getType() != Material.AIR) {
                myItems.add(item.clone());
            }
        }

        plugin.getTradeManager().resetConfirmation(session);
        refresh(session, plugin);
    }

    private void enforceLimit(Player player, TradeSession session, Inventory inv, TradePlugin plugin) {
        int count = 0;
        for (int i = 0; i < MY_SLOTS.length; i++) {
            ItemStack item = inv.getItem(MY_SLOTS[i]);
            if (item != null && item.getType() != Material.AIR) {
                count++;
                if (count > 20) {
                    // Returneaza excesul
                    giveItem(player, item);
                    inv.setItem(MY_SLOTS[i], null);
                    player.sendMessage(color("&c&lTRADE &8» &cPoti pune maxim &e20 &7de iteme!"));
                }
            }
        }
    }

    private int findFreeMySlot(Inventory inv) {
        for (int i = 0; i < MY_SLOTS.length && i < 20; i++) {
            ItemStack item = inv.getItem(MY_SLOTS[i]);
            if (item == null || item.getType() == Material.AIR) return MY_SLOTS[i];
        }
        return -1;
    }

    private int countMyItems(Inventory inv) {
        int count = 0;
        for (int i = 0; i < MY_SLOTS.length && i < 20; i++) {
            ItemStack item = inv.getItem(MY_SLOTS[i]);
            if (item != null && item.getType() != Material.AIR) count++;
        }
        return count;
    }

    private void giveItem(Player player, ItemStack item) {
        Map<Integer, ItemStack> leftover = player.getInventory().addItem(item.clone());
        for (ItemStack drop : leftover.values()) {
            player.getWorld().dropItemNaturally(player.getLocation(), drop);
        }
    }

    public static void closeAll() {
        openInventories.clear();
    }

    // ─────────────────────────────────────────────
    //  Utilitare
    // ─────────────────────────────────────────────
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
