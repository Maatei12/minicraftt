package dev.kits.plugin.commands;

import dev.kits.plugin.KitsPlugin;
import dev.kits.plugin.models.Kit;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class KitsCommand implements CommandExecutor, TabCompleter {

    private final KitsPlugin plugin;

    public KitsCommand(KitsPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(color("&c&lKIT &8» &cDoar jucatorii pot folosi aceasta comanda!"));
            return true;
        }

        Player player = (Player) sender;

        if (!player.hasPermission("kits.use")) {
            player.sendMessage(color("&c&lKIT &8» &cNu ai permisiunea sa folosesti aceasta comanda!"));
            return true;
        }

        // /kit - deschide meniu
        if (args.length == 0) {
            plugin.getKitsGUI().openMenu(player);
            return true;
        }

        // /kit admin reset <jucator> [kit]
        if (args[0].equalsIgnoreCase("admin") && player.hasPermission("kits.admin")) {
            handleAdmin(player, args);
            return true;
        }

        // /kit reload
        if (args[0].equalsIgnoreCase("reload") && player.hasPermission("kits.admin")) {
            plugin.reloadConfig();
            plugin.getKitManager().loadKits();
            player.sendMessage(color("&a&lKIT &8» &aConfig reincarcata cu succes!"));
            return true;
        }

        // /kit <nume> - ia direct kitul
        String kitId = args[0].toLowerCase();
        Kit kit = plugin.getKitManager().getKit(kitId);
        if (kit == null) {
            player.sendMessage(color("&c&lKIT &8» &cKitul &e" + args[0] + " &cnu exista!"));
            player.sendMessage(color("  &7Foloseste &e/kit &7pentru a vedea kiturile disponibile."));
            return true;
        }

        plugin.getKitManager().claimKit(player, kit);
        return true;
    }

    private void handleAdmin(Player player, String[] args) {
        // /kit admin reset <jucator> [kit]
        if (args.length >= 3 && args[1].equalsIgnoreCase("reset")) {
            Player target = Bukkit.getPlayer(args[2]);
            if (target == null) {
                player.sendMessage(color("&c&lKIT &8» &cJucatorul &e" + args[2] + " &cnu e online!"));
                return;
            }
            if (args.length >= 4) {
                // Reset cooldown specific
                Kit kit = plugin.getKitManager().getKit(args[3]);
                if (kit == null) {
                    player.sendMessage(color("&c&lKIT &8» &cKitul &e" + args[3] + " &cnu exista!"));
                    return;
                }
                plugin.getKitManager().resetCooldown(target, kit);
                player.sendMessage(color("&a&lKIT &8» &7Cooldown-ul kitului &e" + kit.getDisplayName()
                        + " &7al jucatorului &a" + target.getName() + " &7a fost resetat!"));
                target.sendMessage(color("&a&lKIT &8» &7Cooldown-ul kitului &e" + kit.getDisplayName() + " &7a fost resetat de un admin!"));
            } else {
                // Reset toate cooldown-urile
                plugin.getKitManager().resetAllCooldowns(target);
                player.sendMessage(color("&a&lKIT &8» &7Toate cooldown-urile jucatorului &a" + target.getName() + " &7au fost resetate!"));
                target.sendMessage(color("&a&lKIT &8» &7Toate cooldown-urile tale au fost resetate de un admin!"));
            }
            return;
        }

        // Help admin
        player.sendMessage(color("&6&lKIT ADMIN &8» &7Comenzi:"));
        player.sendMessage(color("  &e/kit admin reset &a<jucator> &8- &7Reseteaza toate cooldown-urile"));
        player.sendMessage(color("  &e/kit admin reset &a<jucator> <kit> &8- &7Reseteaza cooldown specific"));
        player.sendMessage(color("  &e/kit reload &8- &7Reincarca config"));
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        List<String> completions = new ArrayList<>();

        if (args.length == 1) {
            List<String> options = new ArrayList<>(Arrays.asList("admin", "reload"));
            for (Kit kit : plugin.getKitManager().getKits()) options.add(kit.getId());
            for (String opt : options) {
                if (opt.toLowerCase().startsWith(args[0].toLowerCase())) completions.add(opt);
            }
        } else if (args.length == 2 && args[0].equalsIgnoreCase("admin")) {
            if ("reset".startsWith(args[1].toLowerCase())) completions.add("reset");
        } else if (args.length == 3 && args[0].equalsIgnoreCase("admin")) {
            for (Player p : Bukkit.getOnlinePlayers()) {
                if (p.getName().toLowerCase().startsWith(args[2].toLowerCase())) completions.add(p.getName());
            }
        } else if (args.length == 4 && args[0].equalsIgnoreCase("admin")) {
            for (Kit kit : plugin.getKitManager().getKits()) {
                if (kit.getId().startsWith(args[3].toLowerCase())) completions.add(kit.getId());
            }
        }

        return completions;
    }

    private String color(String s) {
        return ChatColor.translateAlternateColorCodes('&', s);
    }
}
