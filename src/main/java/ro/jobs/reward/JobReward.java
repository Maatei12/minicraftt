package ro.jobs.reward;

import org.bukkit.Material;

import java.util.List;

/**
 * Reprezinta un reward la un anumit nivel de job.
 */
public class JobReward {

    private final int level;
    private final String displayName;      // ex: "Miner Niv. 10"
    private final double money;            // bani din economy
    private final List<RewardItem> items;  // iteme date jucatorului
    private final List<String> commands;   // comenzi rulate ca console
    private final String fireworkColor;    // culoare foc artificii (hex sau nume)

    public JobReward(int level, String displayName, double money,
                     List<RewardItem> items, List<String> commands, String fireworkColor) {
        this.level = level;
        this.displayName = displayName;
        this.money = money;
        this.items = items;
        this.commands = commands;
        this.fireworkColor = fireworkColor;
    }

    public int getLevel()              { return level; }
    public String getDisplayName()     { return displayName; }
    public double getMoney()           { return money; }
    public List<RewardItem> getItems() { return items; }
    public List<String> getCommands()  { return commands; }
    public String getFireworkColor()   { return fireworkColor; }

    // ─── Item simplu in reward ───────────────────────────
    public static class RewardItem {
        private final Material material;
        private final int amount;
        private final String displayName; // null = default

        public RewardItem(Material material, int amount, String displayName) {
            this.material = material;
            this.amount = amount;
            this.displayName = displayName;
        }

        public Material getMaterial() { return material; }
        public int getAmount()        { return amount; }
        public String getDisplayName(){ return displayName; }
    }
}
