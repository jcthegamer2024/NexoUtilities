package org.sylvarion.nexoutilities;

import io.papermc.paper.plugin.lifecycle.event.types.LifecycleEvents;
import org.bukkit.plugin.java.JavaPlugin;

public final class NexoUtilities extends JavaPlugin {
    @Override
    public void onEnable() {
        saveDefaultConfig();
        try {
            new DirectionalBlocks(this).start();
        } catch (Exception | LinkageError exception) {
            getLogger().log(java.util.logging.Level.SEVERE, "Could not initialize directional block module", exception);
        }
        getLifecycleManager().registerEventHandler(LifecycleEvents.COMMANDS, event ->
                event.registrar().register("nexoutils", "View Nexo custom block slot usage", new BlockAllocationsCommand(this)));
    }
}
