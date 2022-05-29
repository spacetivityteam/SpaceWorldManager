package net.spacetivity.world.scoreboardapi;

import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.Scoreboard;

import java.util.Objects;

public abstract class Tablist {

    @Getter
    private final Scoreboard scoreboard;
    @Getter
    private final Player player;

    public Tablist(Player player) {
        this.player = player;

        if (player.getScoreboard().equals(Objects.requireNonNull(Bukkit.getScoreboardManager()).getMainScoreboard()))
            player.setScoreboard(Bukkit.getScoreboardManager().getNewScoreboard());

        this.scoreboard = player.getScoreboard();
    }

    public abstract void init();

    public abstract void update();

}
