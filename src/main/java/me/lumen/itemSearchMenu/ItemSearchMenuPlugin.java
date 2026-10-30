package me.lumen.itemSearchMenu;

import me.lumen.itemSearchMenu.config.ItemSearchMenuConfig;
import org.bukkit.plugin.java.JavaPlugin;

public final class ItemSearchMenuPlugin extends JavaPlugin {
    private static ItemSearchMenuPlugin instance;

    private final ItemSearchMenuConfig config = new ItemSearchMenuConfig(this);

    @Override
    public void onEnable() {
        instance = this;
    }

    public ItemSearchMenuConfig getItemSearchMenuConfig(){
        return config;
    }

    public static ItemSearchMenuPlugin get(){
        return instance;
    }
}
