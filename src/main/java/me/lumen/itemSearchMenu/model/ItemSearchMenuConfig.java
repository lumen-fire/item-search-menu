package me.lumen.itemSearchMenu.model;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;

public class ItemSearchMenuConfig {
    private final Plugin plugin;
    public ItemSearchMenuConfig(Plugin plugin){
        this.plugin = plugin;
    }

    public @Nullable ItemCategory getCategory(byte slot){
        ConfigurationSection section = getCategoriesSection().getConfigurationSection(Byte.toString(slot));
        if (section == null) return null;
        return ItemCategory.deserialize(section);
    }

    private ConfigurationSection getCategoriesSection(){
        return getOrCreateSection("categories");
    }

    private ConfigurationSection getOrCreateSection(String path){
        ConfigurationSection section = plugin.getConfig().getConfigurationSection(path);
        return Objects.requireNonNullElseGet(section, () -> plugin.getConfig().createSection(path));
    }
}
