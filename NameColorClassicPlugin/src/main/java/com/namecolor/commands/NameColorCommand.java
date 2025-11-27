package com.namecolor.commands;

import com.namecolor.NameColorPlugin;
import com.namecolor.utils.ColorHandler;
import com.namecolor.utils.MessageFormatter;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Handles the /namecolor command
 */
public class NameColorCommand implements CommandExecutor, TabCompleter {
    
    private final NameColorPlugin plugin;
    private MessageFormatter formatter;
    
    public NameColorCommand(NameColorPlugin plugin) {
        this.plugin = plugin;
        this.formatter = new MessageFormatter(plugin.getConfig());
    }
    
    /**
     * Reload the message formatter with updated config
     */
    public void reloadFormatter() {
        this.formatter = new MessageFormatter(plugin.getConfig());
    }
    
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        // Check if reload subcommand
        if (args.length > 0 && args[0].equalsIgnoreCase("reload")) {
            return handleReload(sender);
        }
        
        // Check if help subcommand
        if (args.length > 0 && args[0].equalsIgnoreCase("help")) {
            return handleHelp(sender);
        }
        
        // Check if list subcommand
        if (args.length > 0 && args[0].equalsIgnoreCase("list")) {
            return handleList(sender);
        }
        
        // Check if admin command (2 arguments)
        if (args.length == 2) {
            return handleAdminCommand(sender, args[0], args[1]);
        }
        
        // Color change command - must be a player
        if (!(sender instanceof Player)) {
            if (formatter != null && sender != null) {
                sender.sendMessage(formatter.formatMessage("player-only"));
            }
            return true;
        }
        
        Player player = (Player) sender;
        
        // No arguments -> show help
        if (args.length == 0) {
            return handleHelp(sender);
        }
        
        String colorName = args[0].toLowerCase();
        
        // Check if reset command
        if (colorName.equals("reset")) {
            return handleReset(player);
        }
        
        // Validate color exists
        if (!ColorHandler.isValidColor(colorName)) {
            String[] availableColors = getAvailableColors(player);
            String colorList = ColorHandler.getFormattedColorList(availableColors);
            player.sendMessage(formatter.formatMessage("invalid-color", "{colors}", colorList));
            return true;
        }
        
        // Check permission for specific color
        if (!hasColorPermission(player, colorName)) {
            player.sendMessage(formatter.formatMessage("no-permission", "{color}", colorName));
            return true;
        }
        
        // Apply the color
        ChatColor color = ColorHandler.getColorByName(colorName);
        if (color == null) {
            player.sendMessage(formatter.formatMessage("invalid-color", "{colors}", ColorHandler.getFormattedColorList(getAvailableColors(player))));
            return true;
        }
        
        // Apply color based on config settings
        plugin.applyColorToPlayer(player, colorName);
        
        // Save to data file
        plugin.getPlayerDataManager().savePlayerColor(player.getUniqueId(), colorName);
        
        // Send success message with colored name and colored color name
        String coloredColorName = color + colorName + ChatColor.RESET;
        player.sendMessage(formatter.formatMessage("color-changed", "{color}", coloredColorName, "{name}", player.getDisplayName()));
        
        return true;
    }
    
    /**
     * Handle the reload subcommand
     */
    private boolean handleReload(CommandSender sender) {
        // Check if sender is OP
        if (!sender.isOp()) {
            sender.sendMessage(formatter.formatMessage("reload-no-permission"));
            return true;
        }
        
        // Reload config
        plugin.reloadConfig();
        plugin.getPlayerDataManager().reload();
        
        // Reload formatter with new config
        reloadFormatter();
        
        // Reapply colors to online players
        plugin.reapplyAllColors();
        
        sender.sendMessage(formatter.formatMessage("reload-success"));
        return true;
    }
    
    /**
     * Handle the reset command
     */
    private boolean handleReset(Player player) {
        // Reset display name and tab list name to original
        player.setDisplayName(player.getName());
        player.setPlayerListName(player.getName());
        
        // Remove from data file
        plugin.getPlayerDataManager().removePlayerColor(player.getUniqueId());
        
        // Send success message
        player.sendMessage(formatter.formatMessage("color-reset"));
        
        return true;
    }
    
    /**
     * Handle admin command to change another player's color
     */
    private boolean handleAdminCommand(CommandSender sender, String targetName, String colorName) {
        // Check if sender has admin permission
        if (!sender.isOp() && !sender.hasPermission("namecolor.admin")) {
            sender.sendMessage(formatter.formatMessage("admin-no-permission"));
            return true;
        }
        
        // Get target player
        Player target = plugin.getServer().getPlayer(targetName);
        if (target == null) {
            sender.sendMessage(formatter.formatMessage("player-not-found", "{player}", targetName));
            return true;
        }
        
        colorName = colorName.toLowerCase();
        
        // Check if reset command
        if (colorName.equals("reset")) {
            // Reset target's display name and tab list name
            target.setDisplayName(target.getName());
            target.setPlayerListName(target.getName());
            
            // Remove from data file
            plugin.getPlayerDataManager().removePlayerColor(target.getUniqueId());
            
            // Send messages
            sender.sendMessage(formatter.formatMessage("admin-color-reset", "{player}", target.getName()));
            target.sendMessage(formatter.formatMessage("admin-color-reset-target"));
            
            return true;
        }
        
        // Validate color exists
        if (!ColorHandler.isValidColor(colorName)) {
            String colorList = ColorHandler.getFormattedColorList(ColorHandler.getAllColorNames());
            sender.sendMessage(formatter.formatMessage("invalid-color-admin", "{colors}", colorList));
            return true;
        }
        
        // Apply the color
        ChatColor color = ColorHandler.getColorByName(colorName);
        if (color == null) {
            sender.sendMessage(formatter.formatMessage("invalid-color-admin", "{colors}", ColorHandler.getFormattedColorList(ColorHandler.getAllColorNames())));
            return true;
        }
        
        // Apply color based on config settings
        plugin.applyColorToPlayer(target, colorName);
        
        // Save to data file
        plugin.getPlayerDataManager().savePlayerColor(target.getUniqueId(), colorName);
        
        // Send success messages with colored name and colored color name
        String coloredColorName = color + colorName + ChatColor.RESET;
        sender.sendMessage(formatter.formatMessage("admin-color-changed", "{player}", target.getName(), "{color}", coloredColorName, "{name}", target.getDisplayName()));
        target.sendMessage(formatter.formatMessage("admin-color-changed-target", "{color}", coloredColorName, "{name}", target.getDisplayName()));
        
        return true;
    }
    
    /**
     * Handle the help command
     */
    private boolean handleHelp(CommandSender sender) {
        sender.sendMessage(formatter.getPrefix() + " " + formatter.getHelpMessage("header"));
        sender.sendMessage(formatter.getHelpMessage("change-color"));
        sender.sendMessage(formatter.getHelpMessage("reset-color"));
        sender.sendMessage(formatter.getHelpMessage("list-colors"));
        sender.sendMessage(formatter.getHelpMessage("show-help"));
        
        // Show admin commands if they have permission
        if (sender.isOp() || sender.hasPermission("namecolor.admin")) {
            sender.sendMessage("");
            sender.sendMessage(formatter.getPrefix() + " " + formatter.getHelpMessage("admin-header"));
            sender.sendMessage(formatter.getHelpMessage("admin-change-color"));
            sender.sendMessage(formatter.getHelpMessage("admin-reset-color"));
        }
        
        // Show reload command if they are OP
        if (sender.isOp()) {
            sender.sendMessage(formatter.getHelpMessage("reload-command"));
        }
        
        return true;
    }
    
    /**
     * Handle the list command
     */
    private boolean handleList(CommandSender sender) {
        // Must be a player to check permissions
        if (!(sender instanceof Player)) {
            if (formatter != null && sender != null) {
                sender.sendMessage(formatter.formatMessage("player-only"));
            }
            return true;
        }
        
        Player player = (Player) sender;
        String[] availableColors = getAvailableColors(player);
        
        if (availableColors.length == 0) {
            sender.sendMessage(formatter.formatMessage("no-colors-available"));
            return true;
        }
        
        String colorList = ColorHandler.getFormattedColorList(availableColors);
        sender.sendMessage(formatter.formatMessage("color-list", "{colors}", colorList));
        
        return true;
    }
    
    /**
     * Get available colors for a player based on permissions
     */
    private String[] getAvailableColors(Player player) {
        List<String> availableColors = new ArrayList<>();
        String[] allColors = ColorHandler.getAllColorNames();
        
        for (String color : allColors) {
            if (hasColorPermission(player, color)) {
                availableColors.add(color);
            }
        }
        
        return availableColors.toArray(new String[0]);
    }
    
    /**
     * Check if player has permission to use a specific color
     */
    private boolean hasColorPermission(Player player, String colorName) {
        // OP players have access to all colors
        if (player.isOp()) {
            return true;
        }
        
        // Admin permission grants access to all colors
        if (player.hasPermission("namecolor.admin")) {
            return true;
        }
        
        // Check for all colors permission
        if (player.hasPermission("namecolor.color.all")) {
            return true;
        }
        
        // Check for all colors except black permission
        if (player.hasPermission("namecolor.color.noblack") && !colorName.equals("black")) {
            return true;
        }
        
        // Check individual color permission
        return player.hasPermission("namecolor.color." + colorName);
    }
    
    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        List<String> completions = new ArrayList<>();
        
        if (args.length == 1) {
            // First argument - suggest colors, reset, reload, or player names
            if (sender instanceof Player) {
                Player player = (Player) sender;
                String[] colors = getAvailableColors(player);
                completions.addAll(Arrays.asList(colors));
                completions.add("reset");
            }
            
            // Add help for everyone
            completions.add("help");
            completions.add("list");
            
            // Add reload if sender is OP
            if (sender != null && sender.isOp()) {
                completions.add("reload");
            }
            
            // Add player names if sender has admin permission
            if (sender != null && (sender.isOp() || sender.hasPermission("namecolor.admin"))) {
                plugin.getServer().getOnlinePlayers().forEach(p -> completions.add(p.getName()));
            }
            
            // Filter based on what the player has typed
            String input = args[0].toLowerCase();
            return completions.stream()
                    .filter(s -> s.toLowerCase().startsWith(input))
                    .collect(Collectors.toList());
        } else if (args.length == 2) {
            // Second argument - suggest colors or reset (only for admin commands)
            if (sender.isOp() || sender.hasPermission("namecolor.admin")) {
                completions.addAll(Arrays.asList(ColorHandler.getAllColorNames()));
                completions.add("reset");
                
                // Filter based on what the player has typed
                String input = args[1].toLowerCase();
                return completions.stream()
                        .filter(s -> s.toLowerCase().startsWith(input))
                        .collect(Collectors.toList());
            }
        }
        
        return completions;
    }
}

