package com.hitw.commands;

import com.hitw.HoleInTheWallPlugin;
import com.hitw.utils.MessageUtil;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Main player command handler for /hitw
 */
public class HitwCommand implements CommandExecutor, TabCompleter {
    
    private final HoleInTheWallPlugin plugin;
    
    public HitwCommand(HoleInTheWallPlugin plugin) {
        this.plugin = plugin;
    }
    
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length == 0) {
            sendHelp(sender);
            return true;
        }
        
        String subCommand = args[0].toLowerCase();
        
        switch (subCommand) {
            case "join":
                return handleJoin(sender);
            case "leave":
                return handleLeave(sender);
            case "stats":
                return handleStats(sender, args);
            case "info":
                return handleInfo(sender);
            case "help":
            default:
                sendHelp(sender);
                return true;
        }
    }
    
    /**
     * Handle join command
     */
    private boolean handleJoin(CommandSender sender) {
        if (!(sender instanceof Player)) {
            MessageUtil.sendMessage(sender, plugin.getConfigManager().getMessage("errors.console-not-allowed", "&cThis command can only be used by players!"));
            return true;
        }
        
        Player player = (Player) sender;
        
        if (!player.hasPermission("hitw.join")) {
            MessageUtil.sendMessage(player, plugin.getConfigManager().getMessage("errors.no-permission", "&cYou don't have permission to do that!"));
            return true;
        }
        
        // Try to join a game
        boolean success = plugin.getGameManager().joinGame(player);
        
        if (success) {
            MessageUtil.sendMessage(player, plugin.getConfigManager().getMessage("game.join-success", "&aYou joined the game!"));
        } else {
            MessageUtil.sendMessage(player, plugin.getConfigManager().getMessage("errors.join-failed", "&cCould not join game. No available games or game is full."));
        }
        
        return true;
    }
    
    /**
     * Handle leave command
     */
    private boolean handleLeave(CommandSender sender) {
        if (!(sender instanceof Player)) {
            MessageUtil.sendMessage(sender, plugin.getConfigManager().getMessage("errors.console-not-allowed", "&cThis command can only be used by players!"));
            return true;
        }
        
        Player player = (Player) sender;
        
        boolean success = plugin.getGameManager().leaveGame(player);
        
        if (success) {
            MessageUtil.sendMessage(player, plugin.getConfigManager().getMessage("game.leave-success", "&aYou left the game!"));
        } else {
            MessageUtil.sendMessage(player, plugin.getConfigManager().getMessage("errors.not-in-game", "&cYou are not in a game!"));
        }
        
        return true;
    }
    
    /**
     * Handle stats command
     */
    private boolean handleStats(CommandSender sender, String[] args) {
        if (!sender.hasPermission("hitw.stats")) {
            MessageUtil.sendMessage(sender, plugin.getConfigManager().getMessage("errors.no-permission", "&cYou don't have permission to do that!"));
            return true;
        }
        
        Player target;
        
        if (args.length > 1) {
            // View another player's stats
            if (!sender.hasPermission("hitw.stats.others")) {
                MessageUtil.sendMessage(sender, plugin.getConfigManager().getMessage("errors.no-permission", "&cYou don't have permission to view other players' stats!"));
                return true;
            }
            
            target = plugin.getServer().getPlayer(args[1]);
            if (target == null) {
                MessageUtil.sendMessage(sender, "&cPlayer not found!");
                return true;
            }
        } else {
            // View own stats
            if (!(sender instanceof Player)) {
                MessageUtil.sendMessage(sender, "&cYou must specify a player name when using this from console!");
                return true;
            }
            target = (Player) sender;
        }
        
        // Display stats
        plugin.getPlayerManager().displayStats(sender, target);
        return true;
    }
    
    /**
     * Handle info command
     */
    private boolean handleInfo(CommandSender sender) {
        List<String> info = Arrays.asList(
            "&8▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓",
            "&c  Hole in the Wall - MCC Style Plugin",
            "&8▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓",
            "&e  Version: &a" + plugin.getDescription().getVersion(),
            "&e  Author: &a" + String.join(", ", plugin.getDescription().getAuthors()),
            "&e  Description: &7" + plugin.getDescription().getDescription(),
            "",
            "&e  Game Statistics:",
            "&7  • Active Games: &a" + plugin.getGameManager().getActiveGames().size(),
            "&7  • Available Arenas: &a" + plugin.getArenaManager().getArenas().size(),
            "&7  • Wall Patterns: &a" + plugin.getWallManager().getAllPatterns().size(),
            "",
            "&e  Commands:",
            "&7  • &e/hitw join &7- Join a game",
            "&7  • &e/hitw leave &7- Leave current game",
            "&7  • &e/hitw stats &7- View your statistics",
            "&8▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓"
        );
        
        MessageUtil.sendMessages(sender, info);
        return true;
    }
    
    /**
     * Send help message
     */
    private void sendHelp(CommandSender sender) {
        List<String> help = Arrays.asList(
            "&8▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓",
            "&c  Hole in the Wall - Player Commands",
            "&8▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓",
            "&e  /hitw join &7- Join an available game",
            "&e  /hitw leave &7- Leave your current game",
            "&e  /hitw stats [player] &7- View statistics",
            "&e  /hitw info &7- Show plugin information",
            "&e  /hitw help &7- Show this help message",
            "",
            "&7  Game Objective: Dodge through holes in moving walls!",
            "&7  Last player standing wins!",
            "&8▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓"
        );
        
        MessageUtil.sendMessages(sender, help);
    }
    
    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        List<String> completions = new ArrayList<>();
        
        if (args.length == 1) {
            // First argument - subcommands
            List<String> subcommands = Arrays.asList("join", "leave", "stats", "info", "help");
            
            for (String subcommand : subcommands) {
                if (subcommand.toLowerCase().startsWith(args[0].toLowerCase())) {
                    completions.add(subcommand);
                }
            }
        } else if (args.length == 2 && args[0].equalsIgnoreCase("stats")) {
            // Second argument for stats - player names
            if (sender.hasPermission("hitw.stats.others")) {
                for (Player player : plugin.getServer().getOnlinePlayers()) {
                    if (player.getName().toLowerCase().startsWith(args[1].toLowerCase())) {
                        completions.add(player.getName());
                    }
                }
            }
        }
        
        return completions;
    }
}