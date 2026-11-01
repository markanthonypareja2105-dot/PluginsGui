package com.plugingui.service;

import com.plugingui.PluginGuiPlugin;
import com.plugingui.model.CommandInfo;
import com.plugingui.model.PluginEntry;
import com.plugingui.util.Text;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.PluginIdentifiableCommand;
import org.bukkit.plugin.Plugin;
import io.papermc.paper.plugin.configuration.PluginMeta;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.IdentityHashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

/** Reads every installed plugin and the commands it registered, straight from the server's command map. */
public final class CommandScanner {

    private final PluginGuiPlugin plugin;

    public CommandScanner(PluginGuiPlugin plugin) {
        this.plugin = plugin;
    }

    public List<PluginEntry> scan() {
        Map<String, List<CommandInfo>> byPlugin = new LinkedHashMap<>();
        List<CommandInfo> server = new ArrayList<>();
        Set<Command> seen = Collections.newSetFromMap(new IdentityHashMap<>());

        for (Command command : Bukkit.getCommandMap().getKnownCommands().values()) {
            if (!seen.add(command)) {
                continue; // aliases and "plugin:command" keys point at the same object
            }
            if (command instanceof PluginIdentifiableCommand owned) {
                String owner = owned.getPlugin().getName();
                byPlugin.computeIfAbsent(owner, k -> new ArrayList<>()).add(info(command, owner));
            } else {
                server.add(info(command, "Minecraft"));
            }
        }

        List<PluginEntry> result = new ArrayList<>();
        for (Plugin installed : Bukkit.getPluginManager().getPlugins()) {
            PluginMeta meta = installed.getPluginMeta();
            List<CommandInfo> commands = byPlugin.getOrDefault(installed.getName(), new ArrayList<>());
            commands.sort(Comparator.comparing(c -> c.name().toLowerCase(Locale.ROOT)));
            String authors = String.join(", ", meta.getAuthors());
            String description = meta.getDescription() == null ? "" : Text.stripLegacy(meta.getDescription());
            result.add(new PluginEntry(installed.getName(), meta.getVersion(),
                    authors.isBlank() ? "Unknown" : authors, description, installed.isEnabled(), commands));
        }
        result.sort(Comparator.comparing(p -> p.name().toLowerCase(Locale.ROOT)));

        if (plugin.getConfig().getBoolean("settings.include-server-commands", true)) {
            server.sort(Comparator.comparing(c -> c.name().toLowerCase(Locale.ROOT)));
            result.add(0, new PluginEntry("Minecraft", Bukkit.getMinecraftVersion(), "Mojang / PaperMC",
                    "Built-in server commands.", true, server));
        }
        return result;
    }

    public PluginEntry find(String name) {
        for (PluginEntry entry : scan()) {
            if (entry.name().equalsIgnoreCase(name)) {
                return entry;
            }
        }
        return null;
    }

    private CommandInfo info(Command c, String owner) {
        String usage = c.getUsage() == null || c.getUsage().isBlank() ? "/<command>" : c.getUsage();
        usage = usage.replace("<command>", c.getName());
        return new CommandInfo(c.getName(), Text.stripLegacy(c.getDescription()), usage,
                new ArrayList<>(c.getAliases()), c.getPermission(), owner, c);
    }
}
