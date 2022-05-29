package net.spacetivity.world.commands;

import net.spacetivity.world.SpaceWorldManager;
import net.spacetivity.world.generation.WorldTemplate;
import net.spacetivity.world.message.Message;
import net.spacetivity.world.message.MessageUtil;
import net.spacetivity.world.password.PasswordContainer;
import net.spacetivity.world.permission.PermissionChecker;
import net.spacetivity.world.settings.WorldSettings;
import net.spacetivity.world.settings.WorldSettingsFileManager;
import net.spacetivity.world.utils.PageConverter;
import net.spacetivity.world.utils.WorldUtils;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.TextComponent;
import org.apache.commons.io.FileUtils;
import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.command.*;
import org.bukkit.entity.Player;
import org.bukkit.generator.WorldInfo;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.util.*;
import java.util.function.Consumer;
import java.util.logging.Level;
import java.util.stream.Collectors;

public class WorldCommand implements CommandExecutor, TabCompleter {

    private final WorldUtils worldUtils = SpaceWorldManager.getInstance().getWorldUtils();
    private final WorldSettingsFileManager worldSettingsFileManager = SpaceWorldManager.getInstance().getWorldSettingsFileManager();
    private final PageConverter pageConverter = SpaceWorldManager.getInstance().getPageConverter();


    public WorldCommand(JavaPlugin plugin) {
        PluginCommand command = plugin.getCommand("swm");
        assert command != null;
        command.setExecutor(this);
        command.setTabCompleter(this);
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (!(sender instanceof final Player player)) {
            Bukkit.getLogger().log(Level.INFO, "You must be a player to execute that command!");
            return true;
        }

        if (args.length == 1) {
            if (args[0].equalsIgnoreCase("templates")) {

                if (!PermissionChecker.hasPermission(player, "awm.command.templates")) {
                    player.sendMessage(SpaceWorldManager.NO_PERMISSION);
                    return true;
                }

                player.sendMessage(SpaceWorldManager.PREFIX + "All available world-templates:");
                StringJoiner stringJoiner = new StringJoiner(", ");
                Arrays.stream(WorldTemplate.values()).map(worldTemplate -> worldTemplate.name().split("_")[0]).toList().forEach(stringJoiner::add);
                player.sendMessage(SpaceWorldManager.PREFIX + stringJoiner);
                return true;
            }

            if (args[0].equalsIgnoreCase("list")) {

                if (!PermissionChecker.hasPermission(player, "awm.command.list")) {
                    player.sendMessage(SpaceWorldManager.NO_PERMISSION);
                    return true;
                }

                List<String> mapNames = worldUtils.getAllWorldFiles().stream().map(File::getName).collect(Collectors.toList());
                pageConverter.showPage(player, mapNames, "All worlds: ", 5, 1, worldNames -> worldNames.forEach(worldName -> {
                    boolean worldLoaded = worldUtils.isWorldLoaded(worldName);
                    boolean worldLocked = worldUtils.isWorldLocked(worldName);
                    player.sendMessage("§f- §7" + worldName + " " + (worldLoaded ? "§aLOADED" : "§cUNLOADED") +
                            " " + (worldLocked ? "§4LOCKED" : ""));
                }));
                return true;
            }

            if (args[0].equalsIgnoreCase("info")) {

                if (!PermissionChecker.hasPermission(player, "awm.command.info")) {
                    player.sendMessage(SpaceWorldManager.NO_PERMISSION);
                    return true;
                }

                World world = player.getWorld();
                player.sendMessage(SpaceWorldManager.PREFIX + "You are in the world: §f" + world.getName());
                if (worldUtils.isWorldLocked(worldUtils.getWorldFolder(world.getName())))
                    player.sendMessage(SpaceWorldManager.PREFIX + "This world is §clocked§7.");

                return true;
            }

        } else if (args.length == 2 && args[0].equalsIgnoreCase("list")) {

            if (!PermissionChecker.hasPermission(player, "awm.command.list")) {
                player.sendMessage(SpaceWorldManager.NO_PERMISSION);
                return true;
            }

            int pageNumber;

            try {
                pageNumber = Integer.parseInt(args[1]);
            } catch (NumberFormatException e) {
                player.sendMessage(SpaceWorldManager.PREFIX + "Please enter a integer!");
                return true;
            }

            List<String> mapNames = worldUtils.getAllWorldFiles().stream().map(File::getName).collect(Collectors.toList());
            pageConverter.showPage(player, mapNames, "All worlds: ", 5, pageNumber, worldNames -> worldNames.forEach(worldName -> {
                boolean worldLoaded = worldUtils.isWorldLoaded(worldName);
                boolean worldLocked = worldUtils.isWorldLocked(worldName);
                player.sendMessage("§f- §7" + worldName + " " + (worldLoaded ? "§aLOADED" : "§cUNLOADED") +
                        " " + (worldLocked ? "§4LOCKED" : ""));
            }));

        } else if ((args.length == 2 || args.length == 3) && args[0].equalsIgnoreCase("join")) {

            if (!PermissionChecker.hasPermission(player, "awm.command.join")) {
                player.sendMessage(SpaceWorldManager.NO_PERMISSION);
                return true;
            }

            final String worldName = args[1];

            if (!worldUtils.isWorldLoaded(worldName)) {
                player.sendMessage(SpaceWorldManager.PREFIX + "This world doesn't exist.");
                return true;
            }

            checkIfPasswordMatches(args, player, worldName, unused -> {
                Location spawnLocation = Objects.requireNonNull(Bukkit.getWorld(worldName)).getSpawnLocation();
                player.teleport(spawnLocation);
                if (worldUtils.worldPasswordConfirmation.getIfPresent(player.getUniqueId()) != null)
                    worldUtils.worldPasswordConfirmation.invalidate(player.getUniqueId());
            });

        } else if ((args.length == 2 || args.length == 3) && args[0].equalsIgnoreCase("delete")) {

            if (!PermissionChecker.hasPermission(player, "awm.command.delete")) {
                player.sendMessage(SpaceWorldManager.NO_PERMISSION);
                return true;
            }

            final String worldName = args[1];

            if (!worldUtils.isWorldLoaded(worldName)) {
                player.sendMessage(SpaceWorldManager.PREFIX + "This world doesn't exist.");
                return true;
            }

            assert worldUtils.getMinecraftMainWorld().isPresent();
            World mainWorld = worldUtils.getMinecraftMainWorld().get();

            if (mainWorld.getName().equalsIgnoreCase(worldName)) {
                player.sendMessage(SpaceWorldManager.PREFIX + "You can't delete the main world ingame.");
                return true;
            }

            if (worldUtils.worldDeleteConfirmation.getIfPresent(player.getUniqueId()) == null) {
                player.sendMessage(SpaceWorldManager.PREFIX + "Are you sure that you want delete the world §f" + worldName + "§7?");
                player.sendMessage(SpaceWorldManager.PREFIX + "If your decision is made, type this command again. (lasts §f60 §7seconds)");
                worldUtils.worldDeleteConfirmation.put(player.getUniqueId(), true);
                return true;
            }

            checkIfPasswordMatches(args, player, worldName, unused -> {
                worldUtils.getMinecraftMainWorld().ifPresent(world -> Objects.requireNonNull(Bukkit.getWorld(worldName))
                        .getPlayers().forEach(current -> current.teleport(world.getSpawnLocation())));

                FileUtils.deleteQuietly(worldUtils.getWorldFolder(worldName));
                World world = Bukkit.getWorld(worldName);

                assert world != null;
                Arrays.stream(world.getLoadedChunks()).forEach(Chunk::unload);
                Bukkit.unloadWorld(world, false);

                if (worldUtils.hasWorldSettings(worldName)) {
                    try {
                        worldSettingsFileManager.deleteWorldSettings(worldName);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }

                player.sendMessage(SpaceWorldManager.PREFIX + "World §f" + worldName + " §7was unloaded.");
                player.sendMessage(SpaceWorldManager.PREFIX + "World §f" + worldName + " §7was deleted. Storage file was also destroyed.");
                worldUtils.worldDeleteConfirmation.invalidate(player.getUniqueId());
            });

        } else if ((args.length == 2 || args.length == 3) && args[0].equalsIgnoreCase("import")) {

            if (!PermissionChecker.hasPermission(player, "awm.command.import")) {
                player.sendMessage(SpaceWorldManager.NO_PERMISSION);
                return true;
            }

            String worldName = args[1];

            if (Bukkit.getWorld(worldName) != null) {
                player.sendMessage(SpaceWorldManager.PREFIX + "This world is already imported and loaded.");
                return true;
            }

            if (!worldUtils.isWorldFolderExisting(worldName)) {
                player.sendMessage(SpaceWorldManager.PREFIX + "There is no world folder existing with that name.");
                return true;
            }

            worldUtils.justLoadWorld(worldName);

            if (args.length == 3) {
                String rawPassword = args[2];
                try {
                    worldUtils.insertPasswordToWorld(worldName, rawPassword);
                } catch (NoSuchAlgorithmException e) {
                    e.printStackTrace();
                }
            }

            if (!worldUtils.hasWorldSettings(worldName))
                worldUtils.createSettingsForWorld(player, worldName);

            player.sendMessage(SpaceWorldManager.PREFIX + "World §f" + worldName + " §7was successfully imported.");

        } else if ((args.length == 2 || args.length == 3) && args[0].equalsIgnoreCase("lock")) {

            if (!PermissionChecker.hasPermission(player, "awm.command.lock")) {
                player.sendMessage(SpaceWorldManager.NO_PERMISSION);
                return true;
            }

            String worldName = args[1];

            if (!worldUtils.isWorldLoaded(worldName)) {
                player.sendMessage(SpaceWorldManager.PREFIX + "This world doesn't exist.");
                return true;
            }

            if (worldUtils.isWorldLocked(worldUtils.getWorldFolder(worldName))) {
                player.sendMessage(SpaceWorldManager.PREFIX + "This world is already locked.");
                return true;
            }

            if (args.length == 3) {
                String rawPassword = args[2];
                try {
                    worldUtils.insertPasswordToWorld(worldName, rawPassword);
                } catch (NoSuchAlgorithmException e) {
                    e.printStackTrace();
                }
            }

        } else if ((args.length == 2 || args.length == 3) && args[0].equalsIgnoreCase("unlock")) {

            if (!PermissionChecker.hasPermission(player, "awm.command.unlock")) {
                player.sendMessage(SpaceWorldManager.NO_PERMISSION);
                return true;
            }

            String worldName = args[1];

            if (!worldUtils.isWorldLoaded(worldName)) {
                player.sendMessage(SpaceWorldManager.PREFIX + "This world doesn't exist.");
                return true;
            }

            if (!worldUtils.isWorldLocked(worldUtils.getWorldFolder(worldName))) {
                player.sendMessage(SpaceWorldManager.PREFIX + "This world is not locked.");
                return true;
            }

            checkIfPasswordMatches(args, player, worldName, unused -> {
                try {
                    worldUtils.deletePasswordFromWorld(worldName);
                } catch (IOException e) {
                    e.printStackTrace();
                }

                player.sendMessage(SpaceWorldManager.PREFIX + "Password from world §f" + worldName + " §7was removed.");
            });

        } else if ((args.length == 3 || args.length == 4) && args[0].equalsIgnoreCase("create")) {

            if (!PermissionChecker.hasPermission(player, "awm.command.create")) {
                player.sendMessage(SpaceWorldManager.NO_PERMISSION);
                return true;
            }

            String worldName = args[1];

            Optional<World> newWorld;

            if (args.length == 3)
                newWorld = worldUtils.createWorld(player, worldName, args[2].toUpperCase(), false);
            else
                newWorld = worldUtils.createWorld(player, worldName, args[2].toUpperCase(), true, args[3]);

            if (args.length == 3)
                newWorld.ifPresent(world -> player.spigot().sendMessage(makeComponent(newWorld.get())));

        } else if (args.length == 3) {
            Player target = Bukkit.getPlayer(args[1]);

            if (target == null) {
                player.sendMessage(SpaceWorldManager.PREFIX + "This player is currently offline!");
                return true;
            }

            String worldName = args[2];

            if (!worldUtils.isWorldLoaded(worldName)) {
                player.sendMessage(SpaceWorldManager.PREFIX + "This world doesn't exist.");
                return true;
            }

            String trustMode = args[0];

            if (trustMode.equalsIgnoreCase("trust")) {

                if (!PermissionChecker.hasPermission(player, "awm.command.trust")) {
                    player.sendMessage(SpaceWorldManager.NO_PERMISSION);
                    return true;
                }

                WorldSettings worldSettings = worldSettingsFileManager.getWorldSettings(worldName);

                if (worldSettings.getTrustedBuilders().contains(target.getUniqueId().toString())) {
                    player.sendMessage(SpaceWorldManager.PREFIX + "This player is already trusted.");
                    return true;
                }

                worldSettings.getTrustedBuilders().add(target.getUniqueId().toString());
                worldSettingsFileManager.updateSettingsForWorld(worldName, worldSettings);
                player.sendMessage(SpaceWorldManager.PREFIX + "Player §f" + target.getName() + " §7was trusted for world §f" + worldName + "§7.");

            } else if (trustMode.equalsIgnoreCase("untrust")) {

                if (!PermissionChecker.hasPermission(player, "awm.command.untrust")) {
                    player.sendMessage(SpaceWorldManager.NO_PERMISSION);
                    return true;
                }

                WorldSettings worldSettings = worldSettingsFileManager.getWorldSettings(worldName);

                if (!worldSettings.getTrustedBuilders().contains(target.getUniqueId().toString())) {
                    player.sendMessage(SpaceWorldManager.PREFIX + "This player is not trusted.");
                    return true;
                }

                worldSettings.getTrustedBuilders().remove(target.getUniqueId().toString());
                worldSettingsFileManager.updateSettingsForWorld(worldName, worldSettings);
                player.sendMessage(SpaceWorldManager.PREFIX + "Player §f" + target.getName() + " §7was untrusted for world §f" + worldName + "§7.");

            }

        } else {
            sendUsage(player);
        }

        return true;
    }

    private void checkIfPasswordMatches(@NotNull String[] args, Player player, String worldName, Consumer<Void> response) {
        File worldFolder = worldUtils.getWorldFolder(worldName);

        boolean hasBypassPermission = player.hasPermission(SpaceWorldManager.getInstance().getConfigurationFileManager().getConfig().getBypassPermission());

        if (hasBypassPermission) {
            response.accept(null);
            return;
        }

        boolean worldLocked = worldUtils.isWorldLocked(worldFolder);

        if (args.length == 2 && !worldLocked) {
            response.accept(null);
            return;
        }

        if (args.length == 2) {
            player.sendMessage(SpaceWorldManager.PREFIX + "Permission denied! Please use the correct password to proceed.");
            return;
        }

        if (args.length == 3 && worldLocked) {
            PasswordContainer passwordFromWorld = worldUtils.getPasswordFromWorld(worldFolder);
            String salt = passwordFromWorld.getSalt();
            String possiblePassword = SpaceWorldManager.getInstance().getHashingManager().createHashedPassword(args[2], salt);

            if (!possiblePassword.equalsIgnoreCase(passwordFromWorld.getHashedPassword())) {
                player.sendMessage(SpaceWorldManager.PREFIX + "Wrong password. Entry denied.");
            } else {
                player.sendMessage(SpaceWorldManager.PREFIX + "Access granted!");
                response.accept(null);
            }
        }
    }

    private void sendUsage(Player player) {
        MessageUtil.send(player,"world.command.usage.title");
        MessageUtil.send(player,"world.command.usage.prefix","swm create <Worldname> <Template> [Password]");
        MessageUtil.send(player,"world.command.usage.prefix","swm delete <Worldname> [Password]");
        MessageUtil.send(player,"world.command.usage.prefix","swm import <Worldname> [Password]");
        MessageUtil.send(player,"world.command.usage.prefix","swm join <Worldname> [Password]");
        MessageUtil.send(player,"world.command.usage.prefix","swm lock <WorldName> <Password>");
        MessageUtil.send(player,"world.command.usage.prefix","swm unlock <Worldname> [Password]");
        MessageUtil.send(player,"world.command.usage.prefix","swm trust <Player> <Worldname>");
        MessageUtil.send(player,"world.command.usage.prefix","swm untrust <Player> <Worldname>");
        MessageUtil.send(player,"world.command.usage.prefix","swm list <Page>");
        MessageUtil.send(player,"world.command.usage.prefix","swm list");
        MessageUtil.send(player,"world.command.usage.prefix","swm templates");
        MessageUtil.send(player,"world.command.usage.prefix","swm info");
        MessageUtil.send(player,"world.command.usage.prefix","swm gui");
    }

    @Nullable
    @Override
    public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String alias, @NotNull String[] args) {
        if (args.length == 1)
            return Arrays.asList("create", "delete", "import", "join", "lock", "unlock", "templates", "list", "info", "gui");

        if (args.length == 2 && (args[0].equalsIgnoreCase("join")) || args[0].equalsIgnoreCase("delete")
                || args[0].equalsIgnoreCase("lock") || args[0].equalsIgnoreCase("unlock"))
            return Bukkit.getWorlds().stream().map(WorldInfo::getName).toList();

        if (args.length == 2 && args[0].equalsIgnoreCase("import"))
            return worldUtils.getAllWorldFiles().stream().map(File::getName).filter(name -> Bukkit.getWorld(name) == null).toList();

        if (args.length == 3 && args[0].equalsIgnoreCase("create"))
            return Arrays.stream(WorldTemplate.values()).map(worldTemplate -> worldTemplate.name().split("_")[0]).toList();

        return Collections.emptyList();
    }

    private TextComponent makeComponent(World newWorld) {
        TextComponent mainComponent = new TextComponent();
        mainComponent.setText(SpaceWorldManager.PREFIX + "Click ");

        TextComponent firstSubComponent = new TextComponent();
        firstSubComponent.setText("§f§lHERE ");
        firstSubComponent.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/swm join " + newWorld.getName()));
        mainComponent.addExtra(firstSubComponent);

        TextComponent secondSubComponent = new TextComponent();
        secondSubComponent.setText("§7to teleport yourself to the new world.");
        mainComponent.addExtra(secondSubComponent);
        return mainComponent;
    }
}
