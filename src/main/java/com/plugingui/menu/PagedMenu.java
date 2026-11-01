package com.plugingui.menu;

import com.plugingui.PluginGuiPlugin;
import com.plugingui.config.MenuConfig;
import com.plugingui.util.Text;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

/**
 * 6-row menu with a border, a 7x4 content area, page buttons (48/50), back (45) and close (49).
 * Subclasses add their own buttons in {@link #extras()} (47 and 51 are free for that).
 */
public abstract class PagedMenu extends Menu {

    public record Entry(ItemStack item, Consumer<InventoryClickEvent> click) {
    }

    protected int page = 0;

    protected PagedMenu(PluginGuiPlugin plugin, Player viewer) {
        super(plugin, viewer);
    }

    protected abstract MenuConfig cfg();

    protected abstract List<Entry> entries();

    /** Where the back button leads, or null for no back button. */
    protected Runnable back() {
        return null;
    }

    protected void extras() {
    }

    @Override
    protected int rows() {
        return 6;
    }

    @Override
    protected void build() {
        frame(cfg());

        List<Integer> slots = new ArrayList<>();
        for (int i = 0; i < rows() * 9; i++) {
            if (isInner(i)) {
                slots.add(i);
            }
        }
        List<Entry> all = entries();
        int perPage = slots.size();
        int pages = Math.max(1, (all.size() + perPage - 1) / perPage);
        page = Math.max(0, Math.min(page, pages - 1));

        for (int i = 0; i < perPage; i++) {
            int index = page * perPage + i;
            if (index >= all.size()) {
                break;
            }
            Entry entry = all.get(index);
            set(slots.get(i), entry.item(), entry.click());
        }
        if (all.isEmpty()) {
            set(22, plugin.settings().button("empty", null), null);
        }

        Map<String, String> ph = Text.ph("page", String.valueOf(page + 1), "pages", String.valueOf(pages));
        int base = (rows() - 1) * 9;
        set(base + 4, plugin.settings().button("close", ph), e -> closeAction().run());

        Runnable back = back();
        if (back != null) {
            set(base, plugin.settings().button("back", ph), e -> back.run());
        }
        if (page > 0) {
            set(base + 3, plugin.settings().button("previous", ph), e -> {
                page--;
                refresh();
            });
        }
        if (page < pages - 1) {
            set(base + 5, plugin.settings().button("next", ph), e -> {
                page++;
                refresh();
            });
        }
        extras();
    }
}
