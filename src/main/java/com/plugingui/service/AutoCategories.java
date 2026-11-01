package com.plugingui.service;

import com.plugingui.PluginGuiPlugin;
import com.plugingui.model.Category;
import com.plugingui.model.CommandInfo;
import com.plugingui.model.PluginEntry;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

/**
 * Sorts the commands a player can use into general categories (Teleport, Economy, Chat, ...)
 * by keywords in the command name or description, so the player menu never shows plugin names.
 * Rules live in config.yml under auto-category-rules.
 */
public final class AutoCategories {

    private record Rule(String id, String name, String icon, List<String> description, List<String> keywords) {
    }

    private final PluginGuiPlugin plugin;

    public AutoCategories(PluginGuiPlugin plugin) {
        this.plugin = plugin;
    }

    private List<Rule> rules() {
        List<Rule> out = new ArrayList<>();
        for (Map<?, ?> map : plugin.getConfig().getMapList("auto-category-rules")) {
            String id = String.valueOf(map.get("id"));
            String name = String.valueOf(map.get("name"));
            String icon = map.get("icon") == null ? "BOOK" : String.valueOf(map.get("icon"));
            out.add(new Rule(id, name, icon, strings(map.get("description")), strings(map.get("keywords"))));
        }
        return out;
    }

    private static List<String> strings(Object o) {
        List<String> out = new ArrayList<>();
        if (o instanceof List<?> list) {
            for (Object e : list) {
                out.add(String.valueOf(e));
            }
        }
        return out;
    }

    /**
     * @param taken command names (lowercase) already placed in admin-made categories
     * @return one category per group that has at least one command this player can use
     */
    public List<Category> build(Player viewer, List<PluginEntry> scan, Set<String> taken) {
        List<Rule> rules = rules();
        Map<String, List<String>> grouped = new LinkedHashMap<>();
        for (Rule r : rules) {
            grouped.put(r.id(), new ArrayList<>());
        }
        List<String> other = new ArrayList<>();
        Set<String> seen = new java.util.HashSet<>(taken);

        for (PluginEntry p : scan) {
            for (CommandInfo c : p.commands()) {
                String key = c.name().toLowerCase(Locale.ROOT);
                if (!c.canUse(viewer) || !seen.add(key)) {
                    continue;
                }
                Rule rule = match(rules, c);
                (rule == null ? other : grouped.get(rule.id())).add("/" + c.name());
            }
        }

        List<Category> out = new ArrayList<>();
        for (Rule r : rules) {
            List<String> entries = grouped.get(r.id());
            if (!entries.isEmpty()) {
                out.add(new Category("auto:" + r.id(), r.name(), r.icon(), "", -1, r.description(), entries));
            }
        }
        if (!other.isEmpty()) {
            ConfigurationSection s = plugin.getConfig().getConfigurationSection("auto-category-other");
            String name = s == null ? "<gray>Other" : s.getString("name", "<gray>Other");
            String icon = s == null ? "NETHER_STAR" : s.getString("icon", "NETHER_STAR");
            List<String> desc = s == null ? List.of() : s.getStringList("description");
            out.add(new Category("auto:other", name, icon, "", -1, desc, other));
        }
        return out;
    }

    /** First rule whose keyword matches the command name (or alias); otherwise one that matches the description. */
    private Rule match(List<Rule> rules, CommandInfo c) {
        List<String> names = new ArrayList<>();
        names.add(c.name().toLowerCase(Locale.ROOT));
        for (String a : c.aliases()) {
            names.add(a.toLowerCase(Locale.ROOT));
        }
        for (Rule r : rules) {
            for (String kw : r.keywords()) {
                String k = kw.toLowerCase(Locale.ROOT);
                for (String n : names) {
                    if (n.equals(k) || (k.length() >= 4 && n.contains(k))) {
                        return r;
                    }
                }
            }
        }
        String desc = c.description() == null ? "" : c.description().toLowerCase(Locale.ROOT);
        if (!desc.isBlank()) {
            for (Rule r : rules) {
                for (String kw : r.keywords()) {
                    String k = kw.toLowerCase(Locale.ROOT);
                    if (k.length() >= 4 && java.util.regex.Pattern.compile("\\b" + java.util.regex.Pattern.quote(k)).matcher(desc).find()) {
                        return r;
                    }
                }
            }
        }
        return null;
    }
}
