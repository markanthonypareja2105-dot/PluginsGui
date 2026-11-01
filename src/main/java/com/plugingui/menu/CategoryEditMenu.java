package com.plugingui.menu;

import com.plugingui.PluginGuiPlugin;
import com.plugingui.config.MenuConfig;
import com.plugingui.model.Category;
import com.plugingui.util.Text;
import net.kyori.adventure.text.Component;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/** "Edits Menu": rename, description, icon, commands, move and delete for one category. */
public final class CategoryEditMenu extends Menu {

    private final Category category;
    private boolean confirmDelete;

    public CategoryEditMenu(PluginGuiPlugin plugin, Player viewer, Category category) {
        super(plugin, viewer);
        this.category = category;
    }

    private MenuConfig cfg() {
        return plugin.settings().menu("editor");
    }

    private void reopen() {
        new CategoryEditMenu(plugin, viewer, category).open();
    }

    @Override
    protected Component title() {
        return Text.mm(cfg().string("edits-title", "Edits Menu"), Text.ph("category", Text.plain(category.name)));
    }

    @Override
    protected int rows() {
        return 3;
    }

    @Override
    protected void build() {
        frame(cfg().border(), cfg().border()); // the Edits Menu is solid dark grey
        Map<String, String> ph = Text.ph(
                "category", Text.plain(category.name),
                "name", Text.plain(category.name),
                "description", category.description.isEmpty() ? "none" : Text.plain(String.join(" | ", category.description)),
                "icon", category.icon,
                "count", String.valueOf(category.entries.size()),
                "slot", String.valueOf(category.slot));

        set(11, cfg().item("rename", ph), e -> plugin.chat().prompt(viewer, "prompt-rename", text -> {
            category.name = text;
            plugin.categories().save();
            plugin.settings().send(viewer, "category-updated");
            reopen();
        }, this::reopen));

        set(12, cfg().item("description", ph), e -> plugin.chat().prompt(viewer, "prompt-description", text -> {
            category.description = new ArrayList<>();
            if (!text.equalsIgnoreCase("clear")) {
                for (String part : text.split("\\|")) {
                    if (!part.isBlank()) {
                        category.description.add(part.trim());
                    }
                }
            }
            plugin.categories().save();
            plugin.settings().send(viewer, "category-updated");
            reopen();
        }, this::reopen));

        set(13, cfg().item("icon", ph), e -> plugin.chat().prompt(viewer, "prompt-icon", text -> {
            String spec = text;
            if (text.equalsIgnoreCase("hand")) {
                ItemStack hand = viewer.getInventory().getItemInMainHand();
                spec = hand.getType() == Material.AIR ? "" : hand.getType().name();
            }
            if (plugin.icons().tryResolve(spec) == null) {
                plugin.settings().send(viewer, "invalid-icon");
            } else {
                category.icon = spec;
                plugin.categories().save();
                plugin.settings().send(viewer, "category-updated");
            }
            reopen();
        }, this::reopen));

        set(14, cfg().item("commands", ph), e ->
                new CommandPickerMenu(plugin, viewer, category, null, this::reopen).open());

        set(15, cfg().item("move", ph), e -> {
            plugin.settings().send(viewer, "move-hint", Text.ph("category", Text.plain(category.name)));
            new EditorMenu(plugin, viewer, category).open();
        });

        set(16, cfg().item(confirmDelete ? "delete-confirm" : "delete", ph), e -> {
            if (!confirmDelete) {
                confirmDelete = true;
                refresh();
                return;
            }
            plugin.categories().delete(category.id);
            plugin.settings().send(viewer, "category-deleted");
            new EditorMenu(plugin, viewer, null).open();
        });

        set(18, plugin.settings().button("back", null), e -> new EditorMenu(plugin, viewer, null).open());
        set(22, plugin.settings().button("close", null), e -> viewer.closeInventory());
    }
}
