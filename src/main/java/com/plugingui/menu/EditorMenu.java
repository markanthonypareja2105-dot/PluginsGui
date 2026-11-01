package com.plugingui.menu;

import com.plugingui.PluginGuiPlugin;
import com.plugingui.config.MenuConfig;
import com.plugingui.model.Category;
import com.plugingui.util.Text;
import net.kyori.adventure.text.Component;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;

/**
 * "Edit Gui": every category on its slot. Click one to edit it, click an empty slot to create one.
 * When {@code moving} is set, the next click picks the new slot for that category.
 */
public final class EditorMenu extends CategoryGridMenu {

    private final Category moving;

    public EditorMenu(PluginGuiPlugin plugin, Player viewer, Category moving) {
        super(plugin, viewer);
        this.moving = moving;
    }

    @Override
    protected MenuConfig cfg() {
        return plugin.settings().menu("editor");
    }

    @Override
    protected Component title() {
        String key = moving == null ? cfg().title() : cfg().string("title-moving", "Move category");
        return Text.mm(key);
    }

    @Override
    protected List<Category> categories() {
        return plugin.categories().all();
    }

    @Override
    protected ItemStack categoryItem(Category c) {
        List<Component> lore = new ArrayList<>();
        for (String line : c.description) {
            lore.add(Text.mm(line));
        }
        lore.addAll(cfg().lore("category", Text.ph("count", String.valueOf(c.entries.size()))));
        return plugin.icons().build(plugin.icons().resolve(c.icon), Text.mm(c.name), lore);
    }

    @Override
    protected void categoryClick(Category c, InventoryClickEvent event) {
        if (moving == null) {
            new CategoryEditMenu(plugin, viewer, c).open();
            return;
        }
        if (c != moving) { // swap the two slots
            int tmp = c.slot;
            c.slot = moving.slot;
            moving.slot = tmp;
            plugin.categories().save();
            plugin.settings().send(viewer, "slot-moved");
        }
        new EditorMenu(plugin, viewer, null).open();
    }

    @Override
    protected ItemStack emptyItem() {
        return cfg().item("empty", null);
    }

    @Override
    protected void emptyClick(int slot, InventoryClickEvent event) {
        if (moving != null) {
            moving.slot = slot;
            plugin.categories().save();
            plugin.settings().send(viewer, "slot-moved");
            new EditorMenu(plugin, viewer, null).open();
            return;
        }
        plugin.chat().prompt(viewer, "prompt-create", text -> {
            Category created = plugin.categories().create(text, slot);
            plugin.settings().send(viewer, "category-created", Text.ph("category", Text.plain(created.name)));
            new CategoryEditMenu(plugin, viewer, created).open();
        }, () -> new EditorMenu(plugin, viewer, null).open());
    }

    @Override
    protected void extras() {
        int base = (rows() - 1) * 9;
        if (moving == null) {
            set(base, plugin.settings().button("back", null), e -> new PluginsMenu(plugin, viewer, null).open());
        } else {
            set(base, plugin.settings().button("back", null), e -> new EditorMenu(plugin, viewer, null).open());
        }
        set(base + 4, plugin.settings().button("close", null), e -> viewer.closeInventory());
    }
}
