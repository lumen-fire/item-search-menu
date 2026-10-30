package me.lumen.itemSearchMenu.config;

import com.mojang.brigadier.Message;
import io.papermc.paper.command.brigadier.MessageComponentSerializer;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.json.JSONComponentSerializer;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Range;
import org.jetbrains.annotations.Unmodifiable;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class ItemCategory {
    private final List<ItemCondition> conditions = new ArrayList<>();
    private @Range(from = 1, to = 9) byte slot;
    private ItemStack icon;
    private Component displayName;

    private ItemCategory(@Range(from = 1, to = 9) byte slot, ItemStack icon, Component displayName){
        this.slot = slot;
        this.icon = icon;
        this.displayName = displayName;
    }

    public ItemCategory(@Range(from = 1, to = 9) byte slot, Component displayName){
        this.slot = slot;
        this.icon = getDefaultIcon();
        this.displayName = displayName;
    }

    public void addItemCondition(ItemCondition condition){
        conditions.add(condition);
    }

    public void removeItemCondition(ItemCondition condition){
        conditions.remove(condition);
    }

    @Contract(pure = true)
    public @NotNull @Unmodifiable List<ItemCondition> filter(){
        return List.copyOf(conditions);
    }

    public void setSlot(@Range(from = 1, to = 9) byte slot){
        this.slot = slot;
    }

    public byte slot() {
        return slot;
    }

    public ItemStack icon(){
        return icon;
    }

    public void setIcon(ItemStack icon) {
        this.icon = icon;
    }

    private @NotNull ItemStack getDefaultIcon(){
        ItemStack itemStack = ItemStack.of(Material.YELLOW_BANNER);
        ItemMeta meta = itemStack.getItemMeta();
        meta.displayName(displayName);
        return itemStack;
    }

    public @NotNull Map<String, Object> serialize() {
        return Map.of("display-name", JSONComponentSerializer.json().serialize(displayName),"icon", icon, "filter", conditionsAsString());
    }

    private List<String> conditionsAsString(){
        return conditions.stream().map(ItemCondition::asString).toList();
    }

    public static @NotNull ItemCategory deserialize(@NotNull ConfigurationSection section){
        ItemStack icon = section.getItemStack("icon");
        List<String> filter = section.getStringList("filter");
        Component displayName = section.getRichMessage("display-name");

        String name = section.getName();
        byte slot = Byte.parseByte(name);

        ItemCategory category = new ItemCategory(slot, icon, displayName);
        for (String asString : filter){
            ItemCondition condition = ItemCondition.from(asString);
            category.addItemCondition(condition);
        }
        return category;
    }

    public Component displayName() {
        return displayName;
    }

    public Message displayNameAsMessage(){
        return MessageComponentSerializer.message().serialize(displayName);
    }

    public void setDisplayName(Component displayName) {
        this.displayName = displayName;
    }
}
