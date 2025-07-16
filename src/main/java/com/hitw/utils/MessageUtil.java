package com.hitw.utils;

import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Utility class for message formatting and sending
 */
public class MessageUtil {
    
    private static final Pattern HEX_PATTERN = Pattern.compile("&#([A-Fa-f0-9]{6})");
    
    /**
     * Format a message with color codes
     */
    public static String format(String message) {
        if (message == null || message.isEmpty()) {
            return "";
        }
        
        // Handle hex colors for 1.16+
        message = formatHexColors(message);
        
        // Handle standard color codes
        return ChatColor.translateAlternateColorCodes('&', message);
    }
    
    /**
     * Format hex colors in message
     */
    private static String formatHexColors(String message) {
        Matcher matcher = HEX_PATTERN.matcher(message);
        StringBuffer buffer = new StringBuffer();
        
        while (matcher.find()) {
            String hexCode = matcher.group(1);
            String replacement = net.md_5.bungee.api.ChatColor.of("#" + hexCode).toString();
            matcher.appendReplacement(buffer, replacement);
        }
        
        matcher.appendTail(buffer);
        return buffer.toString();
    }
    
    /**
     * Send formatted message to player
     */
    public static void sendMessage(Player player, String message) {
        if (player != null && message != null && !message.isEmpty()) {
            player.sendMessage(format(message));
        }
    }
    
    /**
     * Send formatted message to command sender
     */
    public static void sendMessage(CommandSender sender, String message) {
        if (sender != null && message != null && !message.isEmpty()) {
            sender.sendMessage(format(message));
        }
    }
    
    /**
     * Send multiple formatted messages to player
     */
    public static void sendMessages(Player player, List<String> messages) {
        if (player != null && messages != null) {
            for (String message : messages) {
                sendMessage(player, message);
            }
        }
    }
    
    /**
     * Send multiple formatted messages to command sender
     */
    public static void sendMessages(CommandSender sender, List<String> messages) {
        if (sender != null && messages != null) {
            for (String message : messages) {
                sendMessage(sender, message);
            }
        }
    }
    
    /**
     * Send title to player
     */
    public static void sendTitle(Player player, String title, String subtitle, int fadeIn, int stay, int fadeOut) {
        if (player != null) {
            player.sendTitle(
                format(title),
                format(subtitle),
                fadeIn,
                stay,
                fadeOut
            );
        }
    }
    
    /**
     * Send action bar message to player
     */
    public static void sendActionBar(Player player, String message) {
        if (player != null && message != null) {
            player.spigot().sendMessage(
                net.md_5.bungee.api.ChatMessageType.ACTION_BAR,
                new net.md_5.bungee.api.chat.TextComponent(format(message))
            );
        }
    }
    
    /**
     * Replace placeholders in message
     */
    public static String replacePlaceholders(String message, String... replacements) {
        if (message == null || replacements.length % 2 != 0) {
            return message;
        }
        
        for (int i = 0; i < replacements.length; i += 2) {
            message = message.replace(replacements[i], replacements[i + 1]);
        }
        
        return message;
    }
    
    /**
     * Strip color codes from message
     */
    public static String stripColor(String message) {
        if (message == null) {
            return null;
        }
        return ChatColor.stripColor(format(message));
    }
    
    /**
     * Center text for display
     */
    public static String centerText(String text, int lineLength) {
        if (text == null || text.isEmpty()) {
            return text;
        }
        
        String stripped = stripColor(text);
        int textLength = stripped.length();
        
        if (textLength >= lineLength) {
            return text;
        }
        
        int spaces = (lineLength - textLength) / 2;
        StringBuilder builder = new StringBuilder();
        
        for (int i = 0; i < spaces; i++) {
            builder.append(" ");
        }
        
        builder.append(text);
        return builder.toString();
    }
    
    /**
     * Format time in seconds to readable format
     */
    public static String formatTime(int seconds) {
        if (seconds < 60) {
            return seconds + "s";
        } else if (seconds < 3600) {
            int minutes = seconds / 60;
            int remainingSeconds = seconds % 60;
            return minutes + "m " + remainingSeconds + "s";
        } else {
            int hours = seconds / 3600;
            int minutes = (seconds % 3600) / 60;
            int remainingSeconds = seconds % 60;
            return hours + "h " + minutes + "m " + remainingSeconds + "s";
        }
    }
    
    /**
     * Format number with commas
     */
    public static String formatNumber(long number) {
        return String.format("%,d", number);
    }
    
    /**
     * Format percentage
     */
    public static String formatPercentage(double percentage) {
        return String.format("%.1f%%", percentage * 100);
    }
}