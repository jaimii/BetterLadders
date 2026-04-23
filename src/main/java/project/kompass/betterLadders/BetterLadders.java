package project.kompass.betterLadders;

import org.bukkit.plugin.java.JavaPlugin;

public class BetterLadders extends JavaPlugin {

    @Override
    public void onEnable() {
        getServer().getPluginManager().registerEvents(new LadderListener(), this);

        getLogger().info("Climbing better!");
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
    }
}
