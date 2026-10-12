package dev.mobcoins.plugin.commands;

import dev.mobcoins.plugin.MobCoinsPlugin;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.*;
import org.bukkit.entity.Player;

import java.util.*;

public class MobCoinsCommand implements CommandExecutor, TabCompleter {

    private final MobCoinsPlugin plugin;

    public MobCoinsCommand(MobCoinsPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {

        // /flyshop
        if (command.getName().equalsIgnoreCase("flyshop")) {
            if (!(sender instanceof Player)) { sender.sendMessage(color("&cDoar jucatorii!")); return true; }
            Player player = (Player) sender;
            plugin.getShopGUI().openFlyShop(player);
            return true;
        }

        // /spawnershop
        if (command.getName().equalsIgnoreCase("spawnershop")) {
            if (!(sender instanceof Player)) { sender.sendMessage(color("&cDoar jucatorii!")); return true; }
            Player player = (Player) sender;
            if (!player.hasPermission("mobcoins.shop")) {
                player.sendMessage(color("&c&l🪙 &cNu ai permisiunea!")); return true;
            }
            plugin.getShopGUI().openShop(player);
            return true;
        }

        // /mobcoins
        String symbol = plugin.getConfig().getString("coin-symbol", "🪙");

        if (args.length == 0) {
            if (!(sender instanceof Player)) { sendHelp(sender, symbol); return true; }
            Player player = (Player) sender;
            long bal = plugin.getCoinManager().getBalance(player.getUniqueId());
            player.sendMessage("");
            player.sendMessage(color("&6&l" + symbol + " MOBCOINS &8» &7Balanta ta:"));
            player.sendMessage(color("  &e" + bal + " &6MobCoins"));
            player.sendMessage("");
            return true;
        }

        switch (args[0].toLowerCase()) {

            // /mobcoins balance [jucator]
            case "balance":
            case "bal":
            case "b": {
                if (args.length >= 2) {
                    Player target = Bukkit.getPlayer(args[1]);
                    if (target == null) { sender.sendMessage(color("&c" + symbol + " &cJucatorul nu e online!")); return true; }
                    long bal = plugin.getCoinManager().getBalance(target.getUniqueId());
                    sender.sendMessage(color("&6" + symbol + " &e" + target.getName() + " &7are &e" + bal + " &6MobCoins&7."));
                } else if (sender instanceof Player) {
                    Player player = (Player) sender;
                    long bal = plugin.getCoinManager().getBalance(player.getUniqueId());
                    sender.sendMessage(color("&6" + symbol + " &7Balanta ta: &e" + bal + " &6MobCoins&7."));
                }
                break;
            }

            // /mobcoins pay <jucator> <suma>
            case "pay":
            case "send": {
                if (!(sender instanceof Player)) { sender.sendMessage(color("&cDoar jucatorii!")); return true; }
                Player player = (Player) sender;
                if (!player.hasPermission("mobcoins.pay")) {
                    player.sendMessage(color("&c" + symbol + " &cNu ai permisiunea!")); return true;
                }
                if (args.length < 3) { player.sendMessage(color("&cUsage: /mobcoins pay <jucator> <suma>")); return true; }

                Player target = Bukkit.getPlayer(args[1]);
                if (target == null) { player.sendMessage(color("&c" + symbol + " &cJucatorul nu e online!")); return true; }
                if (target.equals(player)) { player.sendMessage(color("&c" + symbol + " &cNu poti trimite tie insuti!")); return true; }

                long amount;
                try { amount = Long.parseLong(args[2]); } catch (Exception e) {
                    player.sendMessage(color("&c" + symbol + " &cSuma invalida!")); return true;
                }
                if (amount <= 0) { player.sendMessage(color("&c" + symbol + " &cSuma trebuie sa fie pozitiva!")); return true; }

                if (!plugin.getCoinManager().hasCoins(player.getUniqueId(), amount)) {
                    player.sendMessage(color("&c" + symbol + " &cNu ai destule MobCoins!"));
                    player.sendMessage(color("  &7Ai: &c" + plugin.getCoinManager().getBalance(player.getUniqueId()) + " " + symbol));
                    return true;
                }

                plugin.getCoinManager().removeCoins(player.getUniqueId(), amount);
                plugin.getCoinManager().addCoins(target.getUniqueId(), amount);

                player.sendMessage(color("&a" + symbol + " &7Ai trimis &e" + amount + " MobCoins &7catre &a" + target.getName() + "&7."));
                target.sendMessage(color("&a" + symbol + " &7Ai primit &e" + amount + " MobCoins &7de la &a" + player.getName() + "&7."));
                break;
            }

            // /mobcoins top
            case "top":
            case "leaderboard": {
                if (!sender.hasPermission("mobcoins.top")) { sender.sendMessage(color("&cNu ai permisiunea!")); return true; }
                List<Map.Entry<UUID, Long>> top = plugin.getCoinManager().getTop(10);
                sender.sendMessage("");
                sender.sendMessage(color("&6&l" + symbol + " TOP MOBCOINS &8» &7Top 10 jucatori"));
                sender.sendMessage(color("&8▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬"));
                int pos = 1;
                for (Map.Entry<UUID, Long> entry : top) {
                    String name = Bukkit.getOfflinePlayer(entry.getKey()).getName();
                    if (name == null) name = "Unknown";
                    String medal = pos == 1 ? "&6#1" : pos == 2 ? "&7#2" : pos == 3 ? "&c#3" : "&8#" + pos;
                    sender.sendMessage(color("  " + medal + " &f" + name + " &8- &e" + entry.getValue() + " " + symbol));
                    pos++;
                }
                sender.sendMessage(color("&8▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬"));
                sender.sendMessage("");
                break;
            }

            // /mobcoins give <jucator> <suma> (admin)
            case "give": {
                if (!sender.hasPermission("mobcoins.admin")) { sender.sendMessage(color("&cNu ai permisiunea!")); return true; }
                if (args.length < 3) { sender.sendMessage(color("&cUsage: /mobcoins give <jucator> <suma>")); return true; }
                Player target = Bukkit.getPlayer(args[1]);
                if (target == null) { sender.sendMessage(color("&cJucatorul nu e online!")); return true; }
                long amount;
                try { amount = Long.parseLong(args[2]); } catch (Exception e) { sender.sendMessage(color("&cSuma invalida!")); return true; }
                plugin.getCoinManager().addCoins(target.getUniqueId(), amount);
                sender.sendMessage(color("&a" + symbol + " &7Ai dat &e" + amount + " MobCoins &7catre &a" + target.getName() + "&7."));
                target.sendMessage(color("&a" + symbol + " &7Ai primit &e" + amount + " MobCoins &7de la admin!"));
                break;
            }

            // /mobcoins set <jucator> <suma> (admin)
            case "set": {
                if (!sender.hasPermission("mobcoins.admin")) { sender.sendMessage(color("&cNu ai permisiunea!")); return true; }
                if (args.length < 3) { sender.sendMessage(color("&cUsage: /mobcoins set <jucator> <suma>")); return true; }
                Player target = Bukkit.getPlayer(args[1]);
                if (target == null) { sender.sendMessage(color("&cJucatorul nu e online!")); return true; }
                long amount;
                try { amount = Long.parseLong(args[2]); } catch (Exception e) { sender.sendMessage(color("&cSuma invalida!")); return true; }
                plugin.getCoinManager().setBalance(target.getUniqueId(), amount);
                sender.sendMessage(color("&a" + symbol + " &7Ai setat balanta lui &a" + target.getName() + " &7la &e" + amount + " MobCoins&7."));
                target.sendMessage(color("&a" + symbol + " &7Balanta ta a fost setata la &e" + amount + " MobCoins &7de catre admin."));
                break;
            }

            // /mobcoins shop
            case "shop": {
                if (!(sender instanceof Player)) { sender.sendMessage(color("&cDoar jucatorii!")); return true; }
                Player player = (Player) sender;
                if (!player.hasPermission("mobcoins.shop")) { player.sendMessage(color("&cNu ai permisiunea!")); return true; }
                plugin.getShopGUI().openShop(player);
                break;
            }

            case "flyshop":
            case "fly": {
                if (!(sender instanceof Player)) { sender.sendMessage(color("&cDoar jucatorii!")); return true; }
                Player player = (Player) sender;
                plugin.getShopGUI().openFlyShop(player);
                break;
            }

            default:
                sendHelp(sender, symbol);
                break;
        }
        return true;
    }

    private void sendHelp(CommandSender sender, String symbol) {
        sender.sendMessage("");
        sender.sendMessage(color("&6&l" + symbol + " MOBCOINS &8» &7Comenzi disponibile:"));
        sender.sendMessage(color("  &e/mobcoins &8- &7Afiseaza balanta ta"));
        sender.sendMessage(color("  &e/mobcoins balance [jucator] &8- &7Afiseaza balanta"));
        sender.sendMessage(color("  &e/mobcoins pay <jucator> <suma> &8- &7Trimite coins"));
        sender.sendMessage(color("  &e/mobcoins top &8- &7Top 10 jucatori"));
        sender.sendMessage(color("  &e/mobcoins shop &8- &7Deschide shop-ul de spawnere"));
        sender.sendMessage(color("  &e/mobcoins flyshop &8- &7Cumpara timp de fly"));
        if (sender.hasPermission("mobcoins.admin")) {
            sender.sendMessage(color("  &c/mobcoins give <jucator> <suma> &8- &7(Admin) Da coins"));
            sender.sendMessage(color("  &c/mobcoins set <jucator> <suma> &8- &7(Admin) Seteaza coins"));
        }
        sender.sendMessage("");
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String label, String[] args) {
        List<String> completions = new ArrayList<>();
        if (args.length == 1) {
            for (String s : Arrays.asList("balance", "pay", "top", "shop", "flyshop", "give", "set")) {
                if (s.startsWith(args[0].toLowerCase())) completions.add(s);
            }
        } else if (args.length == 2 && (args[0].equalsIgnoreCase("pay") || args[0].equalsIgnoreCase("balance")
                || args[0].equalsIgnoreCase("give") || args[0].equalsIgnoreCase("set"))) {
            for (Player p : Bukkit.getOnlinePlayers()) {
                if (p.getName().toLowerCase().startsWith(args[1].toLowerCase())) completions.add(p.getName());
            }
        }
        return completions;
    }

    private String color(String s) {
        return ChatColor.translateAlternateColorCodes('&', s);
    }
}
