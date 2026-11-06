package me.lumen.itemSearchMenu;

import io.papermc.paper.command.brigadier.Commands;
import io.papermc.paper.plugin.lifecycle.event.types.LifecycleEvents;
import me.lumen.itemSearchMenu.commands.ItemSearchMenuCommand;
import me.lumen.itemSearchMenu.commands.ItemSearchMenuConfigCommand;
import me.lumen.itemSearchMenu.config.ItemSearchMenuConfig;
import me.lumen.itemSearchMenu.gui.ItemSearchMenu;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

public final class ItemSearchMenuPlugin extends JavaPlugin implements Listener {
    private static ItemSearchMenuPlugin instance;

    private final ItemSearchMenuConfig config = new ItemSearchMenuConfig(this);

    @Override
    public void onEnable() {
        instance = this;
        commands();
        events();
    }

    public ItemSearchMenuConfig getItemSearchMenuConfig(){
        return config;
    }

    public static ItemSearchMenuPlugin get(){
        return instance;
    }

    private void commands(){
        getLifecycleManager().registerEventHandler(LifecycleEvents.COMMANDS, event -> {
            Commands commands = event.registrar();
            commands.register(ItemSearchMenuConfigCommand.createCommand(), "The main plugin command, used for configuration, etc");
            commands.register(ItemSearchMenuCommand.createCommand(config.getMenuOpenCommand()), "Open an item search menu");
        });
    }

    private void events(){
        Bukkit.getPluginManager().registerEvents(this, this);
    }

    @EventHandler
    public void onInventoryClick(@NotNull InventoryClickEvent event){
        Inventory clicked = event.getClickedInventory();
        if (clicked == null) return;
        if (clicked.getHolder() instanceof ItemSearchMenu menu){
            event.setCancelled(true);
            menu.click(event);
        }
        if (event.getInventory().getHolder() instanceof ItemSearchMenu && event.isShiftClick()){
            event.setCancelled(true);
        }
    }
}
