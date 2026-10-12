package dev.donutspawners.plugin.managers;

import dev.donutspawners.plugin.DonutSpawnersPlugin;
import dev.donutspawners.plugin.data.SpawnerData;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BlockStateMeta;
import org.bukkit.block.CreatureSpawner;

import java.io.File;
import java.io.IOException;
import java.util.*;

public class SpawnerManager {

    private final DonutSpawnersPlugin plugin;
    private final Map<String, SpawnerData> spawners = new HashMap<>();
    private File dataFile;
    private YamlConfiguration dataConfig;

    public SpawnerManager(DonutSpawnersPlugin plugin) {
        this.plugin = plugin;
        loadData();
        startTickTask();
    }

    // ─────────────────────────────────────────────
    //  Tick pasiv - genereaza drops la interval
    // ─────────────────────────────────────────────
    private void startTickTask() {
        int intervalTicks = plugin.getConfig().getInt("tick-interval", 30) * 20;
        Bukkit.getScheduler().runTaskTimer(plugin, this::tickAllSpawners, intervalTicks, intervalTicks);
    }

    private void tickAllSpawners() {
        int maxStock = plugin.getConfig().getInt("max-stock", 1000);
        long now = System.currentTimeMillis();

        for (SpawnerData data : spawners.values()) {
            if (data.getTotalItems() >= maxStock) continue;

            ConfigurationSection typeCfg = plugin.getConfig()
                    .getConfigurationSection("spawner-types." + data.getSpawnerType());
            if (typeCfg == null) continue;

            List<String> drops = typeCfg.getStringList("drops");
            Random random = new Random();

            for (String dropStr : drops) {
                // Format: MATERIAL:min-max
                String[] parts = dropStr.split(":");
                if (parts.length < 2) continue;
                Material mat = Material.matchMaterial(parts[0].trim());
                if (mat == null) continue;

                String[] range = parts[1].split("-");
                int min = Integer.parseInt(range[0].trim());
                int max = range.length > 1 ? Integer.parseInt(range[1].trim()) : min;
                int amount = min + (max > min ? random.nextInt(max - min + 1) : 0);

                // Multiplica dupa stack size (cu diminishing returns la fel ca Donut)
                int stackSize = data.getStackSize();
                if (stackSize > 1) {
                    // Formula diminishing returns: amount * log(stackSize + 1) * factor
                    double multiplier = Math.log(stackSize + 1) / Math.log(2);
                    amount = (int) Math.max(amount, amount * multiplier * 0.7);
                }

                if (data.getTotalItems() + amount > maxStock) {
                    amount = maxStock - data.getTotalItems();
                }
                if (amount > 0) data.addToStock(mat, amount);
            }

            data.setLastTick(now);
        }
        saveData();
    }

    // ─────────────────────────────────────────────
    //  Plasare spawner
    // ─────────────────────────────────────────────
    public void placeSpawner(Location loc, String spawnerType, UUID ownerUUID) {
        String key = SpawnerData.locationToKey(loc);
        SpawnerData data = new SpawnerData(key, spawnerType, ownerUUID);
        spawners.put(key, data);
        saveData();
    }

    // ─────────────────────────────────────────────
    //  Scoatere spawner (break)
    // ─────────────────────────────────────────────
    public SpawnerData removeSpawner(Location loc) {
        String key = SpawnerData.locationToKey(loc);
        SpawnerData data = spawners.remove(key);
        if (data != null) saveData();
        return data;
    }

    // ─────────────────────────────────────────────
    //  Colectare drops (right-click)
    // ─────────────────────────────────────────────
    public void collectDrops(Player player, SpawnerData data) {
        if (data.isEmpty()) {
            player.sendMessage(color("&8[&6⚙&8] &7Spawner-ul nu a acumulat inca nimic! Revino mai tarziu."));
            return;
        }

        Map<Material, Integer> stock = new HashMap<>(data.getStock());
        data.clearStock();
        saveData();

        int given = 0;
        for (Map.Entry<Material, Integer> entry : stock.entrySet()) {
            int remaining = entry.getValue();
            while (remaining > 0) {
                int amount = Math.min(remaining, entry.getKey().getMaxStackSize());
                ItemStack item = new ItemStack(entry.getKey(), amount);
                Map<Integer, ItemStack> leftover = player.getInventory().addItem(item);
                if (!leftover.isEmpty()) {
                    // Inventar plin - returnam in stoc
                    for (ItemStack leftItem : leftover.values()) {
                        data.addToStock(leftItem.getType(), leftItem.getAmount());
                    }
                    player.sendMessage(color("&c&8[&6⚙&8] &7Inventarul tau este plin! Unele iteme au ramas in spawner."));
                    saveData();
                    break;
                }
                remaining -= amount;
                given += amount;
            }
        }

        if (given > 0) {
            player.sendMessage(color("&8[&6⚙&8] &aAi colectat &e" + given + " &7iteme din spawner!"));
            player.playSound(player.getLocation(), org.bukkit.Sound.ENTITY_ITEM_PICKUP, 0.8f, 1.2f);
        }
    }

    // ─────────────────────────────────────────────
    //  Stack spawner (shift+rightclick)
    // ─────────────────────────────────────────────
    public boolean stackSpawner(Player player, Location loc) {
        SpawnerData data = getSpawnerAt(loc);
        if (data == null) return false;

        // Cauta in inventarul jucatorului un spawner de acelasi tip
        ItemStack inHand = player.getInventory().getItemInMainHand();
        if (inHand.getType() != Material.SPAWNER) return false;

        String handType = getSpawnerTypeFromItem(inHand);
        if (handType == null || !handType.equals(data.getSpawnerType())) {
            player.sendMessage(color("&c&8[&6⚙&8] &7Poti stacked doar spawnere de acelasi tip!"));
            return false;
        }

        // Consuma spawnerul din mana
        int amount = inHand.getAmount();
        data.addStack(amount);
        player.getInventory().setItemInMainHand(null);

        player.sendMessage(color("&8[&6⚙&8] &aAi adaugat &e" + amount + " &7spawnere! Stack total: &e" + data.getStackSize() + "x"));
        player.playSound(player.getLocation(), org.bukkit.Sound.BLOCK_ANVIL_USE, 0.5f, 1.2f);
        saveData();
        return true;
    }

    // ─────────────────────────────────────────────
    //  Creeaza item spawner pentru drop / dat de admin
    // ─────────────────────────────────────────────
    public ItemStack createSpawnerItem(String spawnerType, int amount) {
        ItemStack item = new ItemStack(Material.SPAWNER, amount);
        BlockStateMeta bsm = (BlockStateMeta) item.getItemMeta();
        if (bsm != null) {
            try {
                org.bukkit.entity.EntityType eType = org.bukkit.entity.EntityType.valueOf(spawnerType.toUpperCase());
                CreatureSpawner cs = (CreatureSpawner) bsm.getBlockState();
                cs.setSpawnedType(eType);
                bsm.setBlockState(cs);
            } catch (Exception ignored) {}

            ConfigurationSection typeCfg = plugin.getConfig().getConfigurationSection("spawner-types." + spawnerType);
            String displayName = typeCfg != null ? typeCfg.getString("display-name", spawnerType) : spawnerType;
            String tier = typeCfg != null ? typeCfg.getString("tier", "?") : "?";

            bsm.setDisplayName(color(displayName));
            List<String> lore = new ArrayList<>();
            lore.add(color("&8▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬"));
            lore.add(color("  &7Tier: &e" + tier));
            lore.add(color("  &7Drop-uri pasive fara lag!"));
            lore.add(color("&8▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬"));
            lore.add(color("  &eRight-Click &7sa colectezi"));
            lore.add(color("  &eShift+Right-Click &7sa stacked"));
            lore.add(color("&8▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬"));
            lore.add(color("  &8DonutSpawners Plugin"));
            bsm.setLore(lore);
            item.setItemMeta(bsm);
        }
        return item;
    }

    // ─────────────────────────────────────────────
    //  Getters
    // ─────────────────────────────────────────────
    public SpawnerData getSpawnerAt(Location loc) {
        return spawners.get(SpawnerData.locationToKey(loc));
    }

    public boolean isDonutSpawner(Location loc) {
        return spawners.containsKey(SpawnerData.locationToKey(loc));
    }

    public String getSpawnerTypeFromItem(ItemStack item) {
        if (item == null || item.getType() != Material.SPAWNER) return null;
        if (!item.hasItemMeta()) return null;
        BlockStateMeta bsm = (BlockStateMeta) item.getItemMeta();
        if (!(bsm.getBlockState() instanceof CreatureSpawner)) return null;
        CreatureSpawner cs = (CreatureSpawner) bsm.getBlockState();
        if (cs.getSpawnedType() == null) return null;
        return cs.getSpawnedType().name();
    }

    public Map<String, SpawnerData> getAllSpawners() { return spawners; }

    // ─────────────────────────────────────────────
    //  Save / Load
    // ─────────────────────────────────────────────
    private void loadData() {
        dataFile = new File(plugin.getDataFolder(), "spawners.yml");
        if (!dataFile.exists()) {
            try { dataFile.createNewFile(); } catch (IOException e) { e.printStackTrace(); }
        }
        dataConfig = YamlConfiguration.loadConfiguration(dataFile);

        for (String key : dataConfig.getKeys(false)) {
            try {
                String type  = dataConfig.getString(key + ".type");
                String owner = dataConfig.getString(key + ".owner");
                int stack    = dataConfig.getInt(key + ".stack", 1);
                long lastTick = dataConfig.getLong(key + ".lastTick", System.currentTimeMillis());

                SpawnerData data = new SpawnerData(key, type, UUID.fromString(owner));
                data.setStackSize(stack);
                data.setLastTick(lastTick);

                // Carica stocul
                ConfigurationSection stockSec = dataConfig.getConfigurationSection(key + ".stock");
                if (stockSec != null) {
                    for (String matName : stockSec.getKeys(false)) {
                        Material mat = Material.matchMaterial(matName);
                        if (mat != null) data.addToStock(mat, stockSec.getInt(matName));
                    }
                }

                spawners.put(key, data);
            } catch (Exception e) {
                plugin.getLogger().warning("Eroare la incarcarea spawnerului: " + key);
            }
        }
        plugin.getLogger().info("Incarcate " + spawners.size() + " spawnere din disk.");
    }

    public void saveData() {
        for (Map.Entry<String, SpawnerData> entry : spawners.entrySet()) {
            String key = entry.getKey();
            SpawnerData data = entry.getValue();
            dataConfig.set(key + ".type", data.getSpawnerType());
            dataConfig.set(key + ".owner", data.getOwnerUUID().toString());
            dataConfig.set(key + ".stack", data.getStackSize());
            dataConfig.set(key + ".lastTick", data.getLastTick());
            for (Map.Entry<Material, Integer> s : data.getStock().entrySet()) {
                dataConfig.set(key + ".stock." + s.getKey().name(), s.getValue());
            }
        }
        try { dataConfig.save(dataFile); } catch (IOException e) { e.printStackTrace(); }
    }

    private String color(String s) {
        return org.bukkit.ChatColor.translateAlternateColorCodes('&', s);
    }
}
