package com.plugingui.menu;

import com.plugingui.PluginGuiPlugin;
import com.plugingui.config.MenuConfig;
import com.plugingui.model.PluginEntry;
import com.plugingui.util.Text;
import net.kyori.adventure.text.Component;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/** "Plugins Gui": one book per installed plugin (admin menu). */
public final class PluginsMenu extends PagedMenu {

    private String filter;

    public PluginsMenu(PluginGuiPlugin plugin, Player viewer, String filter) {
        super(plugin, viewer);
        this.filter = filter == null || filter.isBlank() ? null : filter;
    }

    @Override
    protected MenuConfig cfg() {
        return plugin.settings().menu("main");
    }

    @Override
    protected Component title() {
        return Text.mm(cfg().title(), Text.ph("filter", filter == null ? "" : filter));
    }

    @Override
    protected List<Entry> entries() {
        boolean hideEmpty = plugin.getConfig().getBoolean("settings.hide-empty-plugins", false);
        List<Entry> out = new ArrayList<>();
        for (PluginEntry p : plugin.scanner().scan()) {
            if (filter != null && !p.name().toLowerCase(Locale.ROOT).contains(filter.toLowerCase(Locale.ROOT))) {
                continue;
            }
            if (hideEmpty && p.commands().isEmpty()) {
                continue;
            }
            Map<String, String> ph = Text.ph(
                    "plugin", p.name(),
                    "version", p.version(),
                    "authors", p.authors(),
                    "description", Text.wrap(p.description().isBlank() ? "No description provided." : p.description(), 38),
                    "count", String.valueOf(p.commands().size()),
                    "status", p.enabled() ? "Enabled" : "Disabled",
                    "status_color", p.enabled() ? "green" : "red");
            out.add(new Entry(cfg().item("plugin", ph), e ->
                    new PluginCommandsMenu(plugin, viewer, p.name(), this::open).open()));
        }
        return out;
    }

    @Override
    protected void extras() {
        int base = (rows() - 1) * 9;
        set(base + 2, cfg().item("edit", null), e -> new EditorMenu(plugin, viewer, null).open());
        set(base + 6, cfg().item("search", Text.ph("filter", filter == null ? "none" : filter)), e -> {
            if (e.isRightClick()) {
                filter = null;
                page = 0;
                open();
                return;
            }
            plugin.chat().prompt(viewer, "prompt-search", text -> {
                filter = text;
                page = 0;
                open();
            }, this::open);
        });
    }
}
