package dev.donutspawners.plugin;

import dev.donutspawners.plugin.gui.SpawnerGUI;
import dev.donutspawners.plugin.listeners.SpawnerListener;
import dev.donutspawners.plugin.managers.SpawnerManager;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

public class DonutSpawnersPlugin extends JavaPlugin {

    private static DonutSpawnersPlugin instance;
    private SpawnerManager spawnerManager;
    private SpawnerGUI spawnerGUI;

    @Override
    public void onEnable() {
        instance = this;
        saveDefaultConfig();

        this.spawnerManager = new SpawnerManager(this);
        this.spawnerGUI     = new SpawnerGUI(this);

        getServer().getPluginManager().registerEvents(new SpawnerListener(this), this);
        getServer().getPluginManager().registerEvents(spawnerGUI, this);

        getLogger().info("DonutSpawnersPlugin pornit! " +
                getConfig().getConfigurationSection("spawner-types").getKeys(false).size() +
                " tipuri de spawnere incarcate.");
    }

    @Override
    public void onDisable() {
        if (spawnerManager != null) spawnerManager.saveData();
        getLogger().info("DonutSpawnersPlugin oprit. Date salvate.");
    }

    // ─────────────────────────────────────────────
    //  Comanda /spawnerinfo + /givespawner (admin)
    // ─────────────────────────────────────────────
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("Doar jucatorii pot folosi aceasta comanda!");
            return true;
        }
        Player player = (Player) sender;

        // /spawnerinfo - info despre spawnerul la care te uiti
        if (command.getName().equalsIgnoreCase("spawnerinfo")) {
            org.bukkit.block.Block target = player.getTargetBlockExact(5);
            if (target == null || target.getType() != Material.SPAWNER) {
                player.sendMessage(color("&c&8[&6⚙&8] &7Uita-te la un spawner (max 5 blocuri distanta)!"));
                return true;
            }
            if (!spawnerManager.isDonutSpawner(target.getLocation())) {
                player.sendMessage(color("&c&8[&6⚙&8] &7Acesta nu e un DonutSpawner!"));
                return true;
            }
            dev.donutspawners.plugin.data.SpawnerData data = spawnerManager.getSpawnerAt(target.getLocation());
            String ownerName = org.bukkit.Bukkit.getOfflinePlayer(data.getOwnerUUID()).getName();
            player.sendMessage(color("&8[&6⚙&8] &eInfo Spawner:"));
            player.sendMessage(color("  &7Tip: &e" + data.getSpawnerType()));
            player.sendMessage(color("  &7Stack: &e" + data.getStackSize() + "x"));
            player.sendMessage(color("  &7Proprietar: &f" + (ownerName != null ? ownerName : "?")));
            player.sendMessage(color("  &7Stoc: &e" + data.getTotalItems() + " &7iteme"));
            return true;
        }

        // /givespawner <tip> [cantitate] - admin
        if (command.getName().equalsIgnoreCase("givespawner")) {
            if (!player.hasPermission("donutspawners.admin")) {
                player.sendMessage(color("&c&8[&6⚙&8] &cNu ai permisiunea!"));
                return true;
            }
            if (args.length < 1) {
                player.sendMessage(color("&cUsage: /givespawner <tip> [cantitate]"));
                player.sendMessage(color("&7Tipuri: " + String.join(", ",
                        getConfig().getConfigurationSection("spawner-types").getKeys(false))));
                return true;
            }
            String type = args[0].toUpperCase();
            if (getConfig().getConfigurationSection("spawner-types." + type) == null) {
                player.sendMessage(color("&c&8[&6⚙&8] &7Tip invalid! Tipuri disponibile:"));
                player.sendMessage(color("  &e" + String.join(", ",
                        getConfig().getConfigurationSection("spawner-types").getKeys(false))));
                return true;
            }
            int amount = args.length >= 2 ? Math.max(1, parseInt(args[1])) : 1;
            org.bukkit.inventory.ItemStack item = spawnerManager.createSpawnerItem(type, amount);
            player.getInventory().addItem(item);
            player.sendMessage(color("&8[&6⚙&8] &aAi primit &e" + amount + "x &fSpawner " + type + "&a!"));
            return true;
        }

        return false;
    }

    private int parseInt(String s) {
        try { return Integer.parseInt(s); } catch (Exception e) { return 1; }
    }

    public static DonutSpawnersPlugin getInstance() { return instance; }
    public SpawnerManager getSpawnerManager() { return spawnerManager; }
    public SpawnerGUI getSpawnerGUI() { return spawnerGUI; }

    private String color(String s) {
        return ChatColor.translateAlternateColorCodes('&', s);
    }
}
