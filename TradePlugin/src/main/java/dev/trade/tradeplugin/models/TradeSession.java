package dev.trade.tradeplugin.models;

import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class TradeSession {

    public enum State {
        PENDING,    // cerere trimisa, asteptam accept
        ACTIVE,     // ambii jucatori au GUI-ul deschis
        CONFIRMING, // cel care primeste a apasat Accept, numara 5 secunde
        COMPLETED,
        CANCELLED
    }

    private final UUID senderUUID;
    private final UUID receiverUUID;
    private final Player sender;
    private final Player receiver;

    private final List<ItemStack> senderItems = new ArrayList<>();
    private final List<ItemStack> receiverItems = new ArrayList<>();

    private State state = State.PENDING;
    private boolean senderConfirmed = false;
    private boolean receiverConfirmed = false;

    public TradeSession(Player sender, Player receiver) {
        this.sender = sender;
        this.receiver = receiver;
        this.senderUUID = sender.getUniqueId();
        this.receiverUUID = receiver.getUniqueId();
    }

    public Player getSender() { return sender; }
    public Player getReceiver() { return receiver; }
    public UUID getSenderUUID() { return senderUUID; }
    public UUID getReceiverUUID() { return receiverUUID; }

    public List<ItemStack> getSenderItems() { return senderItems; }
    public List<ItemStack> getReceiverItems() { return receiverItems; }

    public State getState() { return state; }
    public void setState(State state) { this.state = state; }

    public boolean isSenderConfirmed() { return senderConfirmed; }
    public boolean isReceiverConfirmed() { return receiverConfirmed; }
    public void setSenderConfirmed(boolean v) { this.senderConfirmed = v; }
    public void setReceiverConfirmed(boolean v) { this.receiverConfirmed = v; }

    public boolean involves(UUID uuid) {
        return senderUUID.equals(uuid) || receiverUUID.equals(uuid);
    }

    public Player getOther(Player player) {
        return player.getUniqueId().equals(senderUUID) ? receiver : sender;
    }

    public List<ItemStack> getItemsOf(Player player) {
        return player.getUniqueId().equals(senderUUID) ? senderItems : receiverItems;
    }

    public List<ItemStack> getItemsOfOther(Player player) {
        return player.getUniqueId().equals(senderUUID) ? receiverItems : senderItems;
    }

    /** Adauga item in lista jucatorului (max 20) */
    public boolean addItem(Player player, ItemStack item) {
        List<ItemStack> list = getItemsOf(player);
        if (list.size() >= 20) return false;
        list.add(item.clone());
        return true;
    }
}
