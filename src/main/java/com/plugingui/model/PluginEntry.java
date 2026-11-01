package com.plugingui.model;

import java.util.List;

/** An installed plugin (or the virtual "Minecraft" entry) together with its commands. */
public record PluginEntry(String name, String version, String authors, String description,
                          boolean enabled, List<CommandInfo> commands) {
}
