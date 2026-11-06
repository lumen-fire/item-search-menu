package me.lumen.itemSearchMenu;

import io.papermc.paper.dialog.Dialog;
import io.papermc.paper.plugin.bootstrap.BootstrapContext;
import io.papermc.paper.plugin.bootstrap.PluginBootstrap;
import io.papermc.paper.registry.RegistryAccess;
import io.papermc.paper.registry.RegistryKey;
import io.papermc.paper.registry.data.dialog.ActionButton;
import io.papermc.paper.registry.data.dialog.DialogBase;
import io.papermc.paper.registry.data.dialog.action.DialogAction;
import io.papermc.paper.registry.data.dialog.input.DialogInput;
import io.papermc.paper.registry.data.dialog.type.DialogType;
import io.papermc.paper.registry.event.RegistryEvents;
import io.papermc.paper.registry.keys.DialogKeys;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.key.Namespaced;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.jetbrains.annotations.NotNull;

import java.util.List;

@SuppressWarnings("UnstableApiUsage")
public class ItemSearchMenuBootstrap implements PluginBootstrap {
    private static Key dialogKey;
    private static Key actionKey;

    private static final ActionButton CANCEL = ActionButton.builder(Component.text("Cancel", NamedTextColor.RED)).build();
    public static final String INPUT_KEY = "search_term";


    @Override
    public void bootstrap(@NotNull BootstrapContext context) {
        Namespaced namespace = context.getPluginMeta();
        dialogKey = Key.key(namespace, "search_menu");
        actionKey = Key.key(namespace, "search_item");

        context.getLifecycleManager().registerEventHandler(RegistryEvents.DIALOG.compose(), event ->
                event.registry().register(
                        DialogKeys.create(dialogKey),
                        builder -> {
                            ActionButton confirm = ActionButton.builder(Component.text("Confirm", NamedTextColor.GREEN))
                                    .action(DialogAction.customClick(actionKey, null))
                                    .build();

                            builder.type(DialogType.confirmation(confirm, CANCEL))
                                    .base(DialogBase.builder(Component.text("Search for items", NamedTextColor.LIGHT_PURPLE))
                                            .inputs(List.of(DialogInput.text(INPUT_KEY, Component.text("Search term", NamedTextColor.LIGHT_PURPLE)).build())).build());
                        }
                )
        );
    }

    public static Key getActionKey(){
        return actionKey;
    }

    public static Dialog getDialog(){
        return RegistryAccess.registryAccess().getRegistry(RegistryKey.DIALOG).get(dialogKey);
    }
}
