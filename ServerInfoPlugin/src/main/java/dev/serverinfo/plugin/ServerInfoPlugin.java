package dev.serverinfo.plugin;

import dev.serverinfo.plugin.commands.ServerInfoCommand;
import dev.serverinfo.plugin.gui.ServerInfoGUI;
import org.bukkit.plugin.java.JavaPlugin;

public class ServerInfoPlugin extends JavaPlugin {

    private static ServerInfoPlugin instance;
    private ServerInfoGUI gui;

    @Override
    public void onEnable() {
        instance = this;

        // Salveaza config default daca nu exista
        saveDefaultConfig();

        this.gui = new ServerInfoGUI(this);

        // Inregistreaza comenzile
        ServerInfoCommand cmd = new ServerInfoCommand(this);
        getCommand("meniu").setExecutor(cmd);
        getCommand("meniu").setTabCompleter(cmd);
        getCommand("reload-serverinfo").setExecutor(cmd);

        // Inregistreaza listener pentru GUI
        getServer().getPluginManager().registerEvents(gui, this);

        getLogger().info("ServerInfoPlugin pornit cu succes!");
    }

    @Override
    public void onDisable() {
        getLogger().info("ServerInfoPlugin oprit.");
    }

    public static ServerInfoPlugin getInstance() {
        return instance;
    }

    public ServerInfoGUI getGui() {
        return gui;
    }

    public String c(String path) {
        return org.bukkit.ChatColor.translateAlternateColorCodes('&',
                getConfig().getString(path, ""));
    }
}
