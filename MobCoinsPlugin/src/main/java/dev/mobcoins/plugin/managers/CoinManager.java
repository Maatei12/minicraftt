package dev.mobcoins.plugin.managers;

import dev.mobcoins.plugin.MobCoinsPlugin;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.*;

public class CoinManager {

    private final MobCoinsPlugin plugin;
    private final Map<UUID, Long> balances = new HashMap<>();
    private File dataFile;
    private YamlConfiguration dataConfig;

    public CoinManager(MobCoinsPlugin plugin) {
        this.plugin = plugin;
        load();
    }

    // ── Balanta ──────────────────────────────────────────────────────────────

    public long getBalance(UUID uuid) {
        return balances.getOrDefault(uuid, 0L);
    }

    public void setBalance(UUID uuid, long amount) {
        balances.put(uuid, Math.max(0, amount));
        save();
    }

    public void addCoins(UUID uuid, long amount) {
        setBalance(uuid, getBalance(uuid) + amount);
    }

    public boolean removeCoins(UUID uuid, long amount) {
        long bal = getBalance(uuid);
        if (bal < amount) return false;
        setBalance(uuid, bal - amount);
        return true;
    }

    public boolean hasCoins(UUID uuid, long amount) {
        return getBalance(uuid) >= amount;
    }

    // ── Top ──────────────────────────────────────────────────────────────────

    public List<Map.Entry<UUID, Long>> getTop(int limit) {
        List<Map.Entry<UUID, Long>> list = new ArrayList<>(balances.entrySet());
        list.sort((a, b) -> Long.compare(b.getValue(), a.getValue()));
        return list.subList(0, Math.min(limit, list.size()));
    }

    // ── Persistenta ──────────────────────────────────────────────────────────

    private void load() {
        dataFile = new File(plugin.getDataFolder(), "balances.yml");
        if (!dataFile.exists()) {
            try { dataFile.createNewFile(); } catch (IOException e) { e.printStackTrace(); }
        }
        dataConfig = YamlConfiguration.loadConfiguration(dataFile);
        for (String key : dataConfig.getKeys(false)) {
            try {
                balances.put(UUID.fromString(key), dataConfig.getLong(key));
            } catch (Exception ignored) {}
        }
    }

    public void save() {
        for (Map.Entry<UUID, Long> entry : balances.entrySet()) {
            dataConfig.set(entry.getKey().toString(), entry.getValue());
        }
        try { dataConfig.save(dataFile); } catch (IOException e) { e.printStackTrace(); }
    }
}
