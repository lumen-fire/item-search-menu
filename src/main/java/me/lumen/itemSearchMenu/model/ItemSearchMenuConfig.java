package me.lumen.itemSearchMenu.config;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.plugin.Plugin;

import java.util.Objects;

public class ItemSearchMenuConfig {
    private final Plugin plugin;
    public ItemSearchMenuConfig(Plugin plugin){
        this.plugin = plugin;
    }

    public ItemCategory getCategory(String id){
        ConfigurationSection section = getOrCreateSection(getCategoriesSection(), id);

    }

    private ConfigurationSection getCategoriesSection(){
        return getOrCreateSection("categories");
    }

    private ConfigurationSection getOrCreateSection(String path){
        ConfigurationSection section = plugin.getConfig().getConfigurationSection(path);
        return getOrCreateSection(section, path);
    }

    private ConfigurationSection getOrCreateSection(ConfigurationSection parent, String path){
        return Objects.requireNonNullElseGet(parent, () -> plugin.getConfig().createSection(path));
    }
}
