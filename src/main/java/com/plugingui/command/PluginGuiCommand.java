package com.plugingui.command;

import com.plugingui.PluginGuiPlugin;
import com.plugingui.menu.EditorMenu;
import com.plugingui.menu.PluginCommandsMenu;
import com.plugingui.menu.PluginsMenu;
import com.plugingui.model.PluginEntry;
import com.plugingui.util.Text;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

/** /plugingui (/pgui): the admin menu, plus reload, edit, plugin and search. */
public final class PluginGuiCommand implements CommandExecutor, TabCompleter {

    private static final List<String> SUB = List.of("help", "reload", "edit", "plugin", "search");

    private final PluginGuiPlugin plugin;

    public PluginGuiCommand(PluginGuiPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length == 0) {
            Player p = player(sender);
            if (p != null) {
                new PluginsMenu(plugin, p, null).open();
            }
            return true;
        }
        switch (args[0].toLowerCase(Locale.ROOT)) {
            case "reload" -> {
                plugin.reloadAll();
                plugin.settings().send(sender, "reloaded");
            }
            case "edit" -> {
                Player p = player(sender);
                if (p != null) {
                    new EditorMenu(plugin, p, null).open();
                }
            }
            case "plugin" -> {
                Player p = player(sender);
                if (p == null) {
                    return true;
                }
                if (args.length < 2) {
                    plugin.settings().send(sender, "usage-plugin");
                    return true;
                }
                PluginEntry entry = plugin.scanner().find(args[1]);
                if (entry == null) {
                    plugin.settings().send(sender, "unknown-plugin", Text.ph("plugin", args[1]));
                    return true;
                }
                new PluginCommandsMenu(plugin, p, entry.name(), () -> new PluginsMenu(plugin, p, null).open()).open();
            }
            case "search" -> {
                Player p = player(sender);
                if (p != null) {
                    String text = String.join(" ", Arrays.copyOfRange(args, 1, args.length));
                    new PluginsMenu(plugin, p, text).open();
                }
            }
            default -> {
                sender.sendMessage(plugin.settings().msg("help-title"));
                for (String line : plugin.settings().rawList("help-lines")) {
                    sender.sendMessage(Text.mm(line));
                }
            }
        }
        return true;
    }

    private Player player(CommandSender sender) {
        if (sender instanceof Player p) {
            return p;
        }
        plugin.settings().send(sender, "player-only");
        return null;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        List<String> out = new ArrayList<>();
        if (args.length == 1) {
            for (String s : SUB) {
                if (s.startsWith(args[0].toLowerCase(Locale.ROOT))) {
                    out.add(s);
                }
            }
        } else if (args.length == 2 && args[0].equalsIgnoreCase("plugin")) {
            for (PluginEntry p : plugin.scanner().scan()) {
                if (p.name().toLowerCase(Locale.ROOT).startsWith(args[1].toLowerCase(Locale.ROOT))) {
                    out.add(p.name());
                }
            }
        }
        return out;
    }
}
