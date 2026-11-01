package com.plugingui.menu;

import com.plugingui.PluginGuiPlugin;
import com.plugingui.config.MenuConfig;
import com.plugingui.model.Category;
import net.kyori.adventure.text.Component;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

/** Category icons laid out on the inner area of a 6-row menu (player menu and category editor). */
abstract class CategoryGridMenu extends Menu {

    protected CategoryGridMenu(PluginGuiPlugin plugin, Player viewer) {
        super(plugin, viewer);
    }

    protected abstract MenuConfig cfg();

    protected abstract List<Category> categories();

    protected abstract ItemStack categoryItem(Category category);

    protected abstract void categoryClick(Category category, InventoryClickEvent event);

    /** Item for unused inner slots, or null to leave the filler. */
    protected ItemStack emptyItem() {
        return null;
    }

    protected void emptyClick(int slot, InventoryClickEvent event) {
    }

    protected void extras() {
    }

    @Override
    protected Component title() {
        return com.plugingui.util.Text.mm(cfg().title(), com.plugingui.util.Text.ph("player", viewer.getName()));
    }

    @Override
    protected int rows() {
        return cfg().rows(6);
    }

    @Override
    protected void build() {
        frame(cfg());
        Set<Integer> used = new HashSet<>();
        List<Category> list = categories();

        // First pass: categories that sit on their configured slot.
        java.util.Map<Category, Integer> placed = new java.util.LinkedHashMap<>();
        for (Category c : list) {
            if (isInner(c.slot) && used.add(c.slot)) {
                placed.put(c, c.slot);
            }
        }
        // Second pass: the rest go to the first free inner slot.
        for (Category c : list) {
            if (placed.containsKey(c)) {
                continue;
            }
            for (int s = 0; s < rows() * 9; s++) {
                if (isInner(s) && used.add(s)) {
                    placed.put(c, s);
                    break;
                }
            }
        }
        for (java.util.Map.Entry<Category, Integer> e : placed.entrySet()) {
            Category category = e.getKey();
            set(e.getValue(), categoryItem(category), click -> categoryClick(category, click));
        }

        ItemStack empty = emptyItem();
        if (empty != null) {
            for (int s = 0; s < rows() * 9; s++) {
                if (isInner(s) && !used.contains(s)) {
                    int slot = s;
                    set(s, empty.clone(), click -> emptyClick(slot, click));
                }
            }
        }
        extras();
    }
}
