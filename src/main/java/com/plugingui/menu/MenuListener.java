package com.plugingui.menu;

import com.plugingui.PluginGuiPlugin;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.inventory.Inventory;

/** Keeps items inside PluginGui menus and routes clicks to the menu. */
public final class MenuListener implements Listener {

    public MenuListener(PluginGuiPlugin plugin) {
        // plugin kept for symmetry with other listeners; menus hold their own reference
    }

    @EventHandler
    public void onClick(InventoryClickEvent event) {
        Inventory top = event.getView().getTopInventory();
        if (!(top.getHolder() instanceof Menu menu)) {
            return;
        }
        event.setCancelled(true); // also blocks shift-clicking items into the menu
        if (event.getClickedInventory() == null || !event.getClickedInventory().equals(top)) {
            return;
        }
        menu.click(event);
    }

    @EventHandler
    public void onDrag(InventoryDragEvent event) {
        if (event.getView().getTopInventory().getHolder() instanceof Menu) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onClose(InventoryCloseEvent event) {
        if (event.getInventory().getHolder() instanceof Menu menu) {
            menu.onClose(event);
        }
    }
}
