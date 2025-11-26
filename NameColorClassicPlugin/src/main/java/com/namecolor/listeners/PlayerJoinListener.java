package com.namecolor.listeners;

import com.namecolor.NameColorPlugin;
import com.namecolor.utils.ColorHandler;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

/**
 * Listener to restore player colors on join
 */
public class PlayerJoinListener implements Listener {
    
    private final NameColorPlugin plugin;
    
    public PlayerJoinListener(NameColorPlugin plugin) {
        this.plugin = plugin;
    }
    
    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        
        // Check if player has a saved color
        String savedColor = plugin.getPlayerDataManager().getPlayerColor(player.getUniqueId());
        
        if (savedColor != null && ColorHandler.isValidColor(savedColor)) {
            plugin.applyColorToPlayer(player, savedColor);
        }
    }
}
