package com.namecolor.utils;

import org.bukkit.ChatColor;
import org.bukkit.configuration.file.FileConfiguration;

/**
 * Utility class to format plugin messages with the configured prefix
 */
public class MessageFormatter {
    
    private final FileConfiguration config;
    
    public MessageFormatter(FileConfiguration config) {
        this.config = config;
    }
    
    /**
     * Format a message with the plugin prefix from config
     * @param messageKey The key from config.yml messages section
     * @return Formatted message with prefix
     */
    public String formatMessage(String messageKey) {
        String prefix = ChatColor.translateAlternateColorCodes('§', 
                config.getString("message-format.prefix", "§1[§9NameColor§1]§3"));
        String message = config.getString("messages." + messageKey, "");
        return prefix + " " + ChatColor.translateAlternateColorCodes('&', message);
    }
    
    /**
     * Format a message with the plugin prefix and replace a placeholder
     * @param messageKey The key from config.yml messages section
     * @param placeholder The placeholder to replace (e.g., "{color}")
     * @param value The value to replace it with
     * @return Formatted message with prefix and replaced placeholder
     */
    public String formatMessage(String messageKey, String placeholder, String value) {
        String message = formatMessage(messageKey);
        return message.replace(placeholder, value);
    }
    
    /**
     * Format a message with the plugin prefix and replace multiple placeholders
     * @param messageKey The key from config.yml messages section
     * @param replacements Pairs of placeholder and value (placeholder1, value1, placeholder2, value2, ...)
     * @return Formatted message with prefix and replaced placeholders
     */
    public String formatMessage(String messageKey, String... replacements) {
        String message = formatMessage(messageKey);
        for (int i = 0; i < replacements.length - 1; i += 2) {
            message = message.replace(replacements[i], replacements[i + 1]);
        }
        return message;
    }
    
    /**
     * Get the configured prefix
     * @return The formatted prefix string
     */
    public String getPrefix() {
        return ChatColor.translateAlternateColorCodes('§', 
                config.getString("message-format.prefix", "§1[§9NameColor§1]§3"));
    }
    
    /**
     * Get a help message from config
     * @param key The key from config.yml help section
     * @return The formatted help message
     */
    public String getHelpMessage(String key) {
        String message = config.getString("help." + key, "");
        return ChatColor.translateAlternateColorCodes('§', message);
    }
}
