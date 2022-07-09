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
        setLine(11, "    ");
        setLine(10, "§b§lLocked");
        setLine(9, "§f" + worldUtils.isWorldLocked(player.getWorld().getName()));
        setLine(8, "   ");
        setLine(7, "§b§lStatus");
        setLine(6, "§f" + worldUtils.getSettings(player.getWorld().getName()).getState().getName());
        setLine(5, "  ");
        setLine(4, "§b§lWorld");
        setLine(3, "§f" + player.getWorld().getName());
        setLine(2, " ");
        setLine(1, "§b§lCreator");
        setLine(0, "§f" + worldUtils.getCreator(player.getWorld().getName()));
    }

    @Override
    public void update() {
        setLine(9, "§f" + worldUtils.isWorldLocked(player.getWorld().getName()));
        setLine(6, "§f" + worldUtils.getSettings(player.getWorld().getName()).getState().getName());
        setLine(3, "§f" + player.getWorld().getName());
        setLine(0, "§f" + worldUtils.getCreator(player.getWorld().getName()));
    }
}
