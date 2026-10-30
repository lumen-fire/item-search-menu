package me.lumen.itemSearchMenu.config;

import net.kyori.adventure.text.Component;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.Range;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.function.BiConsumer;

public class ItemSearchMenuConfig {
    private final Plugin plugin;
    public ItemSearchMenuConfig(Plugin plugin){
        this.plugin = plugin;
    }

    public @Nullable ItemCategory getCategory(@Range(from = 1, to = 9) byte slot){
        ConfigurationSection section = getCategoriesSection().getConfigurationSection(Byte.toString(slot));
        if (section == null) return null;
        return ItemCategory.deserialize(section);
    }

    public void deleteCategory(@NotNull ItemCategory category){
        getCategoriesSection().set(Byte.toString(category.slot()), null);
        plugin.saveConfig();
    }

    public void createItemCategory(@Range(from = 1, to = 9) byte slot, Component displayName){
        String stringSlot = Byte.toString(slot);
        if (getCategoriesSection().contains(stringSlot)) throw new IllegalArgumentException("Item category for slot " + slot + " already exists");
        ItemCategory category = new ItemCategory(slot, displayName);
        getCategoriesSection().createSection(stringSlot, category.serialize());
        plugin.saveConfig();
    }

    public Set<String> getItemCategoryIds(){
        return getCategoriesSection().getKeys(false);
    }

    public boolean categoryExists(@Range(from = 1, to = 9) byte slot){
        return getCategoriesSection().contains(Byte.toString(slot));
    }

    public List<ItemCategory> getItemCategories(){
        List<ItemCategory> categories = new ArrayList<>();
        for (String id : getItemCategoryIds()){
            ItemCategory category = getCategory(Byte.parseByte(id));
            categories.add(category);
        }
        return categories;
    }

    public void addBlacklistCondition(ItemCondition condition){
        blacklistConditionOperation(List::add, condition);
    }

    public void removeBlacklistCondition(ItemCondition condition){
        blacklistConditionOperation(List::remove, condition);
    }

    private void blacklistConditionOperation(@NotNull BiConsumer<List<String>, String> operation, @NotNull ItemCondition condition){
        List<String> blacklist = getBlacklistAsString();
        operation.accept(blacklist, condition.asString());
        plugin.getConfig().set("blacklist", blacklist);
        plugin.saveConfig();
    }

    public List<String> getBlacklistAsString(){
        return plugin.getConfig().getStringList("blacklist");
    }

    public List<ItemCondition> getBlacklist(){
        List<ItemCondition> conditions = new ArrayList<>();
        for (String asString : getBlacklistAsString()){
            ItemCondition condition = new ItemCondition(asString);
            conditions.add(condition);
        }
        return conditions;
    }

    private ConfigurationSection getCategoriesSection(){
        return getOrCreateSection("categories");
    }

    private ConfigurationSection getOrCreateSection(String path){
        ConfigurationSection section = plugin.getConfig().getConfigurationSection(path);
        return Objects.requireNonNullElseGet(section, () -> plugin.getConfig().createSection(path));
    }
}
