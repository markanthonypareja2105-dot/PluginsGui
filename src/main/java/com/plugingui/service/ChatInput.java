package com.plugingui.service;

import com.plugingui.PluginGuiPlugin;
import com.plugingui.util.Text;
import io.papermc.paper.event.player.AsyncChatEvent;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.scheduler.BukkitTask;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;

/** Asks a player for one line of chat input (rename, search, ...). The typed line is never broadcast. */
public final class ChatInput implements Listener {

    private record Pending(Consumer<String> onInput, Runnable onCancel, BukkitTask timeout) {
    }

    private final PluginGuiPlugin plugin;
    private final Map<UUID, Pending> pending = new ConcurrentHashMap<>();

    public ChatInput(PluginGuiPlugin plugin) {
        this.plugin = plugin;
    }

    public void prompt(Player player, String messageKey, Consumer<String> onInput, Runnable onCancel) {
        Pending old = pending.remove(player.getUniqueId());
        if (old != null) {
            old.timeout().cancel();
        }
        int seconds = Math.max(5, plugin.getConfig().getInt("settings.prompt-timeout-seconds", 60));
        player.closeInventory();
        player.sendMessage(plugin.settings().msg(messageKey));
        player.sendMessage(plugin.settings().msg("prompt-hint", Text.ph("seconds", String.valueOf(seconds))));

        BukkitTask timeout = Bukkit.getScheduler().runTaskLater(plugin, () -> {
            if (pending.remove(player.getUniqueId()) != null) {
                player.sendMessage(plugin.settings().msg("prompt-timeout"));
            }
        }, seconds * 20L);
        pending.put(player.getUniqueId(), new Pending(onInput, onCancel, timeout));
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onChat(AsyncChatEvent event) {
        Pending p = pending.remove(event.getPlayer().getUniqueId());
        if (p == null) {
            return;
        }
        event.setCancelled(true);
        p.timeout().cancel();
        String text = PlainTextComponentSerializer.plainText().serialize(event.message()).trim();
        Player player = event.getPlayer();
        Bukkit.getScheduler().runTask(plugin, () -> {
            if (text.isEmpty() || text.equalsIgnoreCase("cancel")) {
                player.sendMessage(plugin.settings().msg("prompt-cancelled"));
                if (p.onCancel() != null) {
                    p.onCancel().run();
                }
            } else {
                p.onInput().accept(text);
            }
        });
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        Pending p = pending.remove(event.getPlayer().getUniqueId());
        if (p != null) {
            p.timeout().cancel();
        }
    }
}
