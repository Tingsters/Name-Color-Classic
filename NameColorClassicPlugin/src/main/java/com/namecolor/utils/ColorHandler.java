package com.namecolor.utils;

import org.bukkit.ChatColor;

import java.util.HashMap;
import java.util.Map;

/**
 * Utility class to handle color name to ChatColor conversions
 * Only includes the 16 standard Minecraft colors (no formatting codes)
 */
public class ColorHandler {
    
    private static final Map<String, ChatColor> COLOR_MAP = new HashMap<>();
    
    static {
        // Initialize all 16 standard Minecraft colors
        COLOR_MAP.put("black", ChatColor.BLACK);
        COLOR_MAP.put("darkblue", ChatColor.DARK_BLUE);
        COLOR_MAP.put("darkgreen", ChatColor.DARK_GREEN);
        COLOR_MAP.put("darkaqua", ChatColor.DARK_AQUA);
        COLOR_MAP.put("darkred", ChatColor.DARK_RED);
        COLOR_MAP.put("darkpurple", ChatColor.DARK_PURPLE);
        COLOR_MAP.put("gold", ChatColor.GOLD);
        COLOR_MAP.put("gray", ChatColor.GRAY);
        COLOR_MAP.put("darkgray", ChatColor.DARK_GRAY);
        COLOR_MAP.put("blue", ChatColor.BLUE);
        COLOR_MAP.put("green", ChatColor.GREEN);
        COLOR_MAP.put("aqua", ChatColor.AQUA);
        COLOR_MAP.put("red", ChatColor.RED);
        COLOR_MAP.put("lightpurple", ChatColor.LIGHT_PURPLE);
        COLOR_MAP.put("yellow", ChatColor.YELLOW);
        COLOR_MAP.put("white", ChatColor.WHITE);
    }
    
    /**
     * Get ChatColor from a color name (case-insensitive)
     * @param colorName The name of the color
     * @return ChatColor object or null if not found
     */
    public static ChatColor getColorByName(String colorName) {
        if (colorName == null) {
            return null;
        }
        return COLOR_MAP.get(colorName.toLowerCase());
    }
    
    /**
     * Check if a color name is valid
     * @param colorName The name of the color
     * @return true if the color exists
     */
    public static boolean isValidColor(String colorName) {
        return colorName != null && COLOR_MAP.containsKey(colorName.toLowerCase());
    }
    
    /**
     * Get all available color names
     * @return Array of color names
     */
    public static String[] getAllColorNames() {
        return COLOR_MAP.keySet().toArray(new String[COLOR_MAP.size()]);
    }
    
    /**
     * Get color names excluding black
     * @return Array of color names without black
     */
    public static String[] getColorsWithoutBlack() {
        return COLOR_MAP.keySet().stream()
                .filter(name -> !name.equals("black"))
                .toArray(String[]::new);
    }
    
    /**
     * Get the name of a ChatColor
     * @param color The ChatColor
     * @return The color name or null if not found
     */
    public static String getColorName(ChatColor color) {
        for (Map.Entry<String, ChatColor> entry : COLOR_MAP.entrySet()) {
            if (entry.getValue() == color) {
                return entry.getKey();
            }
        }
        return null;
    }
    
    /**
     * Get a formatted list of color names with their actual colors
     * @param colorNames Array of color names to format
     * @return Formatted string with colored names
     */
    public static String getFormattedColorList(String[] colorNames) {
        StringBuilder result = new StringBuilder();
        for (int i = 0; i < colorNames.length; i++) {
            String colorName = colorNames[i];
            ChatColor color = getColorByName(colorName);
            if (color != null) {
                result.append(color).append(colorName).append(ChatColor.RESET);
                if (i < colorNames.length - 1) {
                    result.append(", ");
                }
            }
        }
        return result.toString();
    }
}
