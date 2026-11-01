package com.plugingui.model;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;

import java.util.List;

/** One command registered on the server. {@code command} is null for names we only know from categories.yml. */
public record CommandInfo(String name, String description, String usage, List<String> aliases,
                          String permission, String plugin, Command command) {

    /** True when the usage line contains a required {@code <argument>}. */
    public boolean needsArgs() {
        String u = usage == null ? "" : usage.replace("<command>", "");
        return u.contains("<");
    }

    public boolean canUse(CommandSender sender) {
        return command == null || command.testPermissionSilent(sender);
    }
}
