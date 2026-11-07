package me.lumen.itemSearchMenu.gui;

import me.lumen.itemSearchMenu.ItemSearchMenuBootstrap;
import me.lumen.itemSearchMenu.ItemSearchMenuPlugin;
import me.lumen.itemSearchMenu.config.ItemCategory;
import me.lumen.itemSearchMenu.config.ItemCondition;
import me.lumen.itemSearchMenu.config.ItemSearchMenuConfig;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Registry;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.*;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.function.Predicate;

public class ItemSearchMenu implements InventoryHolder {
    private static final ItemStack GREY_STAINED_GLASS = getGreyStainedGlass();
    private static final ItemStack BACK_BUTTON = getButton('←');
    private static final ItemStack NEXT_BUTTON = getButton('→');
    private static final ItemStack ALL_BUTTON = getButton(Component.text("All items", NamedTextColor.GREEN), Material.SHULKER_BOX);
    private static final ItemStack DELETE_ITEM_BUTTON = getButton(Component.text("Delete item", NamedTextColor.RED), Material.BARRIER);
    private static final ItemStack SEARCH_BUTTON = getButton(Component.text("Search", NamedTextColor.LIGHT_PURPLE), Material.SPYGLASS);

    private static List<ItemStack> sortedItems;

    private final Inventory inventory;
    private int page = 0;
    private int pages;
    private final ItemStack[][] items;

    private ItemSearchMenu(Predicate<ItemStack> condition, Component title){
        this.inventory = Bukkit.createInventory(this, 54, title);

        List<ItemStack> filtered = new ArrayList<>();
        for (ItemStack item : sortedItems){
            if (condition.test(item)){
                filtered.add(item);
            }
        }
        pages = (int) Math.ceil(filtered.size() / 36D);
        if (pages == 0) pages = 1;
        items = new ItemStack[pages][36];

        int page = 0;
        int slot = 0;
        for (ItemStack item : filtered){
            if (slot == 36){
                slot = 0;
                page++;
            }
            items[page][slot] = item;
            slot++;
        }
    }

    @Override
    public @NotNull Inventory getInventory() {
        return inventory;
    }

    private void show(Player viewer){
        for (int i = 0; i < 54; i++){
            inventory.setItem(i, GREY_STAINED_GLASS);
        }

        for (ItemCategory category : ItemSearchMenuPlugin.get().getItemSearchMenuConfig().getItemCategories()){
            assert category.slot() < 10;
            inventory.setItem(category.slot() - 1, category.icon());
        }

        inventory.setItem(45, BACK_BUTTON);
        inventory.setItem(46, SEARCH_BUTTON);
        inventory.setItem(52, DELETE_ITEM_BUTTON);
        inventory.setItem(53, NEXT_BUTTON);
        inventory.setItem(49, ALL_BUTTON);

        updatePage();

        viewer.openInventory(inventory);
    }

    private void updatePage(){
        for (int i = 0; i < 36; i++){
            ItemStack item = items[page][i];
            if (item == null) item = ItemStack.of(Material.AIR);
            inventory.setItem(i + 9, item);
        }
    }

    public void click(@NotNull InventoryClickEvent event){
        int slot = event.getRawSlot();
        Player player = (Player) event.getWhoClicked();

        //categories
        if (slot < 9){
            ItemSearchMenuConfig config = ItemSearchMenuPlugin.get().getItemSearchMenuConfig();
            ItemCategory category = config.getCategory((byte) (slot + 1));
            if (category != null){
                open(player, category);
                return;
            }
            return;
        }

        //buttons
        if (slot > 44) {
            switch (slot) {
                //back
                case 45 -> {
                    player.playSound(player.getLocation(), Sound.ITEM_BOOK_PAGE_TURN, 1, 1);
                    if (page == 0) return;
                    page--;
                    updatePage();
                }
                //search
                case 46 -> player.showDialog(ItemSearchMenuBootstrap.getDialog());
                //all items
                case 49 -> open(player);
                //delete item
                case 52 -> player.setItemOnCursor(null);
                //next
                case 53 -> {
                    player.playSound(player.getLocation(), Sound.ITEM_BOOK_PAGE_TURN, 1, 1);
                    if (page == pages - 1) return;
                    page++;
                    updatePage();
                }
            }
            return;
        }

        //items
        ItemStack item = items[page][slot - 9];
        if (item == null) return;
        ItemStack cursor = player.getItemOnCursor();
        if (cursor.asOne().equals(item)){
            player.setItemOnCursor(cursor.add());
        } else {
            player.setItemOnCursor(item);
        }
    }

    public static void open(Player viewer){
        ItemSearchMenuConfig config = ItemSearchMenuPlugin.get().getItemSearchMenuConfig();
        List<ItemCondition> blacklist = config.getBlacklist();
        ItemSearchMenu menu = new ItemSearchMenu(itemStack -> isNotBlacklisted(itemStack, blacklist), Component.text("Item Search Menu", NamedTextColor.LIGHT_PURPLE));
        menu.show(viewer);
    }

    public static void open(Player viewer, @NotNull ItemCategory category){
        List<ItemCondition> blacklist = ItemSearchMenuPlugin.get().getItemSearchMenuConfig().getBlacklist();
        List<ItemCondition> filter = category.filter();
        ItemSearchMenu menu = new ItemSearchMenu(item -> isNotBlacklisted(item, blacklist) && anyMatch(item, filter), category.displayName());
        menu.show(viewer);
    }

    public static void open(Player viewer, String searchTerm){
        List<ItemCondition> blacklist = ItemSearchMenuPlugin.get().getItemSearchMenuConfig().getBlacklist();
        ItemSearchMenu menu = new ItemSearchMenu(item -> isNotBlacklisted(item, blacklist) && matches(item, searchTerm), Component.text(searchTerm + " \uD83D\uDD0E", NamedTextColor.LIGHT_PURPLE));
        menu.show(viewer);
    }

    public static void reloadItemSorting(){
        sortedItems = getSortedItems();
    }

    private static boolean isNotBlacklisted(ItemStack item, @NotNull List<ItemCondition> blacklist){
        return !anyMatch(item, blacklist);
    }

    private static boolean anyMatch(ItemStack item, @NotNull List<ItemCondition> conditions){
        for (ItemCondition condition : conditions){
            if (condition.test(item)) return true;
        }
        return false;
    }

    private static boolean matches(@NotNull ItemStack item, @NotNull String searchTerm){
        return PlainTextComponentSerializer.plainText().serialize(item.displayName()).toLowerCase().contains(searchTerm.toLowerCase());
    }

    private static @NotNull ItemStack getGreyStainedGlass(){
        ItemStack item = ItemStack.of(Material.GRAY_STAINED_GLASS_PANE);
        ItemMeta meta = item.getItemMeta();
        meta.setHideTooltip(true);
        item.setItemMeta(meta);
        return item;
    }

    private static @NotNull ItemStack getButton(char icon){
        return getButton(Component.text(icon, NamedTextColor.YELLOW), Material.PAPER);
    }

    private static @NotNull ItemStack getButton(@NotNull Component name, Material material){
        ItemStack item = ItemStack.of(material);
        ItemMeta meta = item.getItemMeta();
        meta.displayName(name.decoration(TextDecoration.ITALIC, false));
        item.setItemMeta(meta);
        return item;
    }

    private static @NotNull List<ItemStack> getSortedItems(){
        List<ItemStack> items = new ArrayList<>();
        for (ItemType type : Registry.ITEM){
            items.add(type.createItemStack());
        }
        items.addAll(ItemSearchMenuPlugin.get().getItemSearchMenuConfig().getExtraItems());
        items.sort(Comparator.comparing(itemStack -> PlainTextComponentSerializer.plainText().serialize(itemStack.displayName())));
        return items;
    }
}
