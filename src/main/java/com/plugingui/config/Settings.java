package com.plugingui.config;

import com.plugingui.PluginGuiPlugin;
import com.plugingui.util.Text;
import net.kyori.adventure.text.Component;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.inventory.ItemStack;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/** Loads config.yml, messages.yml and menus/*.yml (missing keys fall back to the bundled defaults). */
public final class Settings {

    private static final List<String> MENUS = List.of("main", "commands", "player", "editor");

    private final PluginGuiPlugin plugin;
    private final Map<String, MenuConfig> menus = new HashMap<>();
    private YamlConfiguration messages = new YamlConfiguration();

    public Settings(PluginGuiPlugin plugin) {
        this.plugin = plugin;
    }

    public void load() {
        plugin.saveDefaultConfig();
        plugin.reloadConfig();
        messages = yaml("messages.yml");
        menus.clear();
        for (String name : MENUS) {
            menus.put(name, new MenuConfig(plugin, yaml("menus/" + name + ".yml")));
        }
    }

    private YamlConfiguration yaml(String path) {
        File file = new File(plugin.getDataFolder(), path);
        if (!file.exists()) {
            plugin.saveResource(path, false);
        }
        YamlConfiguration yml = YamlConfiguration.loadConfiguration(file);
        try (InputStream in = plugin.getResource(path)) {
            if (in != null) {
                yml.setDefaults(YamlConfiguration.loadConfiguration(new InputStreamReader(in, StandardCharsets.UTF_8)));
            }
        } catch (IOException ignored) {
            // defaults are optional
        }
        return yml;
    }

    public MenuConfig menu(String name) {
        return menus.get(name);
    }

    /** A shared button from config.yml (close, back, previous, next, empty). */
    public ItemStack button(String key, Map<String, String> ph) {
        return plugin.icons().fromSection(plugin.getConfig().getConfigurationSection("buttons." + key), ph);
    }

    public String raw(String key) {
        return messages.getString(key, key);
    }

    public List<String> rawList(String key) {
        return messages.getStringList(key);
    }

    /** A message without the prefix. */
    public Component msg(String key, Map<String, String> ph) {
        return Text.mm(raw(key), ph);
    }

    public Component msg(String key) {
        return msg(key, null);
    }

    public void send(CommandSender to, String key, Map<String, String> ph) {
        to.sendMessage(Text.mm(raw("prefix")).append(msg(key, ph)));
    }

    public void send(CommandSender to, String key) {
        send(to, key, null);
    }
}
