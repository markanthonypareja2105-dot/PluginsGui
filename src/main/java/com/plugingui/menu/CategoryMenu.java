package com.plugingui.menu;

import com.plugingui.PluginGuiPlugin;
import com.plugingui.config.MenuConfig;
import com.plugingui.model.Category;
import com.plugingui.model.CommandInfo;
import com.plugingui.util.Text;
import net.kyori.adventure.text.Component;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

/** The commands of one category, as seen by a player. */
public final class CategoryMenu extends PagedMenu {

    private final Category category;
    private final Runnable back;

    public CategoryMenu(PluginGuiPlugin plugin, Player viewer, Category category, Runnable back) {
        super(plugin, viewer);
        this.category = category;
        this.back = back;
    }

    @Override
    protected MenuConfig cfg() {
        return plugin.settings().menu("commands");
    }

    @Override
    protected Component title() {
        return Text.mm(category.name);
    }

    @Override
    protected Runnable back() {
        return back;
    }

    @Override
    protected List<Entry> entries() {
        List<Entry> out = new ArrayList<>();
        for (CommandInfo c : PlayerMenu.visibleCommands(plugin, viewer, category, plugin.scanner().scan())) {
            out.add(new Entry(CommandActions.icon(plugin, viewer, c),
                    e -> CommandActions.click(plugin, viewer, c, e.getClick())));
        }
        return out;
    }
}
