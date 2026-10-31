package me.lumen.itemSearchMenu.config;

import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickCallback;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.minimessage.tag.Tag;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
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

    public ItemCategory createItemCategory(@Range(from = 1, to = 9) byte slot, Component displayName){
        String stringSlot = Byte.toString(slot);
        if (getCategoriesSection().contains(stringSlot)) throw new IllegalArgumentException("Item category for slot " + slot + " already exists");
        ItemCategory category = new ItemCategory(slot, displayName);
        saveCategory(category);
        return category;
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

    void saveCategory(@NotNull ItemCategory category){
        getCategoriesSection().createSection(category.slotAsString(), category.serialize());
        plugin.saveConfig();
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

    private void sendMessage(String key, String def, @NotNull TagResolver resolver, Audience target){
        String raw = getMessagesSection().getString(key, def);
        Component message = MiniMessage.miniMessage().deserialize(raw, target, resolver);
        target.sendMessage(message);
    }

    public void sendCategoryDisplayNameMessage(@NotNull ItemCategory category, Audience target){
        TagResolver resolver = TagResolver.builder()
                .tag("name", Tag.selfClosingInserting(category.displayName()))
                .resolvers(slotReplacer(category))
                .build();
        sendMessage("category.edit.name", "<green>Set name of category with slot <slot> to <name>", resolver, target);
    }

    public void sendCategoryIconMessage(@NotNull ItemCategory category, Audience target){
        TagResolver resolver = categoryReplacer(category).resolvers(iconReplacer(category)).build();
        sendMessage("category.edit.icon", "<green>Set icon of category <category> to <icon>", resolver, target);
    }

    public void sendCategoryCreatedMessage(ItemCategory created, Audience target){
        TagResolver resolver = categoryReplacer(created).resolvers(slotReplacer(created)).build();
        sendMessage("category.create", "<green>Created category <category> in slot <slot>", resolver, target);
    }

    public void sendCategoryDeletedMessage(ItemCategory deleted, Audience target){
        TagResolver resolver = categoryReplacer(deleted).build();
        sendMessage("category.delete", "<yellow>Deleted category <category>", resolver, target);
    }

    public void sendCategoryConditionAddedMessage(ItemCategory category, ItemCondition added, Audience target){
        sendCategoryFilterChangeMessage(category, added, "category.edit.condition.add", "<green>Added item condition <condition> to category <category>", target);
    }

    public void sendCategoryConditionDeletedMessage(ItemCategory category, ItemCondition deleted, Audience target){
        sendCategoryFilterChangeMessage(category, deleted, "category.edit.condition.remove", "<green>Removed item condition <condition> from category <category>", target);
    }

    private void sendCategoryFilterChangeMessage(ItemCategory category, ItemCondition changed, String key, String def, Audience target){
        TagResolver resolver = categoryReplacer(category).resolvers(conditionReplacer(changed).build()).build();
        sendMessage(key, def, resolver, target);
    }

    public void sendCategoryConditionListHeader(ItemCategory category, Audience target){
        sendMessage("category.edit.condition.list-header", "<green><b>Item conditions for category <category>", categoryReplacer(category).build(), target);
    }

    public void sendCategoryInfoHeader(ItemCategory category, Audience target){
        sendMessage("category.info.header", "<green><b><u>Info for category <category>", categoryReplacer(category).build(), target);
    }

    public void sendCategoryInfoSlot(ItemCategory category, Audience target){
        sendMessage("category.info.slot", "<green>Slot: <slot>", slotReplacer(category), target);
    }

    public void sendCategoryInfoIcon(ItemCategory category, Audience target){
        sendMessage("category.info.icon", "<green>Icon: <icon>", iconReplacer(category), target);
    }

    public void sendCategoryInfoFooter(ClickCallback<Audience> click, Audience target){
        TagResolver resolver = TagResolver.builder()
                .tag("showconditions", Tag.styling(builder -> builder.clickEvent(ClickEvent.callback(click))))
                .build();
        String def = "<showconditions><hover:show_text:'<gold>Click to show conditions'><green><i>[Show conditions...]</hover></showconditions>";
        sendMessage("category.info.footer", def, resolver, target);
    }

    public void sendCategoryListHeader(Audience target){
        sendMessage("category.list-header", "<gold><b>Item categories", TagResolver.builder().build(), target);
    }

    private static TagResolver.@NotNull Builder categoryReplacer(@NotNull ItemCategory category){
        return TagResolver.builder().tag("category", Tag.selfClosingInserting(category.displayName()));
    }

    private static TagResolver.@NotNull Builder conditionReplacer(@NotNull ItemCondition condition){
        return TagResolver.builder().tag("condition", Tag.selfClosingInserting(Component.text(condition.asString())));
    }

    private static @NotNull TagResolver slotReplacer(@NotNull ItemCategory category){
        return TagResolver.builder().tag("slot", Tag.selfClosingInserting(Component.text(category.slot(), NamedTextColor.GRAY))).build();
    }

    private static @NotNull TagResolver iconReplacer(@NotNull ItemCategory category){
        return TagResolver.builder().tag("icon", Tag.selfClosingInserting(category.icon().displayName())).build();
    }

    private ConfigurationSection getCategoriesSection(){
        return getOrCreateSection("categories");
    }

    private ConfigurationSection getMessagesSection(){
        return getOrCreateSection("messages");
    }

    private ConfigurationSection getOrCreateSection(String path){
        ConfigurationSection section = plugin.getConfig().getConfigurationSection(path);
        return Objects.requireNonNullElseGet(section, () -> plugin.getConfig().createSection(path));
    }
}
