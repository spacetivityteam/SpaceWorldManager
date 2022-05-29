package net.spacetivity.world.settings;

import net.spacetivity.world.utils.FileUtils;
import net.spacetivity.world.SpaceWorldManager;
import org.bukkit.GameMode;
import org.bukkit.entity.Player;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

public class WorldSettingsFileManager {

    private final FileUtils fileUtils = SpaceWorldManager.getInstance().getFileUtils();

    public WorldSettingsFileManager() {
        Path path = Paths.get(SpaceWorldManager.getInstance().getDataFolder().getPath() + "/settings");
        if (Files.exists(path)) return;

        try {
            Files.createDirectory(path);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void createWorldSettingsFile(String worldName, Player worldCreator, GameMode forcedGameMode, boolean setCreatorToTrusted) {
        Path worldSettingsFilePath = getWorldSettingsFilePath(worldName);

        File worldSettingsFile = worldSettingsFilePath.toFile();
        WorldSettings settings = new WorldSettings();
        settings.setWorldName(worldName);
        settings.setCreatorName(worldCreator == null ? "-" : worldCreator.getName());
        settings.setForceGameMode(forcedGameMode.name());
        settings.setState(WorldState.IN_PROGRESS);

        List<String> trustedList;

        if (worldCreator != null && setCreatorToTrusted)
            trustedList = List.of(worldCreator.getUniqueId().toString());
        else
            trustedList = List.of();

        settings.setTrustedBuilders(trustedList);

        fileUtils.saveFile(worldSettingsFile, settings);
    }

    public void deleteWorldSettings(String worldName) throws IOException {
        Files.deleteIfExists(getWorldSettingsFilePath(worldName));
    }

    public WorldSettings getWorldSettings(String worldName) {
        return !getWorldSettingsFilePath(worldName).toFile().exists() ? null : fileUtils.readFile(getWorldSettingsFilePath(worldName).toFile(), WorldSettings.class);
    }

    public void updateSettingsForWorld(String worldName, WorldSettings newData) {
        fileUtils.saveFile(getWorldSettingsFilePath(worldName).toFile(), newData);
    }

    private Path getWorldSettingsFilePath(String worldName) {
        return Paths.get(SpaceWorldManager.getInstance().getDataFolder().getPath()
                + "/settings/" + worldName.toLowerCase() + "_settings.json");
    }
}
