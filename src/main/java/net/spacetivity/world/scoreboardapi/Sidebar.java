package net.spacetivity.world.scoreboardapi;

import com.google.common.base.Splitter;
import net.spacetivity.world.SpaceWorldManager;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.*;

import java.util.Iterator;
import java.util.Objects;

public abstract class Sidebar {

    protected final Scoreboard scoreboard;
    protected final Objective objective;
    protected final Player player;

    public Sidebar(Player player) {
        this.player = player;

        if (player.getScoreboard().equals(Objects.requireNonNull(Bukkit.getScoreboardManager()).getMainScoreboard())) {
            player.setScoreboard(Bukkit.getScoreboardManager().getNewScoreboard());
        }

        this.scoreboard = player.getScoreboard();

        if (this.scoreboard.getObjective("display") != null)
            Objects.requireNonNull(this.scoreboard.getObjective("display")).unregister();

        this.objective = this.scoreboard.registerNewObjective("display", "dummy", "text");
        this.objective.setDisplaySlot(DisplaySlot.SIDEBAR);
    }

    public abstract void initSidebar();

    public void setTitle(String name) {
        objective.setDisplayName(name);
    }

    public boolean hasScore(int scoreId) {
        Score score = objective.getScore("§" + scoreId + "§7");
        return score.isScoreSet();
    }

    public void setLine(int score, String text) {
        Team team = SpaceWorldManager.getInstance().getSidebarManager().getTeam(scoreboard, "x" + score, "", "");
        String entryName;

        setTeamPrefixSuffix(team, text);

        if (score < 10) entryName = "§" + score + "§7";
        else entryName = "§" + getColorCodeByNumber(score) + "§7";
        if (team.hasEntry(entryName)) return;
        team.addEntry(entryName);
        objective.getScore(entryName).setScore(score);
    }

    public void setTeamPrefixSuffix(Team team, String text) {
        if (team == null) return;
        Iterator<String> iterator = Splitter.fixedLength(16).split(text).iterator();
        String prefix = iterator.next();

        if (text.length() > 16) {
            String prefixColor = ChatColor.getLastColors(prefix);
            String suffix = iterator.next();

            if (prefix.endsWith(String.valueOf(ChatColor.COLOR_CHAR))) {
                prefix = prefix.substring(0, prefix.length() - 1);
                team.setPrefix(prefix);
                prefixColor = Objects.requireNonNull(ChatColor.getByChar(suffix.charAt(0))).toString();
                suffix = suffix.substring(1);
            }

            if (suffix.length() > 16) suffix = suffix.substring(0, (13 - prefixColor.length()));
            team.setSuffix(prefixColor.equals("") ? "§r" : prefixColor + suffix);
        }
        team.setPrefix(prefix);
    }

    public final void updateLine(int score, String text) {
        if (hasScore(score)) setTeamPrefixSuffix(player.getScoreboard().getTeam("x" + score), text);
    }

    protected final String getColorCodeByNumber(int number) {
        return switch (number) {
            case 10 -> "a";
            case 11 -> "b";
            case 12 -> "c";
            case 13 -> "d";
            case 14 -> "e";
            case 15 -> "f";
            default -> "z";
        };
    }

    public final void reset() {
        this.scoreboard.clearSlot(DisplaySlot.SIDEBAR);
    }

    public void update() {

    }
}
