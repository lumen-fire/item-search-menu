package me.lumen.itemSearchMenu.commands.args;

import com.mojang.brigadier.LiteralMessage;
import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import io.papermc.paper.command.brigadier.argument.ArgumentTypes;
import io.papermc.paper.command.brigadier.argument.CustomArgumentType;
import io.papermc.paper.command.brigadier.argument.predicate.ItemStackPredicate;
import me.lumen.itemSearchMenu.config.ItemCondition;
import org.bukkit.Registry;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.function.Predicate;

/**
 * Like the greedy string argument this should ONLY be used at the end of the command.
 * The argument will throw an error if no items pass the condition.
 */
public class ItemTypeConditionArg implements CustomArgumentType<ItemCondition, ItemStackPredicate> {
    private static final ItemTypeConditionArg DEFAULT_ARGUMENT = new ItemTypeConditionArg();

    private static final DynamicCommandExceptionType NO_DEFAULT_ITEMS = new DynamicCommandExceptionType(object ->
            new LiteralMessage("No items can pass " + object + " by default, it will always return no items")
    );

    /**
     * Like the greedy string argument this should ONLY be used at the end of the command.
     * The argument will throw an error if no items pass the condition.
     */
    public static ItemTypeConditionArg argument(){
        return DEFAULT_ARGUMENT;
    }

    public static ItemCondition getItemCondition(@NotNull CommandContext<?> context, String name){
        return context.getArgument(name, ItemCondition.class);
    }

    @Override
    public @NotNull ItemCondition parse(@NotNull StringReader reader) throws CommandSyntaxException {
        String asString = reader.getRemaining();
        Predicate<ItemStack> predicate = getNativeType().parse(reader);
        if (noDefaultItemsPass(predicate)) throw NO_DEFAULT_ITEMS.create(asString);
        return new ItemCondition(asString, predicate);
    }

    @Override
    public @NotNull ArgumentType<ItemStackPredicate> getNativeType() {
        return ArgumentTypes.itemPredicate();
    }

    private static boolean noDefaultItemsPass(Predicate<ItemStack> predicate){
        return Registry.ITEM.stream().noneMatch(itemType -> predicate.test(itemType.createItemStack()));
    }
}
