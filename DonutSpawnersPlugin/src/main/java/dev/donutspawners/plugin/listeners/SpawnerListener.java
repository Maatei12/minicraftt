package dev.donutspawners.plugin.listeners;

import dev.donutspawners.plugin.DonutSpawnersPlugin;
import dev.donutspawners.plugin.data.SpawnerData;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.block.CreatureSpawner;
import org.bukkit.entity.Player;
import org.bukkit.event.*;
import org.bukkit.event.block.*;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BlockStateMeta;

public class SpawnerListener implements Listener {

    private final DonutSpawnersPlugin plugin;

    public SpawnerListener(DonutSpawnersPlugin plugin) {
        this.plugin = plugin;
    }

    // ─────────────────────────────────────────────
    //  Plasare spawner
    // ─────────────────────────────────────────────
    @EventHandler(priority = EventPriority.HIGH)
    public void onBlockPlace(BlockPlaceEvent event) {
        Block block = event.getBlockPlaced();
        if (block.getType() != Material.SPAWNER) return;

        Player player = event.getPlayer();
        ItemStack item = event.getItemInHand();

        // Determina tipul spawnerului din item
        String spawnerType = plugin.getSpawnerManager().getSpawnerTypeFromItem(item);
        if (spawnerType == null) {
            // Spawner vanilla fara tip definit - il ignoram
            return;
        }

        // Verifica daca tipul e configurat
        if (plugin.getConfig().getConfigurationSection("spawner-types." + spawnerType) == null) {
            player.sendMessage(color("&c&8[&6⚙&8] &7Acest tip de spawner nu este configurat!"));
            event.setCancelled(true);
            return;
        }

        // Inregistreaza spawnerul
        plugin.getSpawnerManager().placeSpawner(block.getLocation(), spawnerType, player.getUniqueId());

        // Opreste AI-ul nativ (spawnerul nu va spawna entitati)
        if (block.getState() instanceof CreatureSpawner) {
            CreatureSpawner cs = (CreatureSpawner) block.getState();
            cs.setDelay(Integer.MAX_VALUE); // Delay infinit = nu spawneaza niciodata
            cs.update();
        }

        String displayName = plugin.getConfig().getString("spawner-types." + spawnerType + ".display-name", spawnerType);
        player.sendMessage(color("&8[&6⚙&8] &aAi plasat " + displayName + "&a!"));
        player.sendMessage(color("  &7Genereaza drops la fiecare &e"
                + plugin.getConfig().getInt("tick-interval", 30) + " &7secunde."));
        player.sendMessage(color("  &eRight-Click &7sa colectezi &8| &eShift+Right-Click &7sa stacked"));
        player.playSound(player.getLocation(), Sound.BLOCK_ANVIL_USE, 0.5f, 1.3f);
    }

    // ─────────────────────────────────────────────
    //  Spargere spawner
    // ─────────────────────────────────────────────
    @EventHandler(priority = EventPriority.HIGH)
    public void onBlockBreak(BlockBreakEvent event) {
        Block block = event.getBlock();
        if (block.getType() != Material.SPAWNER) return;
        if (!plugin.getSpawnerManager().isDonutSpawner(block.getLocation())) return;

        Player player = event.getPlayer();
        SpawnerData data = plugin.getSpawnerManager().removeSpawner(block.getLocation());
        if (data == null) return;

        // Opreste drop-ul default de spawner
        event.setDropItems(false);
        event.setExpToDrop(0);

        // Da spawnerul inapoi ca item
        ItemStack spawnerItem = plugin.getSpawnerManager().createSpawnerItem(data.getSpawnerType(), data.getStackSize());
        player.getWorld().dropItemNaturally(block.getLocation(), spawnerItem);

        // Daca mai era stoc, returneaza si stocul
        if (!data.isEmpty()) {
            player.sendMessage(color("&8[&6⚙&8] &eAi spart spawnerul! Stocul ramas a fost aruncat."));
            for (java.util.Map.Entry<Material, Integer> entry : data.getStock().entrySet()) {
                int remaining = entry.getValue();
                while (remaining > 0) {
                    int amount = Math.min(remaining, entry.getKey().getMaxStackSize());
                    block.getWorld().dropItemNaturally(block.getLocation(), new ItemStack(entry.getKey(), amount));
                    remaining -= amount;
                }
            }
        }

        plugin.getSpawnerGUI().removeViewer(player.getUniqueId());
        player.sendMessage(color("&8[&6⚙&8] &7Spawner recuperat: &e" + data.getStackSize() + "x"));
    }

    // ─────────────────────────────────────────────
    //  Right-Click pe spawner
    // ─────────────────────────────────────────────
    @EventHandler(priority = EventPriority.HIGH)
    public void onPlayerInteract(PlayerInteractEvent event) {
        if (event.getAction() != Action.RIGHT_CLICK_BLOCK) return;
        if (event.getHand() != EquipmentSlot.HAND) return;

        Block block = event.getClickedBlock();
        if (block == null || block.getType() != Material.SPAWNER) return;
        if (!plugin.getSpawnerManager().isDonutSpawner(block.getLocation())) return;

        event.setCancelled(true);
        Player player = event.getPlayer();
        SpawnerData data = plugin.getSpawnerManager().getSpawnerAt(block.getLocation());
        if (data == null) return;

        // Shift + Right-Click = stack
        if (player.isSneaking()) {
            ItemStack inHand = player.getInventory().getItemInMainHand();
            if (inHand.getType() == Material.SPAWNER) {
                plugin.getSpawnerManager().stackSpawner(player, block.getLocation());
                return;
            }
        }

        // Right-Click normal = apele GUI
        plugin.getSpawnerGUI().openFor(player, data);
    }

    // ─────────────────────────────────────────────
    //  Opreste explozia sa distruga spawnerele
    // ─────────────────────────────────────────────
    @EventHandler(priority = EventPriority.HIGH)
    public void onBlockExplode(BlockExplodeEvent event) {
        event.blockList().removeIf(block ->
                block.getType() == Material.SPAWNER &&
                plugin.getSpawnerManager().isDonutSpawner(block.getLocation()));
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onEntityExplode(org.bukkit.event.entity.EntityExplodeEvent event) {
        event.blockList().removeIf(block ->
                block.getType() == Material.SPAWNER &&
                plugin.getSpawnerManager().isDonutSpawner(block.getLocation()));
    }

    private String color(String s) {
        return ChatColor.translateAlternateColorCodes('&', s);
    }
}
