package com.plugingui.util;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/** MiniMessage helpers: placeholders, lore expansion and word wrapping. */
public final class Text {

    private static final MiniMessage MM = MiniMessage.miniMessage();

    private Text() {
    }

    /** Parses MiniMessage and removes the default italic that item names/lore get. */
    public static Component mm(String s) {
        return MM.deserialize(s == null ? "" : s)
                .decorationIfAbsent(TextDecoration.ITALIC, TextDecoration.State.FALSE);
    }

    public static Component mm(String s, Map<String, String> ph) {
        return mm(fill(s, ph));
    }

    /** Replaces {key} with the (tag-escaped) value. */
    public static String fill(String s, Map<String, String> ph) {
        if (s == null) {
            return "";
        }
        if (ph != null) {
            for (Map.Entry<String, String> e : ph.entrySet()) {
                String value = e.getValue() == null ? "" : e.getValue();
                s = s.replace("{" + e.getKey() + "}", MM.escapeTags(value));
            }
        }
        return s;
    }

    /** Builds a placeholder map from key, value, key, value... */
    public static Map<String, String> ph(String... kv) {
        Map<String, String> map = new HashMap<>();
        for (int i = 0; i + 1 < kv.length; i += 2) {
            map.put(kv[i], kv[i + 1]);
        }
        return map;
    }

    /**
     * Expands lore lines. A placeholder value containing '\n' (see {@link #wrap}) becomes several
     * lines, each keeping the text that came before the placeholder (usually a color tag).
     */
    public static List<Component> lore(List<String> raw, Map<String, String> ph) {
        List<Component> out = new ArrayList<>();
        for (String line : raw) {
            String filled = fill(line, ph);
            String[] parts = filled.split("\n", -1);
            int brace = line.indexOf('{');
            String prefix = brace > 0 ? line.substring(0, brace) : "";
            for (int i = 0; i < parts.length; i++) {
                out.add(mm(i == 0 ? parts[i] : prefix + parts[i]));
            }
        }
        return out;
    }

    /** Plain text of a MiniMessage string (all tags removed). */
    public static String plain(String mini) {
        return PlainTextComponentSerializer.plainText().serialize(MM.deserialize(mini == null ? "" : mini));
    }

    /** Removes legacy color codes from text written by other plugins. */
    public static String stripLegacy(String s) {
        return s == null ? "" : s.replaceAll("[§&][0-9a-fk-orA-FK-OR]", "");
    }

    /** Word-wraps text at the given width using '\n'. */
    public static String wrap(String s, int width) {
        if (s == null || s.isBlank()) {
            return "";
        }
        StringBuilder out = new StringBuilder();
        int line = 0;
        for (String word : s.trim().split("\\s+")) {
            if (line > 0 && line + word.length() + 1 > width) {
                out.append('\n');
                line = 0;
            } else if (line > 0) {
                out.append(' ');
                line++;
            }
            out.append(word);
            line += word.length();
        }
        return out.toString();
    }
}
