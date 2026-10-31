package me.lumen.itemSearchMenu.commands;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.Dynamic2CommandExceptionType;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import com.mojang.brigadier.tree.LiteralCommandNode;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;
import io.papermc.paper.command.brigadier.MessageComponentSerializer;
import io.papermc.paper.command.brigadier.argument.ArgumentTypes;
import me.lumen.itemSearchMenu.ItemSearchMenuPlugin;
import me.lumen.itemSearchMenu.commands.args.ItemCategoryArg;
import me.lumen.itemSearchMenu.commands.args.ItemTypeConditionArg;
import me.lumen.itemSearchMenu.commands.args.MiniMessageArg;
import me.lumen.itemSearchMenu.config.ItemCategory;
import me.lumen.itemSearchMenu.config.ItemCondition;
import me.lumen.itemSearchMenu.config.ItemSearchMenuConfig;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.command.CommandSender;
import org.bukkit.inventory.ItemStack;

import java.util.List;
import java.util.concurrent.CompletableFuture;

public class ItemSearchMenuConfigCommand {
    private static final Dynamic2CommandExceptionType ITEM_CONDITION_ALREADY_EXISTS = new Dynamic2CommandExceptionType((a, b) ->
            MessageComponentSerializer.message().serialize(Component.text("Item condition " + a + " already exists in category ").append((Component) b))
    );

    private static final Dynamic2CommandExceptionType ITEM_CONDITION_NOT_EXISTS = new Dynamic2CommandExceptionType((a, b) ->
            MessageComponentSerializer.message().serialize(Component.text("Item condition " + a + " does not exist in category ").append((Component) b))
    );



    public static LiteralCommandNode<CommandSourceStack> createCommand(){
        LiteralArgumentBuilder<CommandSourceStack> categoriesNode = Commands.literal("categories")
                .then(Commands.literal("create")
                        .then(Commands.argument("slot", IntegerArgumentType.integer(1, 9))
                                .suggests(ItemSearchMenuConfigCommand::suggestAvailableSlots)
                                .then(Commands.argument("name", MiniMessageArg.argument())
                                        .executes(context -> {
                                            byte slot = (byte) IntegerArgumentType.getInteger(context, "slot");
                                            Component name = MiniMessageArg.getMessage(context, "name");
                                            ItemSearchMenuConfig config = ItemSearchMenuPlugin.get().getItemSearchMenuConfig();
                                            ItemCategory category = config.createItemCategory(slot, name);
                                            config.sendCategoryCreatedMessage(category, context.getSource().getSender());
                                            return Command.SINGLE_SUCCESS;
                                        })
                                )
                        )
                )
                .then(Commands.literal("delete")
                        .then(Commands.argument("slot", ItemCategoryArg.argument())
                                .executes(context -> {
                                    ItemCategory category = ItemCategoryArg.getItemCategory(context, "slot");
                                    ItemSearchMenuConfig config = ItemSearchMenuPlugin.get().getItemSearchMenuConfig();
                                    config.deleteCategory(category);
                                    config.sendCategoryDeletedMessage(category, context.getSource().getSender());
                                    return Command.SINGLE_SUCCESS;
                                })
                        )
                )
                .then(Commands.literal("edit")
                        .then(Commands.argument("slot", ItemCategoryArg.argument())
                                .then(Commands.literal("name")
                                        .then(Commands.argument("minimessage", MiniMessageArg.argument())
                                                .executes(context -> {
                                                    ItemCategory category = ItemCategoryArg.getItemCategory(context, "slot");
                                                    Component name = MiniMessageArg.getMessage(context, "minimessage");
                                                    category.setDisplayName(name);
                                                    ItemSearchMenuPlugin.get().getItemSearchMenuConfig().sendCategoryDisplayNameMessage(category, context.getSource().getSender());
                                                    return Command.SINGLE_SUCCESS;
                                                })
                                        )
                                )
                                .then(Commands.literal("icon")
                                        .then(Commands.argument("item", ArgumentTypes.itemStack())
                                                .executes(context -> {
                                                    ItemCategory category = ItemCategoryArg.getItemCategory(context, "slot");
                                                    ItemStack item = context.getArgument("item", ItemStack.class);
                                                    category.setIcon(item);
                                                    ItemSearchMenuPlugin.get().getItemSearchMenuConfig().sendCategoryIconMessage(category, context.getSource().getSender());
                                                    return Command.SINGLE_SUCCESS;
                                                })
                                        )
                                )
                                .then(Commands.literal("conditions")
                                        .then(Commands.literal("add")
                                                .then(Commands.argument("item-condition", ItemTypeConditionArg.argument())
                                                        .executes(context -> {
                                                            ItemCategory category = ItemCategoryArg.getItemCategory(context, "slot");
                                                            ItemCondition condition = ItemTypeConditionArg.getItemCondition(context, "item-condition");
                                                            if (category.hasItemCondition(condition)) {
                                                                throw ITEM_CONDITION_ALREADY_EXISTS.create(condition.asString(), category.displayName());
                                                            }
                                                            category.addItemCondition(condition);
                                                            CommandSender sender = context.getSource().getSender();
                                                            ItemSearchMenuPlugin.get().getItemSearchMenuConfig().sendCategoryConditionAddedMessage(category, condition, sender);
                                                            return Command.SINGLE_SUCCESS;
                                                        })
                                                )
                                        )
                                        .then(Commands.literal("remove")
                                                .then(Commands.argument("item-condition", ItemTypeConditionArg.argument())
                                                        .suggests((context, builder) -> {
                                                            ItemCategory category = ItemCategoryArg.getItemCategory(context, "slot");
                                                            String remaining = builder.getRemaining();
                                                            for (ItemCondition condition : category.filter()){
                                                                if (!condition.asString().startsWith(remaining)) continue;
                                                                builder.suggest(condition.asString());
                                                            }
                                                            return builder.buildFuture();
                                                        })
                                                        .executes(context -> {
                                                            ItemCategory category = ItemCategoryArg.getItemCategory(context, "slot");
                                                            ItemCondition condition = ItemTypeConditionArg.getItemCondition(context, "item-condition");
                                                            if (!category.hasItemCondition(condition)) {
                                                                throw ITEM_CONDITION_NOT_EXISTS.create(condition.asString(), category.displayName());
                                                            }
                                                            category.removeItemCondition(condition);
                                                            CommandSender sender = context.getSource().getSender();
                                                            ItemSearchMenuPlugin.get().getItemSearchMenuConfig().sendCategoryConditionDeletedMessage(category, condition, sender);
                                                            return Command.SINGLE_SUCCESS;
                                                        })
                                                )
                                        )
                                        .then(Commands.literal("list")
                                                .executes(context -> {
                                                    ItemCategory category = ItemCategoryArg.getItemCategory(context, "slot");
                                                    CommandSender sender = context.getSource().getSender();
                                                    listCategoryConditions(category, sender);
                                                    return category.filter().size();
                                                })
                                        )
                                )
                        )
                )
                .then(Commands.literal("info")
                        .then(Commands.argument("slot", ItemCategoryArg.argument())
                                .executes(context -> {
                                    ItemCategory category = ItemCategoryArg.getItemCategory(context, "slot");
                                    CommandSender sender = context.getSource().getSender();
                                    ItemSearchMenuConfig config = ItemSearchMenuPlugin.get().getItemSearchMenuConfig();

                                    config.sendCategoryInfoHeader(category, sender);
                                    config.sendCategoryInfoSlot(category, sender);
                                    config.sendCategoryInfoIcon(category, sender);
                                    config.sendCategoryInfoFooter(audience -> listCategoryConditions(category, audience), sender);

                                    return Command.SINGLE_SUCCESS;
                                })
                        )
                )
                .then(Commands.literal("list")
                        .executes(context -> {
                            CommandSender sender = context.getSource().getSender();
                            ItemSearchMenuConfig config = ItemSearchMenuPlugin.get().getItemSearchMenuConfig();
                            config.sendCategoryListHeader(sender);
                            List<ItemCategory> categories = config.getItemCategories();
                            for (ItemCategory category : categories){
                                Component line = Component.text(" - ", NamedTextColor.GOLD)
                                        .append(category.displayName());
                                sender.sendMessage(line);
                            }
                            return categories.size();
                        })
                );

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

    private static void listCategoryConditions(ItemCategory category, Audience audience){
        ItemSearchMenuPlugin.get().getItemSearchMenuConfig().sendCategoryConditionListHeader(category, audience);
        for (ItemCondition condition : category.filter()){
            Component line = Component.text(" - " + condition, NamedTextColor.GREEN);
            audience.sendMessage(line);
        }
    }
}
