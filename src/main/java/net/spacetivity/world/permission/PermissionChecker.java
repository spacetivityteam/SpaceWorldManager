package net.spacetivity.world.permission;

import net.spacetivity.world.SpaceWorldManager;
import org.bukkit.entity.Player;

public class PermissionChecker {

    private static boolean hasByPassPermission(Player player){
        return player.hasPermission(SpaceWorldManager.getInstance().getConfigurationFileManager().getConfig().getBypassPermission());
    }

    public static boolean hasPermission(Player player, String neededPermission) {
        return hasByPassPermission(player) || (player.hasPermission(neededPermission));
    }

}
