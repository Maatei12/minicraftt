package dev.serverinfo.plugin.commands;

import dev.serverinfo.plugin.ServerInfoPlugin;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.util.Collections;
import java.util.List;

public class ServerInfoCommand implements CommandExecutor, TabCompleter {

    private final ServerInfoPlugin plugin;

    public ServerInfoCommand(ServerInfoPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {

        // Comanda reload
        if (command.getName().equalsIgnoreCase("reload-serverinfo")) {
            if (!sender.hasPermission("serverinfo.reload")) {
                sender.sendMessage(color("&c&lSERVERINFO &8» &cNu ai permisiunea!"));
                return true;
            }
            plugin.reloadConfig();
            sender.sendMessage(color("&a&lSERVERINFO &8» &aConfig reincarcata cu succes!"));
            return true;
        }

        // Comanda /serverinfo
        if (!(sender instanceof Player)) {
            sender.sendMessage(color("&c&lSERVERINFO &8» &cDoar jucatorii pot folosi aceasta comanda!"));
            return true;
        }

        Player player = (Player) sender;

        if (!player.hasPermission("serverinfo.use")) {
            player.sendMessage(color("&c&lSERVERINFO &8» &cNu ai permisiunea sa folosesti aceasta comanda!"));
            return true;
        }

        plugin.getGui().openMain(player);
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        return Collections.emptyList();
    }

    private String color(String s) {
        return ChatColor.translateAlternateColorCodes('&', s);
    }
}
