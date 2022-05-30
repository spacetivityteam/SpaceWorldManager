package net.spacetivity.world.configuration;

import net.spacetivity.world.utils.FileUtils;
import lombok.Getter;
import net.spacetivity.world.SpaceWorldManager;
import org.bukkit.Material;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class ConfigurationFileManager {

    private final FileUtils fileUtils = SpaceWorldManager.getInstance().getFileUtils();

    @Getter
    private final Path configPath = Paths.get(SpaceWorldManager.getInstance().getDataFolder().getPath() + "/config.json");

    public void createFile() {
        if (Files.exists(configPath)) return;

        try {
            if (!Files.exists(Paths.get(SpaceWorldManager.getInstance().getDataFolder().getPath())))
                Files.createDirectory(Paths.get(SpaceWorldManager.getInstance().getDataFolder().getPath()));
        } catch (IOException e) {
            e.printStackTrace();
        }

        File configFile = configPath.toFile();
        ConfigurationData configData = new ConfigurationData();
        configData.setBypassPermission("swm.bypass");
        configData.setMainWorldName("world");
        configData.setSaveOnShutdown(true);
        configData.setStopLag(false);
        configData.setShowCurrentWorldActionBar(true);
        configData.setShowScoreboard(true);
        configData.setGiveWorldItemOnJoin(true);
        configData.setWorldItemMaterial(Material.NETHER_STAR.name());
        fileUtils.saveFile(configFile, configData);
    }

    public ConfigurationData getConfig() {
        return fileUtils.readFile(configPath.toFile(), ConfigurationData.class);
    }

}
