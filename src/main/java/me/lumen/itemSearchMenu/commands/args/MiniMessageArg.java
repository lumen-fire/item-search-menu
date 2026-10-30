package me.lumen.itemSearchMenu.commands.args;

import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import io.papermc.paper.command.brigadier.argument.CustomArgumentType;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.jetbrains.annotations.NotNull;

public class MiniMessageArg implements CustomArgumentType.Converted<Component, String> {
    private static final MiniMessageArg argument = new MiniMessageArg();
    private MiniMessageArg(){}

    @Override
    public @NotNull Component convert(@NotNull String nativeType) {
        return MiniMessage.miniMessage().deserialize(nativeType);
    }

    @Override
    public @NotNull ArgumentType<String> getNativeType() {
        return StringArgumentType.greedyString();
    }

    public static MiniMessageArg argument(){
        return argument;
    }

    public static Component getMessage(@NotNull CommandContext<?> context, String name){
        return context.getArgument(name, Component.class);
    }
}
