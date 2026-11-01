package com.plugingui.menu;

import com.plugingui.PluginGuiPlugin;
import com.plugingui.config.MenuConfig;
import com.plugingui.model.CommandInfo;
import com.plugingui.model.PluginEntry;
import com.plugingui.util.Text;
import net.kyori.adventure.text.Component;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

/** "Plugin Name": one command block per command of a single plugin. */
public final class PluginCommandsMenu extends PagedMenu {

    private final String pluginName;
    private final Runnable back;

    public PluginCommandsMenu(PluginGuiPlugin plugin, Player viewer, String pluginName, Runnable back) {
        super(plugin, viewer);
        this.pluginName = pluginName;
        this.back = back;
    }

    @Override
    protected MenuConfig cfg() {
        return plugin.settings().menu("commands");
    }

    @Override
    protected Component title() {
        return Text.mm(cfg().title(), Text.ph("plugin", pluginName));
    }

    @Override
    protected Runnable back() {
        return back;
    }

    @Override
    protected List<Entry> entries() {
        List<Entry> out = new ArrayList<>();
        PluginEntry entry = plugin.scanner().find(pluginName);
        if (entry == null) {
            return out;
        }
        for (CommandInfo c : entry.commands()) {
            out.add(new Entry(CommandActions.icon(plugin, viewer, c, false),
                    e -> CommandActions.click(plugin, viewer, c, e.getClick())));
        }
        return out;
    }
}
