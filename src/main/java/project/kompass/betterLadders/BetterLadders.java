package project.kompass.betterLadders;

import org.bukkit.plugin.java.JavaPlugin;

public final class BetterLadders extends JavaPlugin {

    public void onEnable() {
        // Registering the listener (this class) so the events work
        getServer().getPluginManager().registerEvents(new LadderListener(), this);
        getLogger().info("Get ready to climb!");
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
    }
}
