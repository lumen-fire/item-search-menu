package me.lumen.itemSearchMenu.commands;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.LiteralMessage;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.Dynamic2CommandExceptionType;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
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
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jspecify.annotations.NonNull;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.function.BiConsumer;
import java.util.function.BiPredicate;
import java.util.function.Predicate;

public class ItemSearchMenuConfigCommand {
    //errors
    private static final DynamicCommandExceptionType CATEGORY_ALREADY_EXISTS = new DynamicCommandExceptionType(object ->
            new LiteralMessage("A category already exists in slot " + object)
    );

    private static final Dynamic2CommandExceptionType ITEM_CONDITION_ALREADY_EXISTS = new Dynamic2CommandExceptionType((a, b) ->
            MessageComponentSerializer.message().serialize(Component.text("Item condition " + a + " already exists in category ").append((Component) b))
    );

    private static final Dynamic2CommandExceptionType ITEM_CONDITION_NOT_EXISTS = new Dynamic2CommandExceptionType((a, b) ->
            MessageComponentSerializer.message().serialize(Component.text("Item condition " + a + " does not exist in category ").append((Component) b))
    );

    private static final DynamicCommandExceptionType ALREADY_IN_BLACKLIST = new DynamicCommandExceptionType(o ->
            new LiteralMessage("Item condition " + o + " is already in the blacklist")
    );

    private static final DynamicCommandExceptionType NOT_IN_BLACKLIST = new DynamicCommandExceptionType(o ->
            new LiteralMessage("Item condition " + o + " is not in the blacklist")
    );

    private static final DynamicCommandExceptionType ALREADY_AN_EXTRA_ITEM = new DynamicCommandExceptionType(o ->
            MessageComponentSerializer.message().serialize(Component.empty().append((Component) o).append(Component.text(" is already an extra item")))
    );

    private static final DynamicCommandExceptionType NOT_AN_EXTRA_ITEM = new DynamicCommandExceptionType(o ->
            MessageComponentSerializer.message().serialize(Component.empty().append((Component) o).append(Component.text(" is not an extra item")))
    );


    //perms
    private static final Predicate<CommandSourceStack> CATEGORY_CREATE = hasPermission("item-search-menu.plugin-command.category.create");
    private static final Predicate<CommandSourceStack> CATEGORY_DELETE = hasPermission("item-search-menu.plugin-command.category.delete");

    private static final Predicate<CommandSourceStack> CATEGORY_EDIT_NAME = hasPermission("item-search-menu.plugin-command.category.edit.name");
    private static final Predicate<CommandSourceStack> CATEGORY_EDIT_ICON = hasPermission("item-search-menu.plugin-command.category.edit.icon");

    private static final Predicate<CommandSourceStack> CATEGORY_EDIT_CONDITION_ADD = hasPermission("item-search-menu.plugin-command.category.edit.condition.add");
    private static final Predicate<CommandSourceStack> CATEGORY_EDIT_CONDITION_REMOVE = hasPermission("item-search-menu.plugin-command.category.edit.condition.remove");
    private static final Predicate<CommandSourceStack> CATEGORY_EDIT_CONDITION_LIST = hasPermission("item-search-menu.plugin-command.category.edit.condition.list");

    private static final Predicate<CommandSourceStack> CATEGORY_EDIT_CONDITION = CATEGORY_EDIT_CONDITION_ADD.or(CATEGORY_EDIT_CONDITION_REMOVE).or(CATEGORY_EDIT_CONDITION_LIST);
    private static final Predicate<CommandSourceStack> CATEGORY_EDIT = CATEGORY_EDIT_CONDITION.or(CATEGORY_EDIT_NAME).or(CATEGORY_EDIT_ICON);

    private static final Predicate<CommandSourceStack> CATEGORY_INFO = hasPermission("item-search-menu.plugin-command.category.info");
    private static final Predicate<CommandSourceStack> CATEGORY_LIST = hasPermission("item-search-menu.plugin-command.category.list");

    private static final Predicate<CommandSourceStack> CATEGORY = CATEGORY_CREATE.or(CATEGORY_DELETE).or(CATEGORY_EDIT).or(CATEGORY_INFO).or(CATEGORY_LIST);

    private static final Predicate<CommandSourceStack> BLACKLIST_ADD = hasPermission("item-search-menu.plugin-command.blacklist.add");
    private static final Predicate<CommandSourceStack> BLACKLIST_REMOVE = hasPermission("item-search-menu.plugin-command.blacklist.remove");
    private static final Predicate<CommandSourceStack> BLACKLIST_LIST = hasPermission("item-search-menu.plugin-command.blacklist.list");

    private static final Predicate<CommandSourceStack> BLACKLIST = BLACKLIST_ADD.or(BLACKLIST_REMOVE).or(BLACKLIST_LIST);

    private static final Predicate<CommandSourceStack> EXTRA_ITEMS_ADD = hasPermission("item-search-menu.plugin-command.extra-items.add");
    private static final Predicate<CommandSourceStack> EXTRA_ITEMS_REMOVE = hasPermission("item-search-menu.plugin-command.extra-items.remove");
    private static final Predicate<CommandSourceStack> EXTRA_ITEMS_LIST = hasPermission("item-search-menu.plugin-command.extra-items.list");

    private static final Predicate<CommandSourceStack> EXTRA_ITEMS = EXTRA_ITEMS_ADD.or(EXTRA_ITEMS_REMOVE).or(EXTRA_ITEMS_LIST);

    private static final Predicate<CommandSourceStack> VERSION = hasPermission("item-search-menu.plugin-command.version");
    private static final Predicate<CommandSourceStack> RELOAD = hasPermission("item-search-menu.plugin-command.reload");

    private static final Predicate<CommandSourceStack> COMMAND = CATEGORY.or(EXTRA_ITEMS).or(BLACKLIST).or(VERSION).or(RELOAD);


    public static LiteralCommandNode<CommandSourceStack> createCommand(){
        LiteralArgumentBuilder<CommandSourceStack> categoriesNode = Commands.literal("categories")
                .requires(CATEGORY)
                .then(Commands.literal("create")
                        .requires(CATEGORY_CREATE)
                        .then(Commands.argument("slot", IntegerArgumentType.integer(1, 9))
                                .suggests(ItemSearchMenuConfigCommand::suggestAvailableSlots)
                                .then(Commands.argument("name", MiniMessageArg.argument())
                                        .executes(context -> {
                                            byte slot = (byte) IntegerArgumentType.getInteger(context, "slot");
                                            Component name = MiniMessageArg.getMessage(context, "name");
                                            ItemSearchMenuConfig config = ItemSearchMenuPlugin.get().getItemSearchMenuConfig();
                                            if (config.categoryExists(slot)) throw CATEGORY_ALREADY_EXISTS.create(slot);
                                            ItemCategory category = config.createItemCategory(slot, name);
                                            config.sendCategoryCreatedMessage(category, context.getSource().getSender());
                                            return Command.SINGLE_SUCCESS;
                                        })
                                )
                        )
                )
                .then(Commands.literal("delete")
                        .requires(CATEGORY_DELETE)
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
                        .requires(CATEGORY_EDIT)
                        .then(Commands.argument("slot", ItemCategoryArg.argument())
                                .then(Commands.literal("name")
                                        .requires(CATEGORY_EDIT_NAME)
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
                                        .requires(CATEGORY_EDIT_ICON)
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
                                        .requires(CATEGORY_EDIT_CONDITION)
                                        .then(Commands.literal("add")
                                                .requires(CATEGORY_EDIT_CONDITION_ADD)
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
                                                .requires(CATEGORY_EDIT_CONDITION_REMOVE)
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
                                                .requires(CATEGORY_EDIT_CONDITION_LIST)
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
                        .requires(CATEGORY_INFO)
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
                        .requires(CATEGORY_LIST)
                        .executes(context -> {
                            CommandSender sender = context.getSource().getSender();
                            ItemSearchMenuConfig config = ItemSearchMenuPlugin.get().getItemSearchMenuConfig();
                            config.sendCategoryListHeader(sender);
                            List<ItemCategory> categories = config.getItemCategories();
                            for (ItemCategory category : categories){
                                Component line = Component.text(" • ", NamedTextColor.GOLD)
                                        .append(category.displayName());
                                sender.sendMessage(line);
                            }
                            return categories.size();
                        })
                );

        LiteralArgumentBuilder<CommandSourceStack> blacklistNode = Commands.literal("blacklist")
                .requires(BLACKLIST)
                .then(Commands.literal("add")
                        .requires(BLACKLIST_ADD)
                        .then(Commands.argument("item-condition", ItemTypeConditionArg.argument())
                                .executes(context ->
                                        blacklistOperation(context, (config, condition) -> !config.isBlacklisted(condition), ALREADY_IN_BLACKLIST, ItemSearchMenuConfig::addBlacklistCondition, ItemSearchMenuConfig::sendBlacklistConditionAdded)
                                )
                        )
                )
                .then(Commands.literal("remove")
                        .requires(BLACKLIST_REMOVE)
                        .then(Commands.argument("item-condition", ItemTypeConditionArg.argument())
                                .suggests((context, builder) -> {
                                    ItemSearchMenuPlugin.get().getItemSearchMenuConfig().getBlacklistAsString().forEach(builder::suggest);
                                    return builder.buildFuture();
                                })
                                .executes(context ->
                                        blacklistOperation(context, ItemSearchMenuConfig::isBlacklisted, NOT_IN_BLACKLIST, ItemSearchMenuConfig::removeBlacklistCondition, ItemSearchMenuConfig::sendBlacklistConditionRemoved)
                                )
                        )
                )
                .then(Commands.literal("list")
                        .requires(BLACKLIST_LIST)
                        .executes(context -> {
                            CommandSender sender = context.getSource().getSender();
                            ItemSearchMenuConfig config = ItemSearchMenuPlugin.get().getItemSearchMenuConfig();
                            List<String> blacklist = config.getBlacklistAsString();
                            config.sendBlacklistListHeader(sender);
                            for (String condition : blacklist){
                                sender.sendMessage(Component.text(" • " + condition, NamedTextColor.GREEN));
                            }
                            return blacklist.size();
                        })
                );

        LiteralArgumentBuilder<CommandSourceStack> extraItemsNode = Commands.literal("extra-items")
                .requires(EXTRA_ITEMS)
                .then(Commands.literal("add")
                        .requires(EXTRA_ITEMS_ADD)
                        .then(Commands.argument("item", ArgumentTypes.itemStack())
                                .executes(context -> {
                                    ItemStack item = context.getArgument("item", ItemStack.class);
                                    ItemSearchMenuConfig config = ItemSearchMenuPlugin.get().getItemSearchMenuConfig();
                                    if (config.getExtraItems().contains(item)) throw ALREADY_AN_EXTRA_ITEM.create(item.displayName());
                                    config.addExtraItem(item);
                                    config.sendExtraItemAdded(context.getSource().getSender(), item);
                                    return Command.SINGLE_SUCCESS;
                                })
                        )
                )
                .then(Commands.literal("remove")
                        .requires(EXTRA_ITEMS_REMOVE)
                        .then(Commands.argument("item", ArgumentTypes.itemStack())
                                .suggests((context, builder) -> {
                                    for (ItemStack item : ItemSearchMenuPlugin.get().getItemSearchMenuConfig().getExtraItems()){
                                        builder.suggest(item.getType().key().asString() + item.getItemMeta().getAsComponentString());
                                    }
                                    return builder.buildFuture();
                                })
                                .executes(context -> {
                                    ItemStack item = context.getArgument("item", ItemStack.class);
                                    ItemSearchMenuConfig config = ItemSearchMenuPlugin.get().getItemSearchMenuConfig();
                                    if (!config.getExtraItems().contains(item)) throw NOT_AN_EXTRA_ITEM.create(item.displayName());
                                    config.removeExtraItem(item);
                                    config.sendExtraItemRemoved(context.getSource().getSender(), item);
                                    return Command.SINGLE_SUCCESS;
                                })
                        )
                )
                .then(Commands.literal("list")
                        .requires(EXTRA_ITEMS_LIST)
                        .executes(context -> {
                            CommandSender sender = context.getSource().getSender();
                            ItemSearchMenuConfig config = ItemSearchMenuPlugin.get().getItemSearchMenuConfig();
                            config.sendExtraItemsListHeader(sender);
                            List<ItemStack> items = config.getExtraItems();
                            for (ItemStack item : items){
                                sender.sendMessage(Component.text(" • ", NamedTextColor.GREEN).append(item.displayName()));
                            }
                            return items.size();
                        })
                );

        return Commands.literal("itemsearchmenu")
                .requires(COMMAND)
                .then(Commands.literal("reload")
                        .requires(RELOAD)
                        .executes(context -> {
                            ItemSearchMenuPlugin plugin = ItemSearchMenuPlugin.get();
                            plugin.reloadConfig();
                            plugin.getItemSearchMenuConfig().sendReload(context.getSource().getSender());
                            return Command.SINGLE_SUCCESS;
                        })
                )
                .then(Commands.literal("version")
                        .requires(VERSION)
                        .executes(context -> {
                            ItemSearchMenuPlugin.get().getItemSearchMenuConfig().sendVersion(context.getSource().getSender());
                            return Command.SINGLE_SUCCESS;
                        })
                )
                .then(categoriesNode)
                .then(blacklistNode)
                .then(extraItemsNode)
                .build();
    }

    private static CompletableFuture<Suggestions> suggestAvailableSlots(CommandContext<?> ignored, SuggestionsBuilder builder){
        for (byte b = 1; b <= 9; b++){
            if (ItemSearchMenuPlugin.get().getItemSearchMenuConfig().categoryExists(b)) continue;
            builder.suggest(b);
        }
        return builder.buildFuture();
    }

    private static void listCategoryConditions(ItemCategory category, Audience audience){
        ItemSearchMenuPlugin.get().getItemSearchMenuConfig().sendCategoryConditionListHeader(category, audience);
        for (ItemCondition condition : category.filter()){
            Component line = Component.text(" • " + condition.asString(), NamedTextColor.GREEN);
            audience.sendMessage(line);
        }
    }

    private static int blacklistOperation(CommandContext<CommandSourceStack> context, @NonNull BiPredicate<ItemSearchMenuConfig, ItemCondition> conditionPredicate, DynamicCommandExceptionType conditionFailed,
                                          BiConsumer<ItemSearchMenuConfig, ItemCondition> operation, BlacklistOperationMessage message) throws CommandSyntaxException
    {
        ItemCondition condition = ItemTypeConditionArg.getItemCondition(context, "item-condition");
        ItemSearchMenuConfig config = ItemSearchMenuPlugin.get().getItemSearchMenuConfig();
        if (!conditionPredicate.test(config, condition)) {
            throw conditionFailed.create(condition.asString());
        }
        message.send(config, context.getSource().getSender(), condition);
        operation.accept(config, condition);
        return Command.SINGLE_SUCCESS;
    }

    @Contract(pure = true)
    private static @NotNull Predicate<CommandSourceStack> hasPermission(String permission){
        return Commands.restricted(source -> source.getSender().hasPermission(permission));
    }

    private interface BlacklistOperationMessage {
        void send(ItemSearchMenuConfig config, Audience target, ItemCondition condition);
    }
}
