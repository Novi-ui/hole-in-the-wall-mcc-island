package com.hitw.commands;

import com.hitw.HoleInTheWallPlugin;
import com.hitw.arena.Arena;
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
 * Admin command handler for /hitwadmin
 */
public class HitwAdminCommand implements CommandExecutor, TabCompleter {
    
    private final HoleInTheWallPlugin plugin;
    
    public HitwAdminCommand(HoleInTheWallPlugin plugin) {
        this.plugin = plugin;
    }
    
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!sender.hasPermission("hitw.admin")) {
            MessageUtil.sendMessage(sender, plugin.getConfigManager().getMessage("errors.no-permission", "&cYou don't have permission to do that!"));
            return true;
        }
        
        if (args.length == 0) {
            sendHelp(sender);
            return true;
        }
        
        String subCommand = args[0].toLowerCase();
        
        switch (subCommand) {
            case "reload":
                return handleReload(sender);
            case "create":
                return handleCreate(sender, args);
            case "delete":
                return handleDelete(sender, args);
            case "list":
                return handleList(sender);
            case "start":
                return handleStart(sender, args);
            case "stop":
                return handleStop(sender);
            case "forcestart":
                return handleForceStart(sender);
            case "stats":
                return handleStats(sender);
            case "help":
            default:
                sendHelp(sender);
                return true;
        }
    }
    
    private boolean handleReload(CommandSender sender) {
        try {
            plugin.reload();
            MessageUtil.sendMessage(sender, plugin.getConfigManager().getMessage("admin.config-reloaded", "&aConfiguration reloaded!"));
        } catch (Exception e) {
            MessageUtil.sendMessage(sender, "&cFailed to reload configuration: " + e.getMessage());
        }
        return true;
    }
    
    private boolean handleCreate(CommandSender sender, String[] args) {
        if (!(sender instanceof Player)) {
            MessageUtil.sendMessage(sender, "&cThis command can only be used by players!");
            return true;
        }
        
        if (args.length < 2) {
            MessageUtil.sendMessage(sender, "&cUsage: /hitwadmin create <name> [size]");
            return true;
        }
        
        Player player = (Player) sender;
        String arenaName = args[1];
        int size = args.length > 2 ? parseInt(args[2], 15) : 15;
        
        if (plugin.getArenaManager().arenaExists(arenaName)) {
            MessageUtil.sendMessage(sender, "&cArena with that name already exists!");
            return true;
        }
        
        boolean success = plugin.getArenaManager().createArena(arenaName, player.getLocation(), size);
        
        if (success) {
            MessageUtil.sendMessage(sender, plugin.getConfigManager().getMessage("admin.arena-created", "&aArena created successfully!"));
        } else {
            MessageUtil.sendMessage(sender, "&cFailed to create arena!");
        }
        
        return true;
    }
    
    private boolean handleDelete(CommandSender sender, String[] args) {
        if (args.length < 2) {
            MessageUtil.sendMessage(sender, "&cUsage: /hitwadmin delete <name>");
            return true;
        }
        
        String arenaName = args[1];
        
        if (!plugin.getArenaManager().arenaExists(arenaName)) {
            MessageUtil.sendMessage(sender, "&cArena not found!");
            return true;
        }
        
        boolean success = plugin.getArenaManager().deleteArena(arenaName);
        
        if (success) {
            MessageUtil.sendMessage(sender, plugin.getConfigManager().getMessage("admin.arena-deleted", "&aArena deleted successfully!"));
        } else {
            MessageUtil.sendMessage(sender, "&cFailed to delete arena!");
        }
        
        return true;
    }
    
    private boolean handleList(CommandSender sender) {
        List<String> arenaNames = new ArrayList<>(plugin.getArenaManager().getArenaNames());
        
        if (arenaNames.isEmpty()) {
            MessageUtil.sendMessage(sender, "&7No arenas found.");
            return true;
        }
        
        MessageUtil.sendMessage(sender, "&eAvailable Arenas (" + arenaNames.size() + "):");
        for (String name : arenaNames) {
            Arena arena = plugin.getArenaManager().getArena(name);
            String status = arena != null && arena.isEnabled() ? "&aEnabled" : "&cDisabled";
            MessageUtil.sendMessage(sender, "&7  - &e" + name + " " + status);
        }
        
        return true;
    }
    
    private boolean handleStart(CommandSender sender, String[] args) {
        // TODO: Implement manual game start
        MessageUtil.sendMessage(sender, "&7Manual game start not yet implemented.");
        return true;
    }
    
    private boolean handleStop(CommandSender sender) {
        plugin.getGameManager().stopAllGames();
        MessageUtil.sendMessage(sender, plugin.getConfigManager().getMessage("admin.game-stopped", "&aAll games stopped!"));
        return true;
    }
    
    private boolean handleForceStart(CommandSender sender) {
        // TODO: Implement force start
        MessageUtil.sendMessage(sender, "&7Force start not yet implemented.");
        return true;
    }
    
    private boolean handleStats(CommandSender sender) {
        MessageUtil.sendMessage(sender, "&eServer Statistics:");
        MessageUtil.sendMessage(sender, "&7  Active Games: &a" + plugin.getGameManager().getActiveGames().size());
        MessageUtil.sendMessage(sender, "&7  Available Arenas: &a" + plugin.getArenaManager().getArenas().size());
        MessageUtil.sendMessage(sender, "&7  Wall Patterns: &a" + plugin.getWallManager().getAllPatterns().size());
        return true;
    }
    
    private void sendHelp(CommandSender sender) {
        List<String> help = Arrays.asList(
            "&8▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓",
            "&c  Hole in the Wall - Admin Commands",
            "&8▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓",
            "&e  /hitwadmin reload &7- Reload configuration",
            "&e  /hitwadmin create <name> [size] &7- Create arena",
            "&e  /hitwadmin delete <name> &7- Delete arena",
            "&e  /hitwadmin list &7- List all arenas",
            "&e  /hitwadmin start [arena] &7- Start a new game",
            "&e  /hitwadmin stop &7- Stop all active games",
            "&e  /hitwadmin forcestart &7- Force start current game",
            "&e  /hitwadmin stats &7- Show server statistics",
            "&e  /hitwadmin help &7- Show this help message",
            "&8▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓"
        );
        
        MessageUtil.sendMessages(sender, help);
    }
    
    private int parseInt(String str, int defaultValue) {
        try {
            return Integer.parseInt(str);
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }
    
    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        List<String> completions = new ArrayList<>();
        
        if (!sender.hasPermission("hitw.admin")) {
            return completions;
        }
        
        if (args.length == 1) {
            List<String> subcommands = Arrays.asList("reload", "create", "delete", "list", "start", "stop", "forcestart", "stats", "help");
            
            for (String subcommand : subcommands) {
                if (subcommand.toLowerCase().startsWith(args[0].toLowerCase())) {
                    completions.add(subcommand);
                }
            }
        } else if (args.length == 2) {
            if (args[0].equalsIgnoreCase("delete") || args[0].equalsIgnoreCase("start")) {
                for (String arenaName : plugin.getArenaManager().getArenaNames()) {
                    if (arenaName.toLowerCase().startsWith(args[1].toLowerCase())) {
                        completions.add(arenaName);
                    }
                }
            }
        }
        
        return completions;
    }
}