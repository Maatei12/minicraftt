package dev.worldlock.plugin;

import org.bukkit.ChatColor;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityPortalEvent;
import org.bukkit.event.player.PlayerPortalEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.plugin.java.JavaPlugin;

public class WorldLockPlugin extends JavaPlugin implements Listener {

    @Override
    public void onEnable() {
        getServer().getPluginManager().registerEvents(this, this);
        getLogger().info("WorldLockPlugin pornit! Nether = BLOCAT | End = doar OP");
    }

    @Override
    public void onDisable() {
        getLogger().info("WorldLockPlugin oprit.");
    }

    // ─────────────────────────────────────────────
    //  Blocare portal (principal - prinde toate portalele)
    // ─────────────────────────────────────────────
    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = false)
    public void onPlayerPortal(PlayerPortalEvent event) {
        Player player = event.getPlayer();
        World.Environment dest = getDestinationEnvironment(event.getCause());

        if (dest == World.Environment.NETHER) {
            event.setCancelled(true);
            sendMessage(player, "nether");
            return;
        }

        if (dest == World.Environment.THE_END) {
            if (!player.hasPermission("worldlock.end") && !player.hasPermission("worldlock.bypass")) {
                event.setCancelled(true);
                sendMessage(player, "end");
            }
        }
    }

    // ─────────────────────────────────────────────
    //  Blocare teleportare generala (comanda /tp, plugin-uri etc.)
    // ─────────────────────────────────────────────
    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = false)
    public void onPlayerTeleport(PlayerTeleportEvent event) {
        Player player = event.getPlayer();
        if (event.getTo() == null) return;

        World.Environment env = event.getTo().getWorld().getEnvironment();

        // Bypass complet
        if (player.hasPermission("worldlock.bypass")) return;

        if (env == World.Environment.NETHER) {
            event.setCancelled(true);
            sendMessage(player, "nether");
            return;
        }

        if (env == World.Environment.THE_END) {
            if (!player.hasPermission("worldlock.end")) {
                event.setCancelled(true);
                sendMessage(player, "end");
            }
        }
    }

    // ─────────────────────────────────────────────
    //  Blocare mob portal (ca sa nu traga mobs in nether/end)
    // ─────────────────────────────────────────────
    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = false)
    public void onEntityPortal(EntityPortalEvent event) {
        if (event.getTo() == null) return;
        World.Environment env = event.getTo().getWorld().getEnvironment();
        if (env == World.Environment.NETHER || env == World.Environment.THE_END) {
            event.setCancelled(true);
        }
    }

    // ─────────────────────────────────────────────
    //  Blocare respawn in end (daca cumva ajunge acolo)
    // ─────────────────────────────────────────────
    @EventHandler(priority = EventPriority.HIGHEST)
    public void onRespawn(PlayerRespawnEvent event) {
        Player player = event.getPlayer();
        if (player.hasPermission("worldlock.bypass")) return;

        World.Environment env = event.getRespawnLocation().getWorld().getEnvironment();

        if (env == World.Environment.NETHER) {
            // Teleporteaza inapoi in overworld spawn
            event.setRespawnLocation(player.getWorld().getSpawnLocation());
            sendMessage(player, "nether");
        }

        if (env == World.Environment.THE_END && !player.hasPermission("worldlock.end")) {
            event.setRespawnLocation(player.getWorld().getSpawnLocation());
            sendMessage(player, "end");
        }
    }

    // ─────────────────────────────────────────────
    //  Mesaje
    // ─────────────────────────────────────────────
    private void sendMessage(Player player, String world) {
        if (world.equals("nether")) {
            player.sendMessage(c("&8[&c✗&8] &c&lNETHER &8» &7Nether-ul este &c&linchis &7momentan!"));
        } else {
            player.sendMessage(c("&8[&5✗&8] &5&lEND &8» &7End-ul este &5&linchis &7momentan!"));
        }
        player.playSound(player.getLocation(), org.bukkit.Sound.ENTITY_VILLAGER_NO, 1f, 1f);
    }

    private String c(String s) {
        return ChatColor.translateAlternateColorCodes('&', s);
    }

    // ─────────────────────────────────────────────
    //  Determina destinatia portalului
    // ─────────────────────────────────────────────
    private World.Environment getDestinationEnvironment(PlayerTeleportEvent.TeleportCause cause) {
        switch (cause) {
            case NETHER_PORTAL:
                return World.Environment.NETHER;
            case END_PORTAL:
            case END_GATEWAY:
                return World.Environment.THE_END;
            default:
                return World.Environment.NORMAL;
        }
    }
}
