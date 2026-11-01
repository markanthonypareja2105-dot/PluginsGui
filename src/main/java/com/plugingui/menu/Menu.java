package com.plugingui.menu;

import com.plugingui.PluginGuiPlugin;
import com.plugingui.config.MenuConfig;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

/** Base class of every menu. Clicks are always cancelled; only registered slot actions run. */
public abstract class Menu implements InventoryHolder {

    protected final PluginGuiPlugin plugin;
    protected final Player viewer;
    private Inventory inventory;
    private final Map<Integer, Consumer<InventoryClickEvent>> actions = new HashMap<>();

    protected Menu(PluginGuiPlugin plugin, Player viewer) {
        this.plugin = plugin;
        this.viewer = viewer;
    }

    protected abstract Component title();

    protected abstract int rows();

    protected abstract void build();

    public void open() {
        inventory = Bukkit.createInventory(this, rows() * 9, title());
        actions.clear();
        build();
        viewer.openInventory(inventory);
    }

    /** Rebuilds the contents of the open menu (page change, toggles, ...). */
    public void refresh() {
        if (inventory == null) {
            return;
        }
        inventory.clear();
        actions.clear();
        build();
    }

    protected void set(int slot, ItemStack item, Consumer<InventoryClickEvent> click) {
        if (slot < 0 || slot >= inventory.getSize()) {
            return;
        }
        inventory.setItem(slot, item);
        if (click != null) {
            actions.put(slot, click);
        } else {
            actions.remove(slot);
        }
    }

    protected void frame(MenuConfig cfg) {
        frame(cfg.border(), cfg.filler());
    }

    /** Fills the outer ring with {@code border} and everything inside with {@code filler}. */
    protected void frame(ItemStack border, ItemStack filler) {
        int rows = rows();
        for (int i = 0; i < rows * 9; i++) {
            int row = i / 9;
            int col = i % 9;
            boolean edge = row == 0 || row == rows - 1 || col == 0 || col == 8;
            inventory.setItem(i, (edge ? border : filler).clone());
        }
    }

    /** True when the slot is inside the border (columns 1-7, rows 1..rows-2). */
    protected boolean isInner(int slot) {
        int row = slot / 9;
        int col = slot % 9;
        return row >= 1 && row <= rows() - 2 && col >= 1 && col <= 7;
    }

    protected Runnable closeAction() {
        return viewer::closeInventory;
    }

    public void click(InventoryClickEvent event) {
        Consumer<InventoryClickEvent> action = actions.get(event.getRawSlot());
        if (action == null) {
            return;
        }
        plugin.playClick(viewer);
        action.accept(event);
    }

    public void onClose(InventoryCloseEvent event) {
    }

    @Override
    public Inventory getInventory() {
        return inventory;
    }
}
