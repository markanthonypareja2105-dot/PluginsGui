package com.plugingui;

import com.plugingui.command.PlayerCommandsCommand;
import com.plugingui.command.PluginGuiCommand;
import com.plugingui.config.Settings;
import com.plugingui.icon.IconFactory;
import com.plugingui.menu.Menu;
import com.plugingui.menu.MenuListener;
import com.plugingui.service.CategoryManager;
import com.plugingui.service.ChatInput;
import com.plugingui.service.CommandScanner;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.sound.Sound;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

/** PluginGui: a menu that shows every installed plugin and its commands. */
public final class PluginGuiPlugin extends JavaPlugin {

    private Settings settings;
    private IconFactory icons;
    private CommandScanner scanner;
    private CategoryManager categories;
    private ChatInput chat;

    @Override
    public void onEnable() {
        icons = new IconFactory(this);
        settings = new Settings(this);
        scanner = new CommandScanner(this);
        categories = new CategoryManager(this);
        chat = new ChatInput(this);

        settings.load();
        categories.load();

        getServer().getPluginManager().registerEvents(new MenuListener(this), this);
        getServer().getPluginManager().registerEvents(chat, this);

        PluginGuiCommand admin = new PluginGuiCommand(this);
        getCommand("plugingui").setExecutor(admin);
        getCommand("plugingui").setTabCompleter(admin);

        PlayerCommandsCommand players = new PlayerCommandsCommand(this);
        getCommand("playercommands").setExecutor(players);
        getCommand("playercommands").setTabCompleter(players);
    }

    @Override
    public void onDisable() {
        closeMenus();
    }

    /** Re-reads config.yml, messages.yml, menus/*.yml and categories.yml. */
    public void reloadAll() {
        closeMenus();
        settings.load();
        categories.load();
    }

    private void closeMenus() {
        for (Player p : Bukkit.getOnlinePlayers()) {
            if (p.getOpenInventory().getTopInventory().getHolder() instanceof Menu) {
                p.closeInventory();
            }
        }
    }

    public void playClick(Player player) {
        String key = getConfig().getString("settings.click-sound", "ui.button.click");
        if (key == null || key.isBlank()) {
            return;
        }
        try {
            player.playSound(Sound.sound(Key.key(key), Sound.Source.UI, 0.6f, 1.0f));
        } catch (RuntimeException ignored) {
            // invalid sound key in config.yml: stay silent
        }
    }

    public Settings settings() {
        return settings;
    }

    public IconFactory icons() {
        return icons;
    }

    public CommandScanner scanner() {
        return scanner;
    }

    public CategoryManager categories() {
        return categories;
    }

    public ChatInput chat() {
        return chat;
    }
}
