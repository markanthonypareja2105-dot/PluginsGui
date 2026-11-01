package com.plugingui.menu;

import com.plugingui.PluginGuiPlugin;
import com.plugingui.config.MenuConfig;
import com.plugingui.model.Category;
import com.plugingui.model.CommandInfo;
import com.plugingui.model.PluginEntry;
import com.plugingui.util.Text;
import net.kyori.adventure.text.Component;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;

/** "'Player name' Commands": the category menu players open with /playercommands. */
public final class PlayerMenu extends CategoryGridMenu {

    private final List<PluginEntry> scan;

    public PlayerMenu(PluginGuiPlugin plugin, Player viewer) {
        super(plugin, viewer);
        this.scan = plugin.scanner().scan();
    }

    @Override
    protected MenuConfig cfg() {
        return plugin.settings().menu("player");
    }

    @Override
    protected List<Category> categories() {
        boolean showEmpty = plugin.getConfig().getBoolean("settings.show-empty-categories", true);
        List<Category> out = new ArrayList<>();
        for (Category c : plugin.categories().visibleTo(viewer)) {
            if (showEmpty || !available(c).isEmpty()) {
                out.add(c);
            }
        }
        return out;
    }

    /** Commands of a category this player may see (respects hide-no-permission-commands). */
    static List<CommandInfo> visibleCommands(PluginGuiPlugin plugin, Player viewer, Category category, List<PluginEntry> scan) {
        boolean hide = plugin.getConfig().getBoolean("settings.hide-no-permission-commands", true);
        List<CommandInfo> out = new ArrayList<>();
        for (CommandInfo c : plugin.categories().resolve(category, scan)) {
            if (!hide || c.canUse(viewer)) {
                out.add(c);
            }
        }
        return out;
    }

    private List<CommandInfo> available(Category c) {
        return visibleCommands(plugin, viewer, c, scan);
    }

    @Override
    protected ItemStack categoryItem(Category c) {
        List<Component> lore = new ArrayList<>();
        for (String line : c.description) {
            lore.add(Text.mm(line));
        }
        lore.addAll(cfg().lore("category", Text.ph("count", String.valueOf(available(c).size()))));
        return plugin.icons().build(plugin.icons().resolve(c.icon), Text.mm(c.name), lore);
    }

    @Override
    protected void categoryClick(Category c, InventoryClickEvent event) {
        new CategoryMenu(plugin, viewer, c, this::open).open();
    }

    @Override
    protected void extras() {
        int base = (rows() - 1) * 9;
        set(base + 2, cfg().item("help", null), e -> {
            viewer.closeInventory();
            for (String line : plugin.settings().rawList("player-help")) {
                viewer.sendMessage(Text.mm(line));
            }
        });
        set(base + 4, plugin.settings().button("close", null), e -> viewer.closeInventory());
    }
}
