package dev.kits.plugin;

import dev.kits.plugin.commands.KitsCommand;
import dev.kits.plugin.gui.KitsGUI;
import dev.kits.plugin.managers.KitManager;
import org.bukkit.plugin.java.JavaPlugin;

public class KitsPlugin extends JavaPlugin {

    private static KitsPlugin instance;
    private KitManager kitManager;
    private KitsGUI kitsGUI;

    @Override
    public void onEnable() {
        instance = this;

        saveDefaultConfig();

        this.kitManager = new KitManager(this);
        this.kitsGUI = new KitsGUI(this);

        KitsCommand cmd = new KitsCommand(this);
        getCommand("kit").setExecutor(cmd);
        getCommand("kit").setTabCompleter(cmd);

        getServer().getPluginManager().registerEvents(kitsGUI, this);

        getLogger().info("KitsPlugin pornit! " + kitManager.getKits().size() + " kituri incarcate.");
    }

    @Override
    public void onDisable() {
        getLogger().info("KitsPlugin oprit.");
    }

    public static KitsPlugin getInstance() { return instance; }
    public KitManager getKitManager() { return kitManager; }
    public KitsGUI getKitsGUI() { return kitsGUI; }
}
