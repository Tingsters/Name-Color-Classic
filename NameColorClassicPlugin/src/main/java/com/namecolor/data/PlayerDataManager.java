package com.namecolor.data;

import org.bukkit.Bukkit;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Manages persistent storage of player color preferences with in-memory caching
 */
public class PlayerDataManager {
    
    private final JavaPlugin plugin;
    private File dataFile;
    private FileConfiguration dataConfig;
    
    // In-memory cache for fast lookups
    private final Map<UUID, String> colorCache = new HashMap<>();
    
    // Track if an async save is already pending
    private volatile boolean saveScheduled = false;
    
    public PlayerDataManager(JavaPlugin plugin) {
        this.plugin = plugin;
        setupDataFile();
        loadCache();
    }
    
    /**
     * Initialize the playerdata.yml file
     */
    private void setupDataFile() {
        dataFile = new File(plugin.getDataFolder(), "playerdata.yml");
        
        if (!dataFile.exists()) {
            try {
                dataFile.createNewFile();
            } catch (IOException e) {
                plugin.getLogger().severe(String.format("Could not create playerdata.yml file: %s", e.getMessage()));
            }
        }
        
        dataConfig = YamlConfiguration.loadConfiguration(dataFile);
    }
    
    /**
     * Load all player colors into memory cache
     */
    private void loadCache() {
        colorCache.clear();
        ConfigurationSection playersSection = dataConfig.getConfigurationSection("players");
        
        if (playersSection != null) {
            for (String uuidString : playersSection.getKeys(false)) {
                try {
                    UUID uuid = UUID.fromString(uuidString);
                    String color = playersSection.getString(uuidString + ".color");
                    if (color != null) {
                        colorCache.put(uuid, color);
                    }
                } catch (IllegalArgumentException e) {
                    plugin.getLogger().warning(String.format("Invalid UUID in playerdata.yml: %s", uuidString));
                }
            }
        }
        
        plugin.getLogger().info(String.format("Loaded %d player colors into cache", colorCache.size()));
    }
    
    /**
     * Save a player's color preference
     * @param uuid Player UUID
     * @param colorName The name of the color
     */
    public void savePlayerColor(UUID uuid, String colorName) {
        synchronized (this) {
            // Update cache
            colorCache.put(uuid, colorName);
            
            // Update config
            dataConfig.set("players." + uuid.toString() + ".color", colorName);
        }
        
        // Save asynchronously
        saveAsync();
    }
    
    /**
     * Get a player's saved color
     * @param uuid Player UUID
     * @return The color name or null if not found
     */
    public String getPlayerColor(UUID uuid) {
        // Read from cache instead of disk
        return colorCache.get(uuid);
    }
    
    /**
     * Check if a player has a saved color
     * @param uuid Player UUID
     * @return true if the player has a saved color
     */
    public boolean hasPlayerColor(UUID uuid) {
        return colorCache.containsKey(uuid);
    }
    
    /**
     * Remove a player's color preference
     * @param uuid Player UUID
     */
    public void removePlayerColor(UUID uuid) {
        synchronized (this) {
            // Remove from cache
            colorCache.remove(uuid);
            
            // Remove from config
            dataConfig.set("players." + uuid.toString(), null);
        }
        
        // Save asynchronously
        saveAsync();
    }
    
    /**
     * Save the data file asynchronously to avoid blocking main thread
     * Uses debouncing to prevent multiple concurrent writes
     */
    private void saveAsync() {
        synchronized (this) {
            // If a save is already scheduled, skip this one
            if (saveScheduled) {
                return;
            }
            
            saveScheduled = true;
        }
        
        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            synchronized (this) {
                try {
                    // Serialize YAML right before writing to capture ALL changes
                    final String yamlContent;
                    yamlContent = dataConfig.saveToString();
                    
                    // Validate YAML content before writing
                    if (yamlContent == null || yamlContent.trim().isEmpty()) {
                        plugin.getLogger().severe("Attempted to save empty/null YAML content - aborting to prevent data loss");
                        saveScheduled = false;  // Must reset flag on early return
                        return;
                    }
                    
                    // Write to main file first
                    java.nio.file.Files.write(dataFile.toPath(), yamlContent.getBytes(java.nio.charset.StandardCharsets.UTF_8));
                } catch (IOException e) {
                    plugin.getLogger().severe(String.format("Could not save playerdata.yml file: %s", e.getMessage()));
                } finally {
                    // Allow next save to proceed
                    saveScheduled = false;
                }
            }
        });
    }
    
    /**
     * Save the data file synchronously (used on shutdown)
     */
    public void saveSync() {
        // Wait for any pending async save to complete (max 5 seconds)
        int attempts = 0;
        while (saveScheduled && attempts < 50) {
            try {
                Thread.sleep(100);
                attempts++;
            } catch (InterruptedException e) {
                break;
            }
        }
        
        if (saveScheduled) {
            plugin.getLogger().warning("Async save still pending during shutdown - forcing synchronous save");
        }
        
        synchronized (this) {
            try {
                dataConfig.save(dataFile);
            } catch (IOException e) {
                plugin.getLogger().severe(String.format("Could not save playerdata.yml file: %s", e.getMessage()));
            }
        }
    }
    
    /**
     * Reload the data file from disk and refresh cache
     */
    public void reload() {
        synchronized (this) {
            dataConfig = YamlConfiguration.loadConfiguration(dataFile);
            loadCache();
        }
    }
}
