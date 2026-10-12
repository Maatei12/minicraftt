package dev.donutspawners.plugin.data;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Reprezinta un spawner plasat in lume.
 * Retine: locatia, tipul, proprietarul, stocul acumulat si timpul ultimului tick.
 */
public class SpawnerData {

    private final String locationKey;   // "world,x,y,z"
    private final String spawnerType;   // ex: "SKELETON"
    private final UUID ownerUUID;
    private long lastTick;              // epoch ms al ultimului ciclu de generare
    private int stackSize;              // cate spawnere sunt stacked (shift+rightclick)

    // Stocul acumulat: Material -> cantitate
    private final Map<Material, Integer> stock = new HashMap<>();

    public SpawnerData(String locationKey, String spawnerType, UUID ownerUUID) {
        this.locationKey = locationKey;
        this.spawnerType = spawnerType;
        this.ownerUUID = ownerUUID;
        this.lastTick = System.currentTimeMillis();
        this.stackSize = 1;
    }

    // ─────────────────────────────────────────────
    //  Stoc
    // ─────────────────────────────────────────────
    public void addToStock(Material mat, int amount) {
        stock.merge(mat, amount, Integer::sum);
    }

    public int getStock(Material mat) {
        return stock.getOrDefault(mat, 0);
    }

    public Map<Material, Integer> getStock() {
        return stock;
    }

    public void clearStock() {
        stock.clear();
    }

    public void removeFromStock(Material mat, int amount) {
        int current = stock.getOrDefault(mat, 0);
        int newAmount = current - amount;
        if (newAmount <= 0) stock.remove(mat);
        else stock.put(mat, newAmount);
    }

    public boolean isEmpty() {
        return stock.isEmpty() || stock.values().stream().allMatch(v -> v <= 0);
    }

    public int getTotalItems() {
        return stock.values().stream().mapToInt(Integer::intValue).sum();
    }

    // ─────────────────────────────────────────────
    //  Stack
    // ─────────────────────────────────────────────
    public void addStack(int amount) { this.stackSize = Math.max(1, stackSize + amount); }
    public int getStackSize() { return stackSize; }
    public void setStackSize(int s) { this.stackSize = Math.max(1, s); }

    // ─────────────────────────────────────────────
    //  Getters / Setters
    // ─────────────────────────────────────────────
    public String getLocationKey() { return locationKey; }
    public String getSpawnerType()  { return spawnerType; }
    public UUID getOwnerUUID()      { return ownerUUID; }
    public long getLastTick()       { return lastTick; }
    public void setLastTick(long t) { this.lastTick = t; }

    // ─────────────────────────────────────────────
    //  Serializers pentru YAML
    // ─────────────────────────────────────────────
    public static String locationToKey(Location loc) {
        return loc.getWorld().getName() + "," + loc.getBlockX() + "," + loc.getBlockY() + "," + loc.getBlockZ();
    }

    public static Location keyToLocation(String key) {
        String[] parts = key.split(",");
        return new Location(
                org.bukkit.Bukkit.getWorld(parts[0]),
                Integer.parseInt(parts[1]),
                Integer.parseInt(parts[2]),
                Integer.parseInt(parts[3])
        );
    }
}
