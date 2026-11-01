package com.plugingui.config;

import com.plugingui.PluginGuiPlugin;
import com.plugingui.util.Text;
import net.kyori.adventure.text.Component;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.inventory.ItemStack;

import java.util.List;
import java.util.Map;

/** Read access to one menus/*.yml file. */
public final class MenuConfig {

    private final PluginGuiPlugin plugin;
    private final YamlConfiguration yml;

    public MenuConfig(PluginGuiPlugin plugin, YamlConfiguration yml) {
        this.plugin = plugin;
        this.yml = yml;
    }

    public String title() {
        return yml.getString("title", "Menu");
    }

    public int rows(int def) {
        return Math.max(1, Math.min(6, yml.getInt("rows", def)));
    }

    public String string(String path, String def) {
        return yml.getString(path, def);
    }

    public ItemStack border() {
        return plugin.icons().fromSection(yml.getConfigurationSection("border"), null);
    }

    public ItemStack filler() {
        return plugin.icons().fromSection(yml.getConfigurationSection("filler"), null);
    }

    public ItemStack item(String key, Map<String, String> ph) {
        return plugin.icons().fromSection(yml.getConfigurationSection("items." + key), ph);
    }

    public List<Component> lore(String key, Map<String, String> ph) {
        return Text.lore(yml.getStringList("items." + key + ".lore"), ph);
    }
}
