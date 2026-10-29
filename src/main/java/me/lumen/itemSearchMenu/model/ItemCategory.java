package me.lumen.itemSearchMenu.config;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.json.JSONComponentSerializer;
import org.bukkit.Material;
import org.bukkit.Tag;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.ItemType;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Range;
import org.jetbrains.annotations.Unmodifiable;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class ItemCategory {
    private final List<ItemType> items = new ArrayList<>();
    private @Range(from = 1, to = 9) byte slot;
    private ItemStack icon;
    private Component displayName;

    private ItemCategory(@Range(from = 1, to = 9) byte slot, ItemStack icon, Component displayName){
        this.slot = slot;
        this.icon = icon;
        this.displayName = displayName;
    }

    public void addItem(ItemType item){
        items.add(item);
    }

    public final void addTag(@NotNull Tag<Material> tag) {
        for (Material material : tag.getValues()) {
            ItemType itemType = material.asItemType();
            assert itemType != null;
            items.add(itemType);
        }
    }

    @Contract(pure = true)
    public @NotNull @Unmodifiable List<ItemType> items(){
        return List.copyOf(items);
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
        return Map.of("display-name", JSONComponentSerializer.json().serialize(displayName),"icon", icon, "items", itemsAsString());
    }

    private List<String> itemsAsString(){
        return items.stream().map(itemType -> itemType.getKey().asString()).toList();
    }

    public static ItemCategory deserialize(@NotNull ConfigurationSection section){
        ItemStack icon = section.getItemStack("icon");
        List<String> itemsAsStrings = section.getStringList("items");
        Component displayName = section.getRichMessage("display-name");

        String name = section.getName();
        byte slot = Byte.parseByte(name);

        ItemCategory category = new ItemCategory(slot, icon, displayName);
        for (String string : itemsAsStrings){

        }
        return category;
    }

    public Component displayName() {
        return displayName;
    }

    public void setDisplayName(Component displayName) {
        this.displayName = displayName;
    }
}
