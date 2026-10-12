package dev.mobcoins.plugin;

import dev.mobcoins.plugin.commands.MobCoinsCommand;
import dev.mobcoins.plugin.gui.ShopGUI;
import dev.mobcoins.plugin.listeners.MobKillListener;
import dev.mobcoins.plugin.managers.CoinManager;
import org.bukkit.plugin.java.JavaPlugin;

public class MobCoinsPlugin extends JavaPlugin {

    private static MobCoinsPlugin instance;
    private CoinManager coinManager;
    private ShopGUI shopGUI;

    @Override
    public void onEnable() {
        instance = this;
        saveDefaultConfig();

        this.coinManager = new CoinManager(this);
        this.shopGUI     = new ShopGUI(this);

        MobCoinsCommand cmd = new MobCoinsCommand(this);
        getCommand("mobcoins").setExecutor(cmd);
        getCommand("mobcoins").setTabCompleter(cmd);
        getCommand("spawnershop").setExecutor(cmd);
        getCommand("flyshop").setExecutor(cmd);

        getServer().getPluginManager().registerEvents(new MobKillListener(this), this);
        getServer().getPluginManager().registerEvents(shopGUI, this);

        getLogger().info("MobCoinsPlugin pornit! " +
                getConfig().getConfigurationSection("mob-rewards").getKeys(false).size() +
                " mob-uri configurate.");
    }

    @Override
    public void onDisable() {
        if (coinManager != null) coinManager.save();
        getLogger().info("MobCoinsPlugin oprit. Datele salvate.");
    }

    public static MobCoinsPlugin getInstance() { return instance; }
    public CoinManager getCoinManager() { return coinManager; }
    public ShopGUI getShopGUI() { return shopGUI; }
}
