package dev.serverinfo.plugin.gui;

import dev.serverinfo.plugin.ServerInfoPlugin;
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
import org.bukkit.inventory.meta.SkullMeta;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * Layout meniu principal (54 sloturi):
 *
 * R0: [=][=][=][=][=][=][=][=][=]   border top
 * R1: [=][SHOP][.][JOBS][.][CF][.][KIT][=]
 * R2: [=][DISC][.][INFO][.][REG][.][DONA][=]
 * R3: [=][TRADE][.][HEAD][.][ONLINE][.][STATS][=]
 * R4: [=][SOC][.][.][.][.][.][.][=]
 * R5: [=][=][=][=][X][=][=][=][=]   border bottom + close
 *
 * Slot-uri butoane:
 *  10 = /shop          11 = spacer  12 = /jobsmenu    13 = spacer  14 = /coinflip   15 = spacer  16 = /kit
 *  19 = Discord        20 = spacer  21 = Info Server  22 = spacer  23 = Reguli      24 = spacer  25 = Donatie
 *  28 = /trade         29 = spacer  30 = Head player  31 = spacer  32 = Online      33 = spacer  34 = Server Stats
 *  37 = Social Media   38..43 = spacer
 *  49 = Inchide
 */
public class ServerInfoGUI implements Listener {

    private final ServerInfoPlugin plugin;

    public ServerInfoGUI(ServerInfoPlugin plugin) {
        this.plugin = plugin;
    }

    // ─────────────────────────────────────────────
    //  Meniu principal
    // ─────────────────────────────────────────────
    public void openMain(Player player) {
        String title = color(plugin.getConfig().getString("menu-title", "&8[ &6✦ &eINFO SERVER &6✦ &8]"));
        Inventory inv = Bukkit.createInventory(null, 54, title);

        // Border
        ItemStack border = makeItem(Material.BLACK_STAINED_GLASS_PANE, "&0", Collections.emptyList());
        for (int i = 0; i < 9; i++) inv.setItem(i, border);
        for (int i = 45; i < 54; i++) inv.setItem(i, border);
        for (int r = 1; r <= 4; r++) { inv.setItem(r * 9, border); inv.setItem(r * 9 + 8, border); }

        // Spacer gri inchis
        ItemStack spacer = makeItem(Material.GRAY_STAINED_GLASS_PANE, "&0", Collections.emptyList());
        for (int s : new int[]{11,13,15,20,22,24,29,31,33,38,39,40,41,42,43,37}) inv.setItem(s, spacer);

        // ── RAND 1: Comenzi principale ──

        // /shop
        inv.setItem(10, makeItem(Material.EMERALD, "&a&l🛒 SHOP",
                Arrays.asList(
                        color("&8▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬"),
                        color("  &7Cumpara si vinde"),
                        color("  &7itemele tale!"),
                        color("&8▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬"),
                        color("  &a» &fClick pentru &e/shop")
                )));

        // /jobsmenu
        inv.setItem(12, makeItem(Material.IRON_PICKAXE, "&e&l⛏ JOBS",
                Arrays.asList(
                        color("&8▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬"),
                        color("  &7Alege-ti jobul si"),
                        color("  &7castiga bani!"),
                        color("&8▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬"),
                        color("  &a» &fClick pentru &e/jobsmenu")
                )));

        // /coinflip
        inv.setItem(14, makeItem(Material.GOLD_NUGGET, "&6&l🪙 COINFLIP",
                Arrays.asList(
                        color("&8▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬"),
                        color("  &7Pariaza bani pe"),
                        color("  &7cap sau pajura!"),
                        color("&8▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬"),
                        color("  &a» &fClick pentru &e/coinflip")
                )));

        // /kit
        inv.setItem(16, makeItem(Material.CHEST, "&d&l🎁 KITURI",
                Arrays.asList(
                        color("&8▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬"),
                        color("  &7Ia kiturile"),
                        color("  &7disponibile!"),
                        color("&8▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬"),
                        color("  &a» &fClick pentru &e/kit")
                )));

        // ── RAND 2: Info & social ──

        // Discord
        String discordLink = plugin.getConfig().getString("discord.invite", "");
        List<String> discordLore = new ArrayList<>();
        discordLore.add(color("&8▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬"));
        if (!discordLink.isEmpty()) discordLore.add(color("  &7Link: &b" + discordLink));
        discordLore.add(color("  &7Alatura-te comunitatii!"));
        discordLore.add(color("&8▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬"));
        discordLore.add(color("  &a» &fClick pentru link Discord"));
        inv.setItem(19, makeItem(Material.CYAN_DYE, "&b&l💬 DISCORD", discordLore));

        // Info Server
        List<String> serverLore = new ArrayList<>();
        serverLore.add(color("&8▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬"));
        serverLore.add(color("  &eIP: &f" + plugin.getConfig().getString("server.ip", "play.server.ro")));
        serverLore.add(color("  &eVersiune: &f" + plugin.getConfig().getString("server.version", "1.20.1")));
        serverLore.add(color("  &eTPS: &a" + getTPS()));
        serverLore.add(color("  &eOnline: &a" + Bukkit.getOnlinePlayers().size() + "&8/&a" + Bukkit.getMaxPlayers()));
        serverLore.add(color("&8▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬"));
        for (String line : plugin.getConfig().getStringList("server.description")) {
            serverLore.add(color("  " + line));
        }
        inv.setItem(21, makeItem(Material.GRASS_BLOCK, "&a&l🌐 INFO SERVER", serverLore));

        // Reguli
        List<String> rulesLore = new ArrayList<>();
        rulesLore.add(color("&8▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬"));
        for (String rule : plugin.getConfig().getStringList("rules")) {
            rulesLore.add(color("  " + rule));
        }
        rulesLore.add(color("&8▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬"));
        rulesLore.add(color("  &c&lNerespectarea = BAN!"));
        rulesLore.add(color("  &a» &fClick pentru a citi regulile"));
        inv.setItem(23, makeItem(Material.WRITABLE_BOOK, "&c&l📋 REGULI", rulesLore));

        // Donatie
        List<String> donateLore = new ArrayList<>();
        donateLore.add(color("&8▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬"));
        donateLore.add(color("  &7Rank: &6&l" + plugin.getConfig().getString("donation.rank-name", "Donator")));
        donateLore.add(color("  &7Pret: &a&l" + plugin.getConfig().getString("donation.price", "5 Euro")));
        donateLore.add(color("&8▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬"));
        for (String b : plugin.getConfig().getStringList("donation.benefits")) {
            donateLore.add(color("  " + b));
        }
        donateLore.add(color("&8▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬"));
        donateLore.add(color("  &a» &fClick pentru detalii donatie"));
        inv.setItem(25, makeItem(Material.GOLD_INGOT, "&6&l💰 DONATIE & RANK", donateLore));

        // ── RAND 3: Jucator & stat ──

        // /trade
        inv.setItem(28, makeItem(Material.DISPENSER, "&5&l⚖ TRADE",
                Arrays.asList(
                        color("&8▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬"),
                        color("  &7Schimba iteme cu"),
                        color("  &7alti jucatori!"),
                        color("&8▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬"),
                        color("  &a» &fClick pentru &e/trade")
                )));

        // Head jucator
        ItemStack skull = new ItemStack(Material.PLAYER_HEAD);
        SkullMeta skullMeta = (SkullMeta) skull.getItemMeta();
        if (skullMeta != null) {
            skullMeta.setOwningPlayer(player);
            skullMeta.setDisplayName(color("&e&l❤ " + player.getName()));
            List<String> skullLore = new ArrayList<>();
            skullLore.add(color("&8▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬"));
            skullLore.add(color("  &7Bine ai venit,"));
            skullLore.add(color("  &e" + player.getName() + "&7!"));
            skullLore.add(color("&8▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬"));
            skullLore.add(color("  &7Rank: &f" + getPrimaryGroup(player)));
            skullLore.add(color("  &7Ping: &a" + player.getPing() + "ms"));
            skullMeta.setLore(skullLore);
            skull.setItemMeta(skullMeta);
        }
        inv.setItem(30, skull);

        // Online players
        int online = Bukkit.getOnlinePlayers().size();
        int max = Bukkit.getMaxPlayers();
        List<String> onlineLore = new ArrayList<>();
        onlineLore.add(color("&8▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬"));
        onlineLore.add(color("  &7Online: &a" + online + "&8/&a" + max));
        onlineLore.add(color("&8▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬"));
        int cnt = 0;
        for (Player p : Bukkit.getOnlinePlayers()) {
            if (cnt >= 8) { onlineLore.add(color("  &8... si altii")); break; }
            onlineLore.add(color("  &7▸ &f" + p.getName()));
            cnt++;
        }
        inv.setItem(32, makeItem(Material.EMERALD,
                "&a&l👥 JUCATORI &8[&a" + online + "&8/&a" + max + "&8]", onlineLore));

        // Server stats
        inv.setItem(34, makeItem(Material.COMPARATOR, "&e&l⚙ STATS SERVER",
                Arrays.asList(
                        color("&8▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬"),
                        color("  &7Versiune: &f" + plugin.getConfig().getString("server.version", "1.20.1")),
                        color("  &7TPS: &a" + getTPS()),
                        color("  &7Online: &a" + online + "&8/&a" + max),
                        color("&8▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬")
                )));

        // ── RAND 4: Social ──
        List<String> socialLore = new ArrayList<>();
        socialLore.add(color("&8▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬"));
        socialLore.add(color("  &5TikTok: &f" + plugin.getConfig().getString("socials.tiktok", "N/A")));
        socialLore.add(color("  &cYouTube: &f" + plugin.getConfig().getString("socials.youtube", "N/A")));
        socialLore.add(color("  &dInstagram: &f" + plugin.getConfig().getString("socials.instagram", "N/A")));
        socialLore.add(color("&8▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬"));
        inv.setItem(37, makeItem(Material.MAGENTA_DYE, "&d&l★ SOCIAL MEDIA", socialLore));

        // Buton inchide
        inv.setItem(49, makeItem(Material.BARRIER, "&c&l✗ INCHIDE",
                Collections.singletonList(color("&7Click pentru a inchide."))));

        player.openInventory(inv);
        player.playSound(player.getLocation(), org.bukkit.Sound.BLOCK_CHEST_OPEN, 0.5f, 1.2f);
    }

    // ─────────────────────────────────────────────
    //  Submeniu Donatie
    // ─────────────────────────────────────────────
    public void openDonateMenu(Player player) {
        String title = color("&8[ &6✦ &6DONATIE &6✦ &8]");
        Inventory inv = Bukkit.createInventory(null, 45, title);

        ItemStack border = makeItem(Material.ORANGE_STAINED_GLASS_PANE, "&6", Collections.emptyList());
        for (int i = 0; i < 9; i++) inv.setItem(i, border);
        for (int i = 36; i < 45; i++) inv.setItem(i, border);
        for (int r = 1; r <= 3; r++) { inv.setItem(r * 9, border); inv.setItem(r * 9 + 8, border); }

        ItemStack bg = makeItem(Material.BLACK_STAINED_GLASS_PANE, "&0", Collections.emptyList());
        for (int s : new int[]{10,11,12,13,14,15,16,19,20,21,22,23,24,25,28,29,30,31,32,33,34}) {
            inv.setItem(s, bg);
        }

        // Nether Star centru
        List<String> rankLore = new ArrayList<>();
        rankLore.add(color("&8▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬"));
        rankLore.add(color("  &6&lRANK DONATOR"));
        rankLore.add(color("  &7Pret: &a&l" + plugin.getConfig().getString("donation.price", "5 Euro")));
        rankLore.add(color("&8▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬"));
        rankLore.add(color("  &e&lCe primesti:"));
        for (String b : plugin.getConfig().getStringList("donation.benefits")) {
            rankLore.add(color("  " + b));
        }
        rankLore.add(color("&8▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬"));
        rankLore.add(color("  &7Dupa plata deschide ticket pe Discord!"));
        String site = plugin.getConfig().getString("donation.site", "");
        if (!site.isEmpty()) rankLore.add(color("  &eSite: &f" + site));
        inv.setItem(13, makeItem(Material.NETHER_STAR, "&6&l✦ RANK DONATOR", rankLore));

        inv.setItem(11, makeItem(Material.GOLD_BLOCK, "&6&lBENEFICII",
                Arrays.asList(color("&8▬▬▬▬▬▬▬▬▬"),
                        color("  &a✔ &7/kit donator"),
                        color("  &a✔ &75,000$ start"),
                        color("  &a✔ &7Prefix colorat"),
                        color("&8▬▬▬▬▬▬▬▬▬"))));

        inv.setItem(15, makeItem(Material.DIAMOND, "&b&lEXCLUSIV",
                Arrays.asList(color("&8▬▬▬▬▬▬▬▬▬"),
                        color("  &b✦ &7/fly in spawn"),
                        color("  &b✦ &7Chat colorat"),
                        color("  &b✦ &7Tag Discord"),
                        color("&8▬▬▬▬▬▬▬▬▬"))));

        // Buton inapoi
        inv.setItem(40, makeItem(Material.ARROW, "&c&l« INAPOI",
                Collections.singletonList(color("&7Inapoi la meniul principal."))));

        player.openInventory(inv);
        player.playSound(player.getLocation(), org.bukkit.Sound.BLOCK_NOTE_BLOCK_BELL, 0.8f, 1f);
    }

    // ─────────────────────────────────────────────
    //  Event Listener
    // ─────────────────────────────────────────────
    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player)) return;
        Player player = (Player) event.getWhoClicked();
        String title = event.getView().getTitle();

        boolean isMain   = title.contains("INFO SERVER");
        boolean isDonate = title.contains("DONATIE");
        if (!isMain && !isDonate) return;

        event.setCancelled(true);
        ItemStack clicked = event.getCurrentItem();
        if (clicked == null || clicked.getType() == Material.AIR) return;

        int slot = event.getRawSlot();

        if (isMain) {
            handleMainClick(player, slot);
        } else {
            handleDonateClick(player, slot);
        }
    }

    private void handleMainClick(Player player, int slot) {
        switch (slot) {
            // Rand 1 - comenzi
            case 10: runCmd(player, "shop"); break;
            case 12: runCmd(player, "jobsmenu"); break;
            case 14: runCmd(player, "coinflip"); break;
            case 16: runCmd(player, "kit"); break;

            // Rand 2 - info
            case 19: // Discord
                String invite = plugin.getConfig().getString("discord.invite", "");
                player.closeInventory();
                player.sendMessage("");
                player.sendMessage(color("&8[&b✦&8] &b&lDISCORD SERVER"));
                if (!invite.isEmpty()) {
                    player.sendMessage(color("  &7Link: &b&n" + invite));
                } else {
                    player.sendMessage(color("  &7Link-ul Discord nu a fost setat inca."));
                }
                player.sendMessage("");
                player.playSound(player.getLocation(), org.bukkit.Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 0.8f, 1.2f);
                break;
            case 21: // Info server - afiseaza IP
                player.sendMessage(color("&8[&a✦&8] &eIP Server: &f" + plugin.getConfig().getString("server.ip", "play.server.ro")));
                player.playSound(player.getLocation(), org.bukkit.Sound.UI_BUTTON_CLICK, 0.5f, 1f);
                break;
            case 23: // Reguli
                player.closeInventory();
                player.sendMessage("");
                player.sendMessage(color("&8&m━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"));
                player.sendMessage(color("           &c&lREGULI SERVER"));
                player.sendMessage(color("&8&m━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"));
                for (String rule : plugin.getConfig().getStringList("rules")) {
                    player.sendMessage(color("  " + rule));
                }
                player.sendMessage(color("&8&m━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"));
                player.sendMessage("");
                player.playSound(player.getLocation(), org.bukkit.Sound.ITEM_BOOK_PAGE_TURN, 0.8f, 1f);
                break;
            case 25: // Donatie
                player.closeInventory();
                Bukkit.getScheduler().runTaskLater(plugin, () -> openDonateMenu(player), 2L);
                break;

            // Rand 3
            case 28: runCmd(player, "trade"); break;
            case 49: // Inchide
                player.closeInventory();
                player.playSound(player.getLocation(), org.bukkit.Sound.BLOCK_CHEST_CLOSE, 0.5f, 1.2f);
                break;
            default:
                player.playSound(player.getLocation(), org.bukkit.Sound.UI_BUTTON_CLICK, 0.3f, 0.8f);
                break;
        }
    }

    private void handleDonateClick(Player player, int slot) {
        if (slot == 40) {
            player.closeInventory();
            Bukkit.getScheduler().runTaskLater(plugin, () -> openMain(player), 2L);
        } else if (slot == 13 || slot == 11 || slot == 15) {
            String site = plugin.getConfig().getString("donation.site", "");
            player.sendMessage("");
            player.sendMessage(color("&8[&6✦&8] &6&lDONATIE RANK DONATOR"));
            player.sendMessage(color("  &7Pret: &a&l" + plugin.getConfig().getString("donation.price", "5 Euro")));
            if (!site.isEmpty()) player.sendMessage(color("  &7Site: &e" + site));
            player.sendMessage(color("  &7Dupa plata deschide un ticket pe Discord!"));
            player.sendMessage("");
            player.playSound(player.getLocation(), org.bukkit.Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1f, 1.5f);
        }
    }

    // ─────────────────────────────────────────────
    //  Helper: executa comanda ca player
    // ─────────────────────────────────────────────
    private void runCmd(Player player, String command) {
        player.closeInventory();
        Bukkit.getScheduler().runTaskLater(plugin, () ->
                Bukkit.dispatchCommand(player, command), 2L);
    }

    // ─────────────────────────────────────────────
    //  Utilitare
    // ─────────────────────────────────────────────
    private String getPrimaryGroup(Player player) {
        try {
            var reg = Bukkit.getServicesManager().getRegistration(net.milkbowl.vault.permission.Permission.class);
            if (reg != null) return reg.getProvider().getPrimaryGroup(player);
        } catch (Throwable ignored) {}
        return "Jucator";
    }

    private String getTPS() {
        try {
            Object server = Bukkit.getServer().getClass().getMethod("getServer").invoke(Bukkit.getServer());
            double[] tps = (double[]) server.getClass().getField("recentTps").get(server);
            return String.format("%.1f", Math.min(tps[0], 20.0));
        } catch (Throwable e) {
            return "20.0";
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
