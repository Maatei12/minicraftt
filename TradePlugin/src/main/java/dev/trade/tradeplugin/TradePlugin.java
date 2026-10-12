package dev.trade.tradeplugin;

import dev.trade.tradeplugin.commands.TradeCommand;
import dev.trade.tradeplugin.gui.TradeGUI;
import dev.trade.tradeplugin.managers.TradeManager;
import dev.trade.tradeplugin.models.TradeSession;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.plugin.java.JavaPlugin;

public class TradePlugin extends JavaPlugin implements Listener {

    private static TradePlugin instance;
    private TradeManager tradeManager;
    private TradeGUI tradeGUI;

    @Override
    public void onEnable() {
        instance = this;
        this.tradeManager = new TradeManager(this);
        this.tradeGUI = new TradeGUI();

        TradeCommand tradeCommand = new TradeCommand(this);
        getCommand("trade").setExecutor(tradeCommand);
        getCommand("trade").setTabCompleter(tradeCommand);

        // Inregistreaza event listeners
        getServer().getPluginManager().registerEvents(tradeGUI, this);
        getServer().getPluginManager().registerEvents(this, this);

        getLogger().info("TradePlugin a pornit cu succes!");
    }

    @Override
    public void onDisable() {
        if (tradeManager != null) {
            tradeManager.cancelAllTrades();
        }
        TradeGUI.closeAll();
        getLogger().info("TradePlugin s-a oprit.");
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        TradeSession session = tradeManager.getSession(player);
        if (session != null) {
            tradeManager.cancelTrade(session, "&cUn jucator a parasit server-ul. Trade anulat.");
        }
    }

    public static TradePlugin getInstance() {
        return instance;
    }

    public TradeManager getTradeManager() {
        return tradeManager;
    }
}
