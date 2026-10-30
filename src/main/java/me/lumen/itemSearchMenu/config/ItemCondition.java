package me.lumen.itemSearchMenu.config;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import io.papermc.paper.command.brigadier.argument.ArgumentTypes;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.util.function.Predicate;

public class ItemCondition {
    private final String asString;
    private final Predicate<ItemStack> condition;

    public ItemCondition(String asString, Predicate<ItemStack> condition) {
        this.asString = asString;
        this.condition = condition;
    }

    public ItemCondition(String asString){
        this.asString = asString;
        this.condition = getPredicate(asString);
    }

    private static Predicate<ItemStack> getPredicate(String asString){
        try {
            return ArgumentTypes.itemPredicate().parse(new StringReader(asString));
        } catch (CommandSyntaxException e) {
            throw new IllegalArgumentException(e);
        }
    }

    public String asString() {
        return asString;
    }

    public Predicate<ItemStack> condition() {
        return condition;
    }

    @Contract("_ -> new")
    public static @NotNull ItemCondition from(String asString){
        return new ItemCondition(asString);
    }
}
