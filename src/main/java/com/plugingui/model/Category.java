package com.plugingui.model;

import java.util.ArrayList;
import java.util.List;

/** A player-facing group of commands (one icon in /pcoms). Name and description are MiniMessage. */
public final class Category {

    public final String id;
    public String name;
    public String icon;
    public String permission;
    public int slot;
    public List<String> description;
    public List<String> entries;

    public Category(String id, String name, String icon, String permission, int slot,
                    List<String> description, List<String> entries) {
        this.id = id;
        this.name = name;
        this.icon = icon;
        this.permission = permission == null ? "" : permission;
        this.slot = slot;
        this.description = new ArrayList<>(description);
        this.entries = new ArrayList<>(entries);
    }
}
