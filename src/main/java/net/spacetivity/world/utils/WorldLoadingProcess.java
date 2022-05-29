package net.spacetivity.world.utils;

import lombok.AllArgsConstructor;
import net.spacetivity.world.SpaceWorldManager;
import org.bukkit.Bukkit;

import java.util.logging.Level;

@AllArgsConstructor
public class WorldLoadingProcess implements Runnable {

    private final WorldUtils worldUtils = SpaceWorldManager.getInstance().getWorldUtils();

    private String worldName;

    @Override
    public void run() {
        Bukkit.getScheduler().runTask(SpaceWorldManager.getInstance(), () -> worldUtils.justLoadWorld(worldName));

        if (!SpaceWorldManager.getInstance().getWorldUtils().hasWorldSettings(worldName))
            worldUtils.createSettingsForWorld(worldName);

        Bukkit.getLogger().log(Level.INFO, "Loaded minecraft world " + worldName + ".");
    }
}