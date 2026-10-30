package me.lumen.itemSearchMenu.commands.args;

import com.mojang.brigadier.LiteralMessage;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import io.papermc.paper.command.brigadier.argument.CustomArgumentType;
import me.lumen.itemSearchMenu.ItemSearchMenuPlugin;
import me.lumen.itemSearchMenu.config.ItemCategory;
import org.jetbrains.annotations.NotNull;

import java.util.concurrent.CompletableFuture;

public class ItemCategoryArg implements CustomArgumentType.Converted<ItemCategory, Integer> {
    private static final DynamicCommandExceptionType CATEGORY_NOT_EXISTS = new DynamicCommandExceptionType(object ->
            new LiteralMessage("Item category " + object + " does not exist!")
    );

    private static final ItemCategoryArg ARGUMENT = new ItemCategoryArg();

    private ItemCategoryArg(){}

    @Override
    public @NotNull ArgumentType<Integer> getNativeType() {
        return IntegerArgumentType.integer(1, 9);
    }

    @Override
    public <S> @NotNull CompletableFuture<Suggestions> listSuggestions(@NotNull CommandContext<S> context, @NotNull SuggestionsBuilder builder) {
        for (ItemCategory category : ItemSearchMenuPlugin.get().getItemSearchMenuConfig().getItemCategories()){
            builder.suggest(category.slot(), category.displayNameAsMessage());
        }
        return builder.buildFuture();
    }

    @Override
    public @NotNull ItemCategory convert(@NotNull Integer nativeType) throws CommandSyntaxException {
        byte slot = (byte) (int) nativeType;
        ItemCategory category = ItemSearchMenuPlugin.get().getItemSearchMenuConfig().getCategory(slot);
        if (category == null) throw CATEGORY_NOT_EXISTS.create(slot);
        return category;
    }

    public static ItemCategoryArg argument(){
        return ARGUMENT;
    }
}
