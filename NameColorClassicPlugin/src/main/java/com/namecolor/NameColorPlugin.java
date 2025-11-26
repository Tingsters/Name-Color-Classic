package com.namecolor;

import com.namecolor.commands.NameColorCommand;
import com.namecolor.data.PlayerDataManager;
import com.namecolor.listeners.PlayerJoinListener;
import com.namecolor.utils.ColorHandler;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

/**
 * Main plugin class for NameColor
 */
public class NameColorPlugin extends JavaPlugin {
    
    private PlayerDataManager playerDataManager;
    private NameColorCommand nameColorCommand;
    
    @Override
    public void onEnable() {
        // Save default config if it doesn't exist
        saveDefaultConfig();
        
        // Initialize player data manager
        playerDataManager = new PlayerDataManager(this);
        
        // Register command
        nameColorCommand = new NameColorCommand(this);
        getCommand("namecolor").setExecutor(nameColorCommand);
        getCommand("namecolor").setTabCompleter(nameColorCommand);
        
        // Register listeners
        getServer().getPluginManager().registerEvents(new PlayerJoinListener(this), this);
        
        // Apply colors to already online players (for reload)
        reapplyAllColors();
        
        getLogger().info("NameColor plugin has been enabled!");
    }
    
    @Override
    public void onDisable() {
        // Save player data synchronously before shutdown
        if (playerDataManager != null) {
            playerDataManager.saveSync();
        }
        getLogger().info("NameColor plugin has been disabled!");
    }
    
    /**
     * Get the player data manager
     * @return PlayerDataManager instance
     */
    public PlayerDataManager getPlayerDataManager() {
        return playerDataManager;
    }
    
    /**
     * Reapply colors to all online players
     * Used when plugin is reloaded
     */
    public void reapplyAllColors() {
        for (Player player : Bukkit.getOnlinePlayers()) {
            String savedColor = playerDataManager.getPlayerColor(player.getUniqueId());
            if (savedColor != null && ColorHandler.isValidColor(savedColor)) {
                applyColorToPlayer(player, savedColor);
            }
        }
    }
    
    /**
     * Apply color to a player based on config settings
     * @param player The player to apply color to
     * @param colorName The color name
     */
    public void applyColorToPlayer(Player player, String colorName) {
        ChatColor color = ColorHandler.getColorByName(colorName);
        if (color == null) return;
        
        // Always apply to display name (chat and above head)
        player.setDisplayName(color + player.getName() + ChatColor.RESET);
        
        // Apply to tab list if enabled
        boolean showInTab = getConfig().getBoolean("display.tab-list", true);
        if (showInTab) {
            player.setPlayerListName(color + player.getName() + ChatColor.RESET);
        } else {
            player.setPlayerListName(player.getName());
        }
    }
}
