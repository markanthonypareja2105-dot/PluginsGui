package com.plugingui.service;

import com.plugingui.PluginGuiPlugin;
import com.plugingui.model.Category;
import com.plugingui.model.CommandInfo;
import com.plugingui.model.PluginEntry;
import com.plugingui.util.Text;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/** Loads, edits and saves categories.yml. */
public final class CategoryManager {

    private final PluginGuiPlugin plugin;
    private final Map<String, Category> categories = new LinkedHashMap<>();

    public CategoryManager(PluginGuiPlugin plugin) {
        this.plugin = plugin;
    }

    private File file() {
        return new File(plugin.getDataFolder(), "categories.yml");
    }

    public void load() {
        if (!file().exists()) {
            plugin.saveResource("categories.yml", false);
        }
        categories.clear();
        YamlConfiguration yml = YamlConfiguration.loadConfiguration(file());
        ConfigurationSection root = yml.getConfigurationSection("categories");
        if (root == null) {
            return;
        }
        for (String id : root.getKeys(false)) {
            ConfigurationSection s = root.getConfigurationSection(id);
            if (s == null) {
                continue;
            }
            categories.put(id, new Category(id, s.getString("name", id), s.getString("icon", "BOOK"),
                    s.getString("permission", ""), s.getInt("slot", -1),
                    s.getStringList("description"), s.getStringList("commands")));
        }
    }

    public void save() {
        YamlConfiguration yml = new YamlConfiguration();
        for (Category c : categories.values()) {
            String base = "categories." + c.id + ".";
            yml.set(base + "name", c.name);
            yml.set(base + "icon", c.icon);
            yml.set(base + "slot", c.slot);
            yml.set(base + "permission", c.permission);
            yml.set(base + "description", c.description);
            yml.set(base + "commands", c.entries);
        }
        try {
            yml.save(file());
        } catch (IOException e) {
            plugin.getLogger().severe("Could not save categories.yml: " + e.getMessage());
        }
    }

    public List<Category> all() {
        return new ArrayList<>(categories.values());
    }

    public Category get(String idOrName) {
        Category byId = categories.get(idOrName.toLowerCase(Locale.ROOT));
        if (byId != null) {
            return byId;
        }
        for (Category c : categories.values()) {
            if (Text.plain(c.name).equalsIgnoreCase(idOrName)) {
                return c;
            }
        }
        return null;
    }

    public List<Category> visibleTo(Player player) {
        List<Category> out = new ArrayList<>();
        for (Category c : categories.values()) {
            if (c.permission.isBlank() || player.hasPermission(c.permission)) {
                out.add(c);
            }
        }
        return out;
    }

    public Category create(String name, int slot) {
        String base = Text.plain(name).toLowerCase(Locale.ROOT).replaceAll("[^a-z0-9]+", "-").replaceAll("^-+|-+$", "");
        if (base.isBlank()) {
            base = "category";
        }
        String id = base;
        int n = 2;
        while (categories.containsKey(id)) {
            id = base + "-" + n++;
        }
        Category c = new Category(id, name, "BOOK", "", slot, List.of(), List.of());
        categories.put(id, c);
        save();
        return c;
    }

    public void delete(String id) {
        categories.remove(id);
        save();
    }

    // ---- entries ("/name" or "plugin:Name") ----

    public boolean hasEntry(Category c, String entry) {
        String wanted = normalize(entry);
        for (String e : c.entries) {
            if (normalize(e).equals(wanted)) {
                return true;
            }
        }
        return false;
    }

    public void toggleEntry(Category c, String entry) {
        String wanted = normalize(entry);
        if (!c.entries.removeIf(e -> normalize(e).equals(wanted))) {
            c.entries.add(entry);
        }
        save();
    }

    private static String normalize(String entry) {
        String e = entry.trim().toLowerCase(Locale.ROOT);
        return e.startsWith("/") ? e.substring(1) : e;
    }

    /** Turns the entries of a category into real commands. */
    public List<CommandInfo> resolve(Category c, List<PluginEntry> all) {
        boolean unregistered = plugin.getConfig().getBoolean("settings.show-unregistered-commands", false);
        Map<String, CommandInfo> out = new LinkedHashMap<>();
        for (String raw : c.entries) {
            String entry = raw.trim();
            if (entry.regionMatches(true, 0, "plugin:", 0, 7)) {
                String wanted = entry.substring(7).trim();
                for (PluginEntry p : all) {
                    if (p.name().equalsIgnoreCase(wanted)) {
                        for (CommandInfo info : p.commands()) {
                            out.putIfAbsent(info.name().toLowerCase(Locale.ROOT), info);
                        }
                    }
                }
                continue;
            }
            String name = normalize(entry);
            CommandInfo found = find(all, name);
            if (found != null) {
                out.putIfAbsent(found.name().toLowerCase(Locale.ROOT), found);
            } else if (unregistered && !name.isBlank()) {
                out.putIfAbsent(name, new CommandInfo(name, "", "/" + name, List.of(), null, "Unknown", null));
            }
        }
        return new ArrayList<>(out.values());
    }

    private static CommandInfo find(List<PluginEntry> all, String name) {
        for (PluginEntry p : all) {
            for (CommandInfo info : p.commands()) {
                if (info.name().equalsIgnoreCase(name)) {
                    return info;
                }
                for (String alias : info.aliases()) {
                    if (alias.equalsIgnoreCase(name)) {
                        return info;
                    }
                }
            }
        }
        return null;
    }
}
