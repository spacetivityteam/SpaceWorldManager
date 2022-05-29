package net.spacetivity.world.scoreboardapi;

import org.bukkit.entity.Player;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.Team;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

public class SidebarManager {

    private final Map<UUID, Tablist> tablistMap = new HashMap<>();
    private final Map<UUID, Sidebar> sidebarMap = new HashMap<>();

    public Optional<Sidebar> getSidebar(UUID uniqueId) {
        if (!sidebarMap.containsKey(uniqueId)) return Optional.empty();
        return Optional.of(sidebarMap.get(uniqueId));
    }

    public void setTablist(Player player, Tablist tablist) {
        tablistMap.put(player.getUniqueId(), tablist);
    }

    public void removeTablist(Player player) {
        tablistMap.remove(player.getUniqueId());
    }

    public void updateTablist(Player player) {
        tablistMap.get(player.getUniqueId()).update();
    }

    public void setSidebar(Player player, Sidebar sidebar) {
        getSidebar(player.getUniqueId()).ifPresent(oldSidebar -> oldSidebar.objective.unregister());
        sidebarMap.put(player.getUniqueId(), sidebar);
    }

    public Team getTeam(Scoreboard sb, String Team, String prefix, String suffix) {
        Team team = sb.getTeam(Team);

        if (team == null) {
            team = sb.registerNewTeam(Team);
            team.setAllowFriendlyFire(true);
            team.setCanSeeFriendlyInvisibles(true);
        }

        team.setPrefix(prefix);
        team.setSuffix(suffix);

        return team;
    }

    public Team getTeam(Scoreboard sb, String Team) {
        Team team = sb.getTeam(Team);
        if (team == null) {
            team = sb.registerNewTeam(Team);
            team.setAllowFriendlyFire(true);
            team.setCanSeeFriendlyInvisibles(true);
        }
        return team;
    }

    public String updateTeam(Scoreboard sb, String Team, String prefix, String suffix, org.bukkit.ChatColor entry) {
        Team team = sb.getTeam(Team);
        if (team == null) {
            team = sb.registerNewTeam(Team);
            team.addEntry(entry.toString());
        }

        team.setPrefix(prefix);
        team.setSuffix(suffix);

        return entry.toString();
    }

}
