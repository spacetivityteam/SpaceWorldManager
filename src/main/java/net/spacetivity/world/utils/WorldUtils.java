package net.spacetivity.world.utils;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import net.spacetivity.world.password.HashingManager;
import net.spacetivity.world.password.PasswordContainer;
import net.spacetivity.world.settings.WorldSettings;
import net.spacetivity.world.settings.WorldSettingsFileManager;
import lombok.SneakyThrows;
import net.spacetivity.world.SpaceWorldManager;
import net.spacetivity.world.generation.WorldBuilder;
import net.spacetivity.world.generation.WorldTemplate;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.World;
import org.bukkit.WorldCreator;
import org.bukkit.entity.Player;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.NoSuchAlgorithmException;
import java.time.Duration;
import java.util.*;
import java.util.stream.Collectors;

public class WorldUtils {

    public final Cache<UUID, Boolean> worldDeleteConfirmation = CacheBuilder.newBuilder().expireAfterWrite(Duration.ofSeconds(10)).build();
    public final Cache<UUID, String> worldPasswordConfirmation = CacheBuilder.newBuilder().expireAfterWrite(Duration.ofSeconds(20)).build();
    private final WorldSettingsFileManager worldSettingsFileManager = SpaceWorldManager.getInstance().getWorldSettingsFileManager();

    /**
     * @return a {@link List<File>} with all world directories from all minecraft worlds stored on the server
     */
    public List<File> getAllWorldFiles() {
        return Arrays.stream(Objects.requireNonNull(Bukkit.getWorldContainer().listFiles())).filter(File::isDirectory)
                .collect(Collectors.toList());
    }

    /**
     * Method to check if the world-folder of a specific world exists
     *
     * @param worldName to identify which world should be checked for a folder
     * @return a {@link Boolean} to get the requester a result on his request
     */
    public boolean isWorldFolderExisting(String worldName) {
        return getAllWorldFiles().stream().anyMatch(file -> file.getName().equalsIgnoreCase(worldName));
    }

    /**
     * Method to just load a world into the Bukkit world cache and NOT put specific data in it like the
     * {@link WorldBuilder} does it
     *
     * @param worldName to identify which world should be loaded into the cache
     */
    public void justLoadWorld(String worldName) {
        new WorldCreator(worldName).createWorld();
    }

    /**
     * Method to ask if there is any Minecraft world which should be considered as the main world, so the user cannot delete it manually.
     *
     * @return an {@link Optional<World>} to give a null-save value to the requester, so he can decide what to do with the result in a
     * secured way
     */
    public Optional<World> getMinecraftMainWorld() {
        String mainWorldName = SpaceWorldManager.getInstance().getConfigurationFileManager().getConfig().getMainWorldName();
        World mainWorld = Bukkit.getWorld(mainWorldName);
        return Optional.ofNullable(mainWorld);
    }

    /**
     * Method to create a new Minecraft world with the {@link WorldBuilder}, secure it with a password and load it in the Bukkit world cache
     *
     * @param executor    is the player who sends the world creating request
     * @param worldName   to give the world a name, so it can later be identified
     * @param template    to decide which type of the {@link WorldTemplate} this world should be
     * @param usePassword to decide if the world should be protected with a password
     * @param password    if 'usePassword' = true, here will be the new password (unencrypted)
     * @return an {@link Optional<World>} to give a save result of the created world to the requester
     */
    public Optional<World> createWorld(Player executor, String worldName, String template, boolean usePassword, String... password) {
        if (getAllWorldFiles().stream().anyMatch(file -> file.getName().equalsIgnoreCase(worldName))) {
            executor.sendMessage(SpaceWorldManager.PREFIX + "There is already an existing world with this name!");
            return Optional.empty();
        }

        if (Arrays.stream(WorldTemplate.values()).noneMatch(pattern -> pattern.name().equalsIgnoreCase(template.toUpperCase() + "_WORLD"))) {
            executor.sendMessage(SpaceWorldManager.PREFIX + "Please choose NORMAL, NETHER, END, VOID, FLAT as the pattern.");
            return Optional.empty();
        }

        executor.sendMessage(SpaceWorldManager.PREFIX + "Starting world generation...");
        long startTime = System.currentTimeMillis();

        WorldTemplate worldTemplate = WorldTemplate.valueOf(template.toUpperCase() + "_WORLD");
        World newWorld;

        if (!usePassword)
            newWorld = new WorldBuilder(worldName, worldTemplate).build(false);
        else
            newWorld = new WorldBuilder(worldName, worldTemplate).build(true, password);

        if (!hasWorldSettings(worldName))
            createSettingsForWorld(executor, worldName);

        long endTime = System.currentTimeMillis() - startTime;
        executor.sendMessage(SpaceWorldManager.PREFIX + "Finished generation for world §f" + worldName + "§7. (in §a" + endTime + "§7ms)");
        return Optional.of(newWorld);
    }

    @SneakyThrows
    public boolean hasWorldSettings(String worldName) {
        return worldSettingsFileManager.getWorldSettings(worldName) != null;
    }

    @SneakyThrows
    public void createSettingsForWorld(Player player, String worldName) {
        worldSettingsFileManager.createWorldSettingsFile(worldName, player, GameMode.CREATIVE, true);
    }

    @SneakyThrows
    public void createSettingsForWorld(String worldName) {
        worldSettingsFileManager.createWorldSettingsFile(worldName, null, GameMode.CREATIVE, false);
    }

    public WorldSettings getSettings(String worldName) {
        return worldSettingsFileManager.getWorldSettings(worldName);
    }

    public void insertPasswordToWorld(String worldName, String rawPassword) throws NoSuchAlgorithmException {
        PasswordContainer passwordContainer = new PasswordContainer();
        HashingManager hashingManager = SpaceWorldManager.getInstance().getHashingManager();
        String salt = hashingManager.getSalt();
        passwordContainer.setHashedPassword(hashingManager.createHashedPassword(rawPassword, salt));
        passwordContainer.setSalt(salt);

        World world = Bukkit.getWorld(worldName);
        assert world != null;
        File worldFolder = world.getWorldFolder();
        File passwordFile = Paths.get(worldFolder.getPath() + "/" + worldName + "_password.json").toFile();
        SpaceWorldManager.getInstance().getFileUtils().saveFile(passwordFile, passwordContainer);
    }

    public void deletePasswordFromWorld(String worldName) throws IOException {
        File worldFile = getWorldFolder(worldName);
        File passwordFile = Paths.get(worldFile.getPath() + "/" + worldName + "_password.json").toFile();
        Files.deleteIfExists(passwordFile.toPath());
    }

    public PasswordContainer getPasswordFromWorld(File worldFile) {
        File file = Paths.get(worldFile.getPath() + "/" + worldFile.getName() + "_password.json").toFile();
        return SpaceWorldManager.getInstance().getFileUtils().readFile(file, PasswordContainer.class);
    }

    public File getWorldFolder(String name) {
        return getAllWorldFiles().stream().filter(file -> file.getName().equalsIgnoreCase(name)).findFirst().orElse(null);
    }

    public boolean isWorldLocked(File worldFile) {
        return Paths.get(worldFile.getPath() + "/" + worldFile.getName() + "_password.json").toFile().exists();
    }

    public boolean isWorldLocked(String worldName) {
        return Paths.get(getWorldFolder(worldName) + "/" + worldName + "_password.json").toFile().exists();
    }

    public boolean canBuild(UUID uniqueId, String worldName) {
        return !worldSettingsFileManager.getWorldSettings(worldName).getTrustedBuilders().contains(uniqueId.toString());
    }

    public boolean isWorldLoaded(File worldFile) {
        return Bukkit.getWorld(worldFile.getName()) != null;
    }

    public boolean isWorldLoaded(String worldName) {
        return Bukkit.getWorld(worldName) != null;
    }
}
