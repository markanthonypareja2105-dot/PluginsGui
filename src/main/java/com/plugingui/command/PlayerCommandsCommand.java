package com.plugingui.command;

import com.plugingui.PluginGuiPlugin;
import com.plugingui.menu.CategoryMenu;
import com.plugingui.menu.PlayerMenu;
import com.plugingui.model.Category;
import com.plugingui.util.Text;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/** /playercommands (/pcoms): the general menu for players. */
public final class PlayerCommandsCommand implements CommandExecutor, TabCompleter {

    private final PluginGuiPlugin plugin;

    public PlayerCommandsCommand(PluginGuiPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            plugin.settings().send(sender, "player-only");
            return true;
        }
        if (args.length == 0) {
            new PlayerMenu(plugin, player).open();
            return true;
        }
        Category category = plugin.categories().get(String.join(" ", args));
        if (category == null || (!category.permission.isBlank() && !player.hasPermission(category.permission))) {
            plugin.settings().send(sender, "unknown-category", Text.ph("category", String.join(" ", args)));
            return true;
        }
        new CategoryMenu(plugin, player, category, () -> new PlayerMenu(plugin, player).open()).open();
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        List<String> out = new ArrayList<>();
        if (args.length == 1 && sender instanceof Player player) {
            for (Category c : plugin.categories().visibleTo(player)) {
                if (c.id.startsWith(args[0].toLowerCase(Locale.ROOT))) {
                    out.add(c.id);
                }
            }
        }
        return out;
    }
}
