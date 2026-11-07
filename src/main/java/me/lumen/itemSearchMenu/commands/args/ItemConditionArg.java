package me.lumen.itemSearchMenu.commands.args;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import io.papermc.paper.command.brigadier.argument.ArgumentTypes;
import io.papermc.paper.command.brigadier.argument.CustomArgumentType;
import io.papermc.paper.command.brigadier.argument.predicate.ItemStackPredicate;
import me.lumen.itemSearchMenu.config.ItemCondition;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.function.Predicate;

/**
 * Like the greedy string argument this should ONLY be used at the end of the command.
 * The argument will throw an error if no items pass the condition.
 */
public class ItemConditionArg implements CustomArgumentType<ItemCondition, ItemStackPredicate> {
    private static final ItemConditionArg DEFAULT_ARGUMENT = new ItemConditionArg();

    /**
     * Like the greedy string argument this should ONLY be used at the end of the command.
     * The argument will throw an error if no items pass the condition.
     */
    public static ItemConditionArg argument(){
        return DEFAULT_ARGUMENT;
    }

    public static ItemCondition getItemCondition(@NotNull CommandContext<?> context, String name){
        return context.getArgument(name, ItemCondition.class);
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
