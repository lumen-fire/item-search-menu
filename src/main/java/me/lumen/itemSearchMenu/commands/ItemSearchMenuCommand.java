package me.lumen.itemSearchMenu.commands;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.LiteralMessage;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import com.mojang.brigadier.tree.LiteralCommandNode;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;
import me.lumen.itemSearchMenu.gui.ItemSearchMenu;
import org.bukkit.entity.Player;

public class ItemSearchMenuCommand {
    private static final SimpleCommandExceptionType NOT_PLAYER = new SimpleCommandExceptionType(new LiteralMessage("This is a player only command!"));

    public static LiteralCommandNode<CommandSourceStack> createCommand(String label){
        return Commands.literal(label)
                .requires(source -> source.getSender().hasPermission("item-search-menu.open-command"))
                .executes(context -> {
                    if (!(context.getSource().getExecutor() instanceof Player player)) throw NOT_PLAYER.create();
                    ItemSearchMenu.open(player);
                    return Command.SINGLE_SUCCESS;
                })
                .then(Commands.argument("search", StringArgumentType.greedyString())
                        .executes(context -> {
                            if (!(context.getSource().getExecutor() instanceof Player player)) throw NOT_PLAYER.create();
                            String search = StringArgumentType.getString(context, "search");
                            ItemSearchMenu.open(player, search);
                            return Command.SINGLE_SUCCESS;
                        })
                )
                .build();
    }
}
