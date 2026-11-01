package com.plugingui.menu;

import com.plugingui.PluginGuiPlugin;
import com.plugingui.config.MenuConfig;
import com.plugingui.model.Category;
import com.plugingui.model.CommandInfo;
import com.plugingui.model.PluginEntry;
import com.plugingui.util.Text;
import net.kyori.adventure.text.Component;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Chooses what a category contains. Level 1 lists plugins (shift-click adds a whole plugin),
 * level 2 lists the commands of one plugin (click toggles a single command).
 */
public final class CommandPickerMenu extends PagedMenu {

    private final Category category;
    private final String pluginName; // null = plugin list
    private final Runnable root;     // where "back" leaves the picker

    public CommandPickerMenu(PluginGuiPlugin plugin, Player viewer, Category category, String pluginName, Runnable root) {
        super(plugin, viewer);
        this.category = category;
        this.pluginName = pluginName;
        this.root = root;
    }

    @Override
    protected MenuConfig cfg() {
        return plugin.settings().menu("editor");
    }

    @Override
    protected Component title() {
        return Text.mm(cfg().string("picker-title", "Commands: {category}"),
                Text.ph("category", Text.plain(category.name), "plugin", pluginName == null ? "" : pluginName));
    }

    @Override
    protected Runnable back() {
        if (pluginName == null) {
            return root;
        }
        return () -> new CommandPickerMenu(plugin, viewer, category, null, root).open();
    }

    @Override
    protected List<Entry> entries() {
        List<Entry> out = new ArrayList<>();
        if (pluginName == null) {
            for (PluginEntry p : plugin.scanner().scan()) {
                if (p.commands().isEmpty()) {
                    continue;
                }
                String key = "plugin:" + p.name();
                boolean whole = plugin.categories().hasEntry(category, key);
                Map<String, String> ph = Text.ph("plugin", p.name(),
                        "count", String.valueOf(p.commands().size()), "included", whole ? "Yes" : "No");
                ItemStack item = plugin.icons().glow(cfg().item("picker-plugin", ph), whole);
                out.add(new Entry(item, e -> {
                    if (e.isShiftClick()) {
                        plugin.categories().toggleEntry(category, key);
                        refresh();
                    } else {
                        new CommandPickerMenu(plugin, viewer, category, p.name(), root).open();
                    }
                }));
            }
            return out;
        }

        PluginEntry entry = plugin.scanner().find(pluginName);
        if (entry == null) {
            return out;
        }
        boolean whole = plugin.categories().hasEntry(category, "plugin:" + pluginName);
        for (CommandInfo c : entry.commands()) {
            String key = "/" + c.name();
            boolean included = whole || plugin.categories().hasEntry(category, key);
            Map<String, String> ph = Text.ph("command", c.name(),
                    "description", Text.wrap(c.description().isBlank() ? "No description." : c.description(), 38),
                    "included", included ? "Yes" : "No");
            ItemStack item = plugin.icons().glow(cfg().item("picker-command", ph), included);
            out.add(new Entry(item, e -> {
                if (!whole) {
                    plugin.categories().toggleEntry(category, key);
                    refresh();
                }
            }));
        }
        return out;
    }
}
