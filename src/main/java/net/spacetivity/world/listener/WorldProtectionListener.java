package net.spacetivity.world.listener;

import net.spacetivity.world.SpaceWorldManager;
import net.spacetivity.world.configuration.ConfigurationData;
import net.spacetivity.world.message.MessageUtil;
import net.spacetivity.world.scoreboard.PlayerWorldScoreboard;
import net.spacetivity.world.scoreboardapi.Sidebar;
import net.spacetivity.world.settings.WorldSettingsFileManager;
import org.bukkit.GameMode;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPhysicsEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.player.PlayerChangedWorldEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.inventory.ItemStack;

public record WorldProtectionListener(WorldSettingsFileManager worldSettingsFileManager) implements Listener {

    @EventHandler(priority = EventPriority.LOWEST)
    public void onJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        ConfigurationData config = SpaceWorldManager.getInstance().getConfigurationFileManager().getConfig();

        ItemStack worldItem = SpaceWorldManager.getInstance().giveWorldItem(player);

        if (config.isGiveWorldItemOnJoin() && !player.getInventory().contains(worldItem.getType()))
            player.getInventory().addItem(worldItem);

        if (config.isShowScoreboard()) {
            PlayerWorldScoreboard scoreboard = new PlayerWorldScoreboard(player);
            scoreboard.initSidebar();
            SpaceWorldManager.getInstance().getSidebarManager().setSidebar(player, scoreboard);
        }
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onWorldJoin(PlayerChangedWorldEvent event) {
        Player player = event.getPlayer();
        String forceGameMode = worldSettingsFileManager.getWorldSettings(player.getWorld().getName()).getForceGameMode();
        player.setGameMode(GameMode.valueOf(forceGameMode));

        MessageUtil.send(player, "messages.join.world", player.getWorld().getName());

        if (SpaceWorldManager.getInstance().getConfigurationFileManager().getConfig().isShowScoreboard())
            SpaceWorldManager.getInstance().getSidebarManager().getSidebar(player.getUniqueId()).ifPresent(Sidebar::update);
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onBlockPlace(BlockPlaceEvent event) {
        Player player = event.getPlayer();
        String worldName = player.getWorld().getName();

        if (hasBypassPermission(player)) {
            event.setCancelled(false);
            return;
        }

        if (SpaceWorldManager.getInstance().getWorldUtils().hasWorldSettings(worldName))
            event.setCancelled(SpaceWorldManager.getInstance().getWorldUtils().canBuild(player.getUniqueId(), player.getWorld().getName()));
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onBlockBreak(BlockBreakEvent event) {
        Player player = event.getPlayer();
        String worldName = player.getWorld().getName();

        if (hasBypassPermission(player)) {
            event.setCancelled(false);
            return;
        }

        if (SpaceWorldManager.getInstance().getWorldUtils().hasWorldSettings(worldName))
            event.setCancelled(SpaceWorldManager.getInstance().getWorldUtils().canBuild(player.getUniqueId(), player.getWorld().getName()));
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onBlockPhysics(BlockPhysicsEvent event) {
        event.setCancelled(SpaceWorldManager.getInstance().getConfigurationFileManager().getConfig().isStopLag());
    }

    private boolean hasBypassPermission(Player player) {
        return player.hasPermission(SpaceWorldManager.getInstance().getConfigurationFileManager().getConfig().getBypassPermission());
    }
}
