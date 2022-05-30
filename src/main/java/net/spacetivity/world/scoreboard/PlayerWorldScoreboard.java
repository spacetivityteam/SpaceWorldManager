package net.spacetivity.world.scoreboard;

import net.spacetivity.world.SpaceWorldManager;
import net.spacetivity.world.scoreboardapi.Sidebar;
import net.spacetivity.world.utils.WorldUtils;
import org.bukkit.entity.Player;

public class PlayerWorldScoreboard extends Sidebar {

    private final WorldUtils worldUtils = SpaceWorldManager.getInstance().getWorldUtils();

    public PlayerWorldScoreboard(Player player) {
        super(player);
    }

    @Override
    public void initSidebar() {
        setTitle("§b§lWorld Manager");
        setLine(9, "   ");
        setLine(8, "§b§lLocked");
        setLine(7, "§f" + worldUtils.isWorldLocked(player.getWorld().getName()));
        setLine(6, "  ");
        setLine(5, "§b§lStatus");
        setLine(4, "§f" + worldUtils.getSettings(player.getWorld().getName()).getState().getName());
        setLine(3, " ");
        setLine(2, "§b§lWorld");
        setLine(1, "§f" + player.getWorld().getName());
    }

    @Override
    public void update() {
        setLine(7, "§f" + worldUtils.isWorldLocked(player.getWorld().getName()));
        setLine(4, "§f" + worldUtils.getSettings(player.getWorld().getName()).getState().getName());
        setLine(1, "§f" + player.getWorld().getName());
    }
}
