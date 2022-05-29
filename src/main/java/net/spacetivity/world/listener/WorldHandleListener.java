package net.spacetivity.world.listener;

import com.google.common.cache.Cache;
import net.spacetivity.world.SpaceWorldManager;
import net.spacetivity.world.utils.WorldUtils;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;

import java.util.UUID;

public class WorldHandleListener implements Listener {

    private final WorldUtils worldUtils = SpaceWorldManager.getInstance().getWorldUtils();

    @EventHandler
    public void onTypeInChat(AsyncPlayerChatEvent event) {
        Player player = event.getPlayer();
        Cache<UUID, String> worldPasswordConfirmation = worldUtils.worldPasswordConfirmation;
        String[] message = event.getMessage().split(" ");

        if (worldPasswordConfirmation.getIfPresent(player.getUniqueId()) != null) {
            event.setCancelled(true);
            Bukkit.getScheduler().runTask(SpaceWorldManager.getInstance(), () -> player.performCommand("awm join "
                    + worldPasswordConfirmation.getIfPresent(player.getUniqueId()) + " " + message[0]));
        }
    }
}
