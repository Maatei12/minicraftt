package dev.trade.tradeplugin.commands;

import dev.trade.tradeplugin.TradePlugin;
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

public class TradeCommand implements CommandExecutor, TabCompleter {

    private final TradePlugin plugin;

    public TradeCommand(TradePlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(colorize("&c&lTRADE &8» &cDoar jucatorii pot folosi aceasta comanda."));
            return true;
        }

        Player player = (Player) sender;

        if (!player.hasPermission("tradeplugin.trade")) {
            player.sendMessage(colorize("&c&lTRADE &8» &cNu ai permisiunea sa folosesti aceasta comanda."));
            return true;
        }

        if (args.length == 0) {
            sendHelp(player);
            return true;
        }

        String sub = args[0].toLowerCase();

        switch (sub) {
            case "accept":
            case "acept":
            case "a":
                plugin.getTradeManager().acceptTrade(player);
                break;

            case "deny":
            case "decline":
            case "refuza":
            case "d":
                plugin.getTradeManager().denyTrade(player);
                break;

            case "cancel":
            case "anuleaza":
                plugin.getTradeManager().cancelTradeForPlayer(player);
                break;

            default:
                // Incearca sa trimita cerere catre jucatorul cu acel nume
                Player target = Bukkit.getPlayer(args[0]);
                if (target == null) {
                    player.sendMessage(colorize("&c&lTRADE &8» &cJucatorul &e" + args[0] + " &cnu este online."));
                    return true;
                }
                if (target.equals(player)) {
                    player.sendMessage(colorize("&c&lTRADE &8» &cNu poti face trade cu tine insuti."));
                    return true;
                }
                plugin.getTradeManager().sendTradeRequest(player, target);
                break;
        }

        return true;
    }

    private void sendHelp(Player player) {
        player.sendMessage("");
        player.sendMessage(colorize("&6&l    ⚖ TRADE PLUGIN &8| &7Comenzi"));
        player.sendMessage(colorize("  &8▸ &e/trade &a<jucator> &8- &7Trimite cerere de trade"));
        player.sendMessage(colorize("  &8▸ &e/trade accept &8- &7Accepta cererea primita"));
        player.sendMessage(colorize("  &8▸ &e/trade deny &8- &7Refuza cererea primita"));
        player.sendMessage(colorize("  &8▸ &e/trade cancel &8- &7Anuleaza trade-ul curent"));
        player.sendMessage("");
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        List<String> completions = new ArrayList<>();
        if (args.length == 1) {
            String partial = args[0].toLowerCase();
            List<String> options = new ArrayList<>(Arrays.asList("accept", "deny", "cancel"));
            for (Player p : Bukkit.getOnlinePlayers()) {
                if (!p.equals(sender)) options.add(p.getName());
            }
            for (String opt : options) {
                if (opt.toLowerCase().startsWith(partial)) completions.add(opt);
            }
        }
        return completions;
    }

    private String colorize(String s) {
        return ChatColor.translateAlternateColorCodes('&', s);
    }
}
