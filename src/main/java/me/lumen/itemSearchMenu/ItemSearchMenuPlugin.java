package me.lumen.itemSearchMenu;

import io.papermc.paper.plugin.lifecycle.event.types.LifecycleEvents;
import me.lumen.itemSearchMenu.commands.ItemSearchMenuConfigCommand;
import me.lumen.itemSearchMenu.config.ItemSearchMenuConfig;
import org.bukkit.plugin.java.JavaPlugin;

public final class ItemSearchMenuPlugin extends JavaPlugin {
    private static ItemSearchMenuPlugin instance;

    private final ItemSearchMenuConfig config = new ItemSearchMenuConfig(this);

    @Override
    public void onEnable() {
        instance = this;
        commands();
    }

    public ItemSearchMenuConfig getItemSearchMenuConfig(){
        return config;
    }

    public static ItemSearchMenuPlugin get(){
        return instance;
    }

    private void commands(){
        getLifecycleManager().registerEventHandler(LifecycleEvents.COMMANDS, event ->
                event.registrar().register(ItemSearchMenuConfigCommand.createCommand(), "The main plugin command, used for configuration, etc")
        );
    }
}
