package me.lumen.itemSearchMenu.commands;

import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import com.mojang.brigadier.tree.LiteralCommandNode;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;
import me.lumen.itemSearchMenu.ItemSearchMenuPlugin;
import me.lumen.itemSearchMenu.commands.args.ItemCategoryArg;
import me.lumen.itemSearchMenu.commands.args.MiniMessageArg;

import java.util.concurrent.CompletableFuture;

public class ItemSearchMenuConfigCommand {
    public static LiteralCommandNode<CommandSourceStack> createCommand(){
        LiteralArgumentBuilder<CommandSourceStack> categoriesNode = Commands.literal("categories")
                .then(Commands.literal("create")
                        .then(Commands.argument("slot", IntegerArgumentType.integer(1, 9))
                                .suggests(ItemSearchMenuConfigCommand::suggestAvailableSlots)
                                .then(Commands.argument("name", MiniMessageArg.argument())

                                )
                        )
                )
                .then(Commands.literal("delete"))
                .then(Commands.literal("edit")
                        .then(Commands.argument("slot", ItemCategoryArg.argument())
                                .then(Commands.literal("name"))
                                .then(Commands.literal("icon"))
                                .then(Commands.literal("conditions")
                                        .then(Commands.literal("add"))
                                        .then(Commands.literal("remove"))
                                        .then(Commands.literal("list"))
                                )
                        )
                )
                .then(Commands.literal("info"))
                .then(Commands.literal("list"));

        LiteralArgumentBuilder<CommandSourceStack> blacklistNode = Commands.literal("blacklist")
                .then(Commands.literal("add"))
                .then(Commands.literal("remove"))
                .then(Commands.literal("list"));

        return Commands.literal("itemsearchmenu")
                .then(Commands.literal("reload"))
                .then(Commands.literal("version"))
                .then(categoriesNode)
                .then(blacklistNode)
                .build();
    }

    private static CompletableFuture<Suggestions> suggestAvailableSlots(CommandContext<?> ignored, SuggestionsBuilder builder){
        for (byte b = 1; b < 9; b++){
            if (ItemSearchMenuPlugin.get().getItemSearchMenuConfig().categoryExists(b)) continue;
            builder.suggest(b);
        }
        return builder.buildFuture();
    }
}
