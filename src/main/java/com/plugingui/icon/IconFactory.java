package com.plugingui.icon;

import com.plugingui.PluginGuiPlugin;
import com.plugingui.util.Text;
import net.kyori.adventure.text.Component;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.components.CustomModelDataComponent;

import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * Builds menu icons from config sections. Supports vanilla materials plus Nexo and ItemsAdder items
 * (through reflection, so neither plugin is needed to build or run PluginGui).
 */
public final class IconFactory {

    private final PluginGuiPlugin plugin;

    public IconFactory(PluginGuiPlugin plugin) {
        this.plugin = plugin;
    }

    /** Builds an icon from a section with material, name, lore, amount, glow, custom-model-data, item-model. */
    public ItemStack fromSection(ConfigurationSection s, Map<String, String> ph) {
        if (s == null) {
            return new ItemStack(Material.STONE);
        }
        ItemStack item = resolve(s.getString("material", "STONE"));
        item.setAmount(Math.max(1, Math.min(64, s.getInt("amount", 1))));
        ItemMeta meta = item.getItemMeta();
        if (meta == null) {
            return item;
        }
        String name = s.getString("name");
        if (name != null) {
            meta.displayName(Text.mm(name, ph));
        }
        List<String> lore = s.getStringList("lore");
        if (!lore.isEmpty()) {
            meta.lore(Text.lore(lore, ph));
        }
        if (s.contains("custom-model-data")) {
            CustomModelDataComponent cmd = meta.getCustomModelDataComponent();
            cmd.setFloats(List.of((float) s.getDouble("custom-model-data")));
            meta.setCustomModelDataComponent(cmd);
        }
        String model = s.getString("item-model");
        if (model != null && !model.isBlank()) {
            NamespacedKey key = NamespacedKey.fromString(model.toLowerCase(Locale.ROOT));
            if (key != null) {
                meta.setItemModel(key);
            }
        }
        meta.addItemFlags(ItemFlag.values());
        if (s.getBoolean("glow", false)) {
            meta.setEnchantmentGlintOverride(true);
        }
        item.setItemMeta(meta);
        return item;
    }

    /** Copies {@code base} and applies an already-built name and lore (used for categories). */
    public ItemStack build(ItemStack base, Component name, List<Component> lore) {
        ItemStack item = base.clone();
        ItemMeta meta = item.getItemMeta();
        if (meta == null) {
            return item;
        }
        if (name != null) {
            meta.displayName(name);
        }
        if (lore != null) {
            meta.lore(lore);
        }
        meta.addItemFlags(ItemFlag.values());
        item.setItemMeta(meta);
        return item;
    }

    public ItemStack glow(ItemStack item, boolean glow) {
        if (!glow) {
            return item;
        }
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.setEnchantmentGlintOverride(true);
            item.setItemMeta(meta);
        }
        return item;
    }

    /** Resolves a spec; never returns null (falls back to PAPER). */
    public ItemStack resolve(String spec) {
        ItemStack item = tryResolve(spec);
        if (item == null) {
            if (spec != null && !spec.isBlank()) {
                plugin.getLogger().warning("Unknown icon '" + spec + "', using PAPER instead.");
            }
            return new ItemStack(Material.PAPER);
        }
        return item;
    }

    /** Resolves a spec such as DIAMOND, nexo:id or itemsadder:ns:id. Returns null when it is not valid. */
    public ItemStack tryResolve(String spec) {
        if (spec == null || spec.isBlank()) {
            return null;
        }
        String trimmed = spec.trim();
        String lower = trimmed.toLowerCase(Locale.ROOT);
        if (lower.startsWith("nexo:")) {
            return plugin.getConfig().getBoolean("hooks.nexo", true) ? nexo(trimmed.substring(5)) : null;
        }
        if (lower.startsWith("itemsadder:")) {
            return plugin.getConfig().getBoolean("hooks.itemsadder", true) ? itemsAdder(trimmed.substring(11)) : null;
        }
        Material material = Material.matchMaterial(trimmed);
        if (material == null || !material.isItem() || material.isAir()) {
            return null;
        }
        return new ItemStack(material);
    }

    private ItemStack nexo(String id) {
        try {
            Class<?> api = Class.forName("com.nexomc.nexo.api.NexoItems");
            Object builder = api.getMethod("itemFromId", String.class).invoke(null, id);
            if (builder == null) {
                return null;
            }
            return ((ItemStack) builder.getClass().getMethod("build").invoke(builder)).clone();
        } catch (Throwable t) {
            return null;
        }
    }

    private ItemStack itemsAdder(String id) {
        try {
            Class<?> api = Class.forName("dev.lone.itemsadder.api.CustomStack");
            Object stack = api.getMethod("getInstance", String.class).invoke(null, id);
            if (stack == null) {
                return null;
            }
            return ((ItemStack) stack.getClass().getMethod("getItemStack").invoke(stack)).clone();
        } catch (Throwable t) {
            return null;
        }
    }
}
