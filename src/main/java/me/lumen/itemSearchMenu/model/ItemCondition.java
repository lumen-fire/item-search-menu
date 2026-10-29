package me.lumen.itemSearchMenu.config;

import org.bukkit.inventory.ItemStack;

import java.util.function.Predicate;

public class ItemCondition {
    private final String asString;
    private final Predicate<ItemStack> condition;

    public ItemCondition(String asString, Predicate<ItemStack> condition) {
        this.asString = asString;
        this.condition = condition;
    }
}
