package net.spacetivity.world.commands;

import net.spacetivity.world.message.MessageUtil;
import net.spacetivity.world.permission.PermissionChecker;
import net.spacetivity.world.SpaceWorldManager;
import net.spacetivity.world.configuration.ConfigurationData;
import org.bukkit.Bukkit;
import org.bukkit.command.*;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.List;
import java.util.logging.Level;

public class StopLagCommand implements CommandExecutor, TabCompleter {

    public StopLagCommand(JavaPlugin plugin) {
        PluginCommand command = plugin.getCommand("stoplag");
        assert command != null;
        command.setExecutor(this);
        command.setTabCompleter(this);
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (!(sender instanceof final Player player)) {
            Bukkit.getLogger().log(Level.INFO, "You must be a player to execute that command!");
            return true;
        }

        if (PermissionChecker.notHasPermission(player, "swm.command.stoplag")) {
            MessageUtil.send(player, "messages.noPermissions");
            return true;
        }

        if (args.length == 0) {
            ConfigurationData config = SpaceWorldManager.getInstance().getConfigurationFileManager().getConfig();
            boolean stopLag = config.isStopLag();

            config.setStopLag(!stopLag);
            SpaceWorldManager.getInstance().getFileUtils().saveFile(SpaceWorldManager.getInstance().getConfigurationFileManager().getConfigPath().toFile(), config);

            if (config.isStopLag()) {
                Bukkit.getOnlinePlayers().forEach(currentPlayer -> {
                    currentPlayer.sendTitle("§f§lStoplag", "§7Was activated", 10, 2, 10);
                    currentPlayer.sendMessage(SpaceWorldManager.PREFIX + "Stoplag mode was globally §factivated§7.");
                    currentPlayer.sendMessage(SpaceWorldManager.PREFIX + "All physical block activities are now disabled.");
                });
            } else {
                Bukkit.getOnlinePlayers().forEach(currentPlayer -> {
                    currentPlayer.sendTitle("§f§lStoplag", "§7Was deactivated", 10, 2, 10);
                    currentPlayer.sendMessage(SpaceWorldManager.PREFIX + "Stoplag mode was globally §fdeactivated§7.");
                    currentPlayer.sendMessage(SpaceWorldManager.PREFIX + "All physical block activities are now enabled again.");
                });
            }
        } else if (args.length == 1 && args[0].equalsIgnoreCase("status")) {
            ConfigurationData config = SpaceWorldManager.getInstance().getConfigurationFileManager().getConfig();
            player.sendMessage(SpaceWorldManager.PREFIX + "Stoplag is §f" + (config.isStopLag() ? "enabled" : "disabled"));
        }

        return true;
    }

    @Nullable
    @Override
    public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String alias, @NotNull String[] args) {
        if (args.length == 1)
            return List.of("status");

        return Collections.emptyList();
    }
}
