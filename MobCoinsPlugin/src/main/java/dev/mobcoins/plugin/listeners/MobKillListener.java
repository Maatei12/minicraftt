package dev.mobcoins.plugin.listeners;

import dev.mobcoins.plugin.MobCoinsPlugin;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.ChatColor;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDeathEvent;

import java.util.Random;

public class MobKillListener implements Listener {

    private final MobCoinsPlugin plugin;
    private final Random random = new Random();

    public MobKillListener(MobCoinsPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onEntityDeath(EntityDeathEvent event) {
        LivingEntity entity = event.getEntity();
        Player killer = entity.getKiller();

        // Trebuie sa fie omorat de un jucator
        if (killer == null) return;

        // Cauta reward in config
        String mobType = entity.getType().name();
        ConfigurationSection rewards = plugin.getConfig().getConfigurationSection("mob-rewards");
        if (rewards == null || !rewards.contains(mobType)) return;

        ConfigurationSection mobCfg = rewards.getConfigurationSection(mobType);
        if (mobCfg == null) return;

        double chance = mobCfg.getDouble("chance", 1.0);
        if (random.nextDouble() > chance) return;

        int min = mobCfg.getInt("min", 1);
        int max = mobCfg.getInt("max", 1);
        long coins = min + (max > min ? random.nextInt(max - min + 1) : 0);

        // Adauga coins
        plugin.getCoinManager().addCoins(killer.getUniqueId(), coins);
        long newBal = plugin.getCoinManager().getBalance(killer.getUniqueId());

        // ActionBar cu animatie
        String symbol = plugin.getConfig().getString("coin-symbol", "🍯");
        String coinName = plugin.getConfig().getString("coin-name", "Miere");
        String msg = ChatColor.translateAlternateColorCodes('&',
                "&6" + symbol + " &e+" + coins + " " + coinName + " &8| &7Balanta: &6" + newBal + " " + symbol);
        killer.spigot().sendMessage(ChatMessageType.ACTION_BAR, new TextComponent(msg));
    }
}
