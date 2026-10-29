package me.lumen.itemSearchMenu.commands.args;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import io.papermc.paper.command.brigadier.argument.ArgumentTypes;
import io.papermc.paper.command.brigadier.argument.CustomArgumentType;
import io.papermc.paper.command.brigadier.argument.predicate.ItemStackPredicate;
import me.lumen.itemSearchMenu.model.ItemCondition;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.function.Predicate;

/**
 * Like the greedy string argument this should ONLY be used at the end of the command
 */
public class ItemConditionArg implements CustomArgumentType<ItemCondition, ItemStackPredicate> {
    private static final ItemConditionArg ARGUMENT = new ItemConditionArg();

    private ItemConditionArg(){}

    /**
     * Like the greedy string argument this should ONLY be used at the end of the command
     */
    public static ItemConditionArg argument(){
        return ARGUMENT;
    }

    @Override
    public @NotNull ItemCondition parse(@NotNull StringReader reader) throws CommandSyntaxException {
        String asString = reader.getRemaining();
        Predicate<ItemStack> predicate = getNativeType().parse(reader);
        return new ItemCondition(asString, predicate);
    }

    @Override
    public @NotNull ArgumentType<ItemStackPredicate> getNativeType() {
        return ArgumentTypes.itemPredicate();
    }
}
