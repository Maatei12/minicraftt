package dev.trade.tradeplugin.managers;

import dev.trade.tradeplugin.TradePlugin;
import dev.trade.tradeplugin.gui.TradeGUI;
import dev.trade.tradeplugin.models.TradeSession;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitTask;

import java.util.*;

public class TradeManager {

    private final TradePlugin plugin;

    // UUID -> sesiunea activa
    private final Map<UUID, TradeSession> activeSessions = new HashMap<>();

    // UUID trimitator -> UUID destinatar (cereri in asteptare)
    private final Map<UUID, UUID> pendingRequests = new HashMap<>();

    // task-uri de timeout pentru cereri (60 secunde expira)
    private final Map<UUID, BukkitTask> requestTimeouts = new HashMap<>();

    // task countdown pentru confirmare (5 secunde)
    private final Map<UUID, BukkitTask> confirmCountdowns = new HashMap<>();

    public TradeManager(TradePlugin plugin) {
        this.plugin = plugin;
    }

    // ─────────────────────────────────────────────
    //  Trimitere cerere de trade
    // ─────────────────────────────────────────────
    public void sendTradeRequest(Player sender, Player target) {
        if (isInTrade(sender)) {
            sender.sendMessage(colorize("&c&lTRADE &8» &cEsti deja intr-un trade!"));
            return;
        }
        if (isInTrade(target)) {
            sender.sendMessage(colorize("&c&lTRADE &8» &c" + target.getName() + " este deja intr-un trade!"));
            return;
        }
        if (hasPendingRequest(sender.getUniqueId())) {
            sender.sendMessage(colorize("&c&lTRADE &8» &cAi deja o cerere de trade in asteptare!"));
            return;
        }

        pendingRequests.put(sender.getUniqueId(), target.getUniqueId());

        // Mesaj sender
        sender.sendMessage(colorize("&a&lTRADE &8» &7Cerere de trade trimisa catre &a" + target.getName() + "&7."));
        sender.playSound(sender.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1f, 1.2f);

        // Mesaj receiver - doar lui in chat
        target.sendMessage("");
        target.sendMessage(colorize("&6&l✦ CERERE DE TRADE ✦"));
        target.sendMessage(colorize("  &7Jucatorul &a" + sender.getName() + " &7iti propune un trade."));
        target.sendMessage(colorize("  &a▶ &f/trade accept &7pentru a accepta"));
        target.sendMessage(colorize("  &c▶ &f/trade deny &7pentru a refuza"));
        target.sendMessage(colorize("  &8(Cererea expira in &760 &8de secunde)"));
        target.sendMessage("");
        target.playSound(target.getLocation(), Sound.BLOCK_NOTE_BLOCK_BELL, 1f, 1f);

        // ActionBar receiver
        sendActionBar(target, "&6✦ &e" + sender.getName() + " &6iti propune un trade! &7| &a/trade accept &7/ &c/trade deny");

        // Timeout 60 secunde
        BukkitTask task = Bukkit.getScheduler().runTaskLater(plugin, () -> {
            if (pendingRequests.containsKey(sender.getUniqueId())) {
                pendingRequests.remove(sender.getUniqueId());
                requestTimeouts.remove(sender.getUniqueId());
                sender.sendMessage(colorize("&c&lTRADE &8» &cCererea ta de trade catre &e" + target.getName() + " &ca expirat."));
                target.sendMessage(colorize("&c&lTRADE &8» &cCererea de trade de la &e" + sender.getName() + " &ca expirat."));
            }
        }, 20L * 60);

        requestTimeouts.put(sender.getUniqueId(), task);
    }

    // ─────────────────────────────────────────────
    //  Acceptare cerere
    // ─────────────────────────────────────────────
    public void acceptTrade(Player receiver) {
        UUID senderUUID = getPendingRequestFor(receiver.getUniqueId());
        if (senderUUID == null) {
            receiver.sendMessage(colorize("&c&lTRADE &8» &cNu ai nicio cerere de trade in asteptare."));
            return;
        }

        Player sender = Bukkit.getPlayer(senderUUID);
        if (sender == null || !sender.isOnline()) {
            receiver.sendMessage(colorize("&c&lTRADE &8» &cJucatorul care ti-a trimis cererea nu mai este online."));
            pendingRequests.remove(senderUUID);
            cancelTimeout(senderUUID);
            return;
        }

        // Anuleaza timeout
        cancelTimeout(senderUUID);
        pendingRequests.remove(senderUUID);

        // Creeaza sesiunea
        TradeSession session = new TradeSession(sender, receiver);
        session.setState(TradeSession.State.ACTIVE);
        activeSessions.put(sender.getUniqueId(), session);
        activeSessions.put(receiver.getUniqueId(), session);

        sender.sendMessage(colorize("&a&lTRADE &8» &a" + receiver.getName() + " &7a acceptat trade-ul!"));
        receiver.sendMessage(colorize("&a&lTRADE &8» &7Ai acceptat trade-ul cu &a" + sender.getName() + "&7."));

        sender.playSound(sender.getLocation(), Sound.BLOCK_CHEST_OPEN, 1f, 1.2f);
        receiver.playSound(receiver.getLocation(), Sound.BLOCK_CHEST_OPEN, 1f, 1.2f);

        // Deschide GUI pentru ambii
        Bukkit.getScheduler().runTask(plugin, () -> {
            TradeGUI.openFor(sender, session, plugin);
            TradeGUI.openFor(receiver, session, plugin);
        });
    }

    // ─────────────────────────────────────────────
    //  Refuzare cerere
    // ─────────────────────────────────────────────
    public void denyTrade(Player receiver) {
        UUID senderUUID = getPendingRequestFor(receiver.getUniqueId());
        if (senderUUID == null) {
            receiver.sendMessage(colorize("&c&lTRADE &8» &cNu ai nicio cerere de trade in asteptare."));
            return;
        }

        Player sender = Bukkit.getPlayer(senderUUID);

        cancelTimeout(senderUUID);
        pendingRequests.remove(senderUUID);

        receiver.sendMessage(colorize("&c&lTRADE &8» &7Ai refuzat cererea de trade."));
        if (sender != null && sender.isOnline()) {
            sender.sendMessage(colorize("&c&lTRADE &8» &c" + receiver.getName() + " &7a refuzat cererea ta de trade."));
            sender.playSound(sender.getLocation(), Sound.ENTITY_VILLAGER_NO, 1f, 1f);
        }
        receiver.playSound(receiver.getLocation(), Sound.ENTITY_VILLAGER_NO, 1f, 1f);
    }

    // ─────────────────────────────────────────────
    //  Confirmare trade (buton Accept din GUI)
    // ─────────────────────────────────────────────
    public void confirmTrade(Player player) {
        TradeSession session = getSession(player);
        if (session == null || session.getState() != TradeSession.State.ACTIVE) return;

        Player other = session.getOther(player);
        boolean isSender = player.getUniqueId().equals(session.getSenderUUID());

        if (isSender) {
            session.setSenderConfirmed(true);
        } else {
            session.setReceiverConfirmed(true);
        }

        // Actualizeaza GUI
        TradeGUI.refresh(session, plugin);

        if (session.isSenderConfirmed() && session.isReceiverConfirmed()) {
            startCountdown(session);
        } else {
            player.sendMessage(colorize("&a&lTRADE &8» &7Ai confirmat trade-ul. Asteapta ca &a" + other.getName() + " &7sa confirme si el."));
            sendActionBar(other, "&6✦ &e" + player.getName() + " &6a confirmat trade-ul! &7Apasa &aAccept &7si tu!");
        }
    }

    // ─────────────────────────────────────────────
    //  Countdown 5 secunde dupa ce ambii confirma
    // ─────────────────────────────────────────────
    private void startCountdown(TradeSession session) {
        session.setState(TradeSession.State.CONFIRMING);
        Player sender = session.getSender();
        Player receiver = session.getReceiver();

        sender.sendMessage(colorize("&a&lTRADE &8» &7Ambii jucatori au confirmat! Trade-ul se finalizeaza in &a5 &7secunde..."));
        receiver.sendMessage(colorize("&a&lTRADE &8» &7Ambii jucatori au confirmat! Trade-ul se finalizeaza in &a5 &7secunde..."));

        final int[] countdown = {5};

        BukkitTask task = Bukkit.getScheduler().runTaskTimer(plugin, () -> {
            if (!sender.isOnline() || !receiver.isOnline()) {
                cancelTrade(session, "&cUn jucator a iesit din joc. Trade anulat.");
                return;
            }
            if (countdown[0] <= 0) {
                completeTrade(session);
                return;
            }
            sendActionBar(sender, "&a✦ Trade se finalizeaza in &e" + countdown[0] + " &asecunde...");
            sendActionBar(receiver, "&a✦ Trade se finalizeaza in &e" + countdown[0] + " &asecunde...");
            sender.playSound(sender.getLocation(), Sound.BLOCK_NOTE_BLOCK_HAT, 0.7f, 1f + (5 - countdown[0]) * 0.1f);
            receiver.playSound(receiver.getLocation(), Sound.BLOCK_NOTE_BLOCK_HAT, 0.7f, 1f + (5 - countdown[0]) * 0.1f);
            countdown[0]--;
        }, 0L, 20L);

        confirmCountdowns.put(sender.getUniqueId(), task);
        confirmCountdowns.put(receiver.getUniqueId(), task);
    }

    // ─────────────────────────────────────────────
    //  Finalizare trade - schimb de iteme
    // ─────────────────────────────────────────────
    private void completeTrade(TradeSession session) {
        cancelCountdown(session.getSenderUUID());

        session.setState(TradeSession.State.COMPLETED);
        Player sender = session.getSender();
        Player receiver = session.getReceiver();

        // Inchide GUI-urile
        if (sender.isOnline()) sender.closeInventory();
        if (receiver.isOnline()) receiver.closeInventory();

        // Da itemele celuilalt
        for (ItemStack item : session.getSenderItems()) {
            if (item != null) giveItem(receiver, item);
        }
        for (ItemStack item : session.getReceiverItems()) {
            if (item != null) giveItem(sender, item);
        }

        activeSessions.remove(sender.getUniqueId());
        activeSessions.remove(receiver.getUniqueId());

        // Mesaje finale
        String senderList = formatItemList(session.getSenderItems());
        String receiverList = formatItemList(session.getReceiverItems());

        sender.sendMessage("");
        sender.sendMessage(colorize("&a&l✦ TRADE FINALIZAT ✦"));
        sender.sendMessage(colorize("  &7Ai primit de la &a" + receiver.getName() + "&7: &f" + receiverList));
        sender.sendMessage(colorize("  &7Ai dat catre &a" + receiver.getName() + "&7: &f" + senderList));
        sender.sendMessage("");

        receiver.sendMessage("");
        receiver.sendMessage(colorize("&a&l✦ TRADE FINALIZAT ✦"));
        receiver.sendMessage(colorize("  &7Ai primit de la &a" + sender.getName() + "&7: &f" + senderList));
        receiver.sendMessage(colorize("  &7Ai dat catre &a" + sender.getName() + "&7: &f" + receiverList));
        receiver.sendMessage("");

        sender.playSound(sender.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1f, 1.2f);
        receiver.playSound(receiver.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1f, 1.2f);
    }

    // ─────────────────────────────────────────────
    //  Anulare trade
    // ─────────────────────────────────────────────
    public void cancelTrade(TradeSession session, String reason) {
        if (session.getState() == TradeSession.State.CANCELLED || session.getState() == TradeSession.State.COMPLETED) return;
        session.setState(TradeSession.State.CANCELLED);

        cancelCountdown(session.getSenderUUID());

        Player sender = session.getSender();
        Player receiver = session.getReceiver();

        // Returneaza itemele
        for (ItemStack item : session.getSenderItems()) {
            if (item != null && sender.isOnline()) giveItem(sender, item);
        }
        for (ItemStack item : session.getReceiverItems()) {
            if (item != null && receiver.isOnline()) giveItem(receiver, item);
        }

        activeSessions.remove(sender.getUniqueId());
        activeSessions.remove(receiver.getUniqueId());

        if (sender.isOnline()) {
            sender.closeInventory();
            sender.sendMessage(colorize("&c&lTRADE &8» " + reason));
            sender.playSound(sender.getLocation(), Sound.ENTITY_VILLAGER_NO, 1f, 1f);
        }
        if (receiver.isOnline()) {
            receiver.closeInventory();
            receiver.sendMessage(colorize("&c&lTRADE &8» " + reason));
            receiver.playSound(receiver.getLocation(), Sound.ENTITY_VILLAGER_NO, 1f, 1f);
        }
    }

    public void cancelTradeForPlayer(Player player) {
        TradeSession session = getSession(player);
        if (session != null) {
            cancelTrade(session, "&c" + player.getName() + " &7a anulat trade-ul.");
        }
    }

    public void cancelAllTrades() {
        Set<TradeSession> done = new HashSet<>();
        for (TradeSession session : activeSessions.values()) {
            if (done.contains(session)) continue;
            done.add(session);
            cancelTrade(session, "&cServer-ul s-a oprit. Trade anulat.");
        }
        for (BukkitTask t : requestTimeouts.values()) t.cancel();
        requestTimeouts.clear();
        pendingRequests.clear();
    }

    // ─────────────────────────────────────────────
    //  Reset confirmare daca cineva mai adauga un item
    // ─────────────────────────────────────────────
    public void resetConfirmation(TradeSession session) {
        if (session.getState() != TradeSession.State.ACTIVE) return;
        boolean wasConfirmed = session.isSenderConfirmed() || session.isReceiverConfirmed();
        session.setSenderConfirmed(false);
        session.setReceiverConfirmed(false);
        if (wasConfirmed) {
            session.getSender().sendMessage(colorize("&e&lTRADE &8» &eOferele s-au schimbat. Confirma din nou!"));
            session.getReceiver().sendMessage(colorize("&e&lTRADE &8» &eOferele s-au schimbat. Confirma din nou!"));
        }
        TradeGUI.refresh(session, plugin);
    }

    // ─────────────────────────────────────────────
    //  Utilitare
    // ─────────────────────────────────────────────
    private void giveItem(Player player, ItemStack item) {
        Map<Integer, ItemStack> leftover = player.getInventory().addItem(item.clone());
        for (ItemStack drop : leftover.values()) {
            player.getWorld().dropItemNaturally(player.getLocation(), drop);
        }
    }

    private String formatItemList(List<ItemStack> items) {
        if (items.isEmpty()) return "Nimic";
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < items.size(); i++) {
            ItemStack item = items.get(i);
            if (item == null) continue;
            String name = item.hasItemMeta() && item.getItemMeta().hasDisplayName()
                    ? item.getItemMeta().getDisplayName()
                    : formatMaterial(item.getType().name());
            sb.append(item.getAmount()).append("x ").append(name);
            if (i < items.size() - 1) sb.append(", ");
        }
        return sb.toString();
    }

    private String formatMaterial(String name) {
        String[] parts = name.toLowerCase().split("_");
        StringBuilder sb = new StringBuilder();
        for (String p : parts) {
            sb.append(Character.toUpperCase(p.charAt(0))).append(p.substring(1)).append(" ");
        }
        return sb.toString().trim();
    }

    private UUID getPendingRequestFor(UUID receiverUUID) {
        for (Map.Entry<UUID, UUID> entry : pendingRequests.entrySet()) {
            if (entry.getValue().equals(receiverUUID)) return entry.getKey();
        }
        return null;
    }

    private boolean hasPendingRequest(UUID senderUUID) {
        return pendingRequests.containsKey(senderUUID);
    }

    public TradeSession getSession(Player player) {
        return activeSessions.get(player.getUniqueId());
    }

    public boolean isInTrade(Player player) {
        return activeSessions.containsKey(player.getUniqueId());
    }

    private void cancelTimeout(UUID uuid) {
        BukkitTask t = requestTimeouts.remove(uuid);
        if (t != null) t.cancel();
    }

    private void cancelCountdown(UUID uuid) {
        BukkitTask t = confirmCountdowns.remove(uuid);
        if (t != null) t.cancel();
    }

    private void sendActionBar(Player player, String msg) {
        player.spigot().sendMessage(ChatMessageType.ACTION_BAR, new TextComponent(ChatColor.translateAlternateColorCodes('&', msg)));
    }

    private String colorize(String s) {
        return ChatColor.translateAlternateColorCodes('&', s);
    }
}
