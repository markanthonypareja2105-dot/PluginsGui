package com.plugingui.menu;

import com.plugingui.PluginGuiPlugin;
import com.plugingui.model.CommandInfo;
import com.plugingui.util.Text;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;

import java.util.Map;

/** Shared look and click behaviour of command icons (plugin menu and category menu). */
final class CommandActions {

    private CommandActions() {
    }

    static ItemStack icon(PluginGuiPlugin plugin, Player viewer, CommandInfo c, boolean playerView) {
        boolean access = c.canUse(viewer);
        Map<String, String> ph = Text.ph(
                "command", c.name(),
                "description", Text.wrap(c.description().isBlank() ? "No description provided." : c.description(), 38),
                "usage", c.usage(),
                "aliases", c.aliases().isEmpty() ? "none" : "/" + String.join(", /", c.aliases()),
                "permission", c.permission() == null || c.permission().isBlank() ? "none" : c.permission(),
                "plugin", c.plugin(),
                "access", access ? "Yes" : "No",
                "access_color", access ? "green" : "red");
        return plugin.settings().menu("commands").item(playerView ? "command-player" : "command", ph);
    }

    /** Left-click runs commands that need no arguments; everything else is placed in the chat box. */
    static void click(PluginGuiPlugin plugin, Player viewer, CommandInfo c, ClickType type) {
        boolean run = plugin.getConfig().getBoolean("settings.run-commands-on-click", true)
                && type.isLeftClick() && !c.needsArgs();
        if (run) {
            if (!c.canUse(viewer)) {
                plugin.settings().send(viewer, "no-permission-command", Text.ph("command", c.name()));
                return;
            }
            viewer.closeInventory();
            viewer.performCommand(c.name());
            return;
        }
        viewer.closeInventory();
        Component message = plugin.settings().msg("click-suggest", Text.ph("command", c.name()))
                .clickEvent(ClickEvent.suggestCommand("/" + c.name() + " "));
        viewer.sendMessage(message);
    }
}
