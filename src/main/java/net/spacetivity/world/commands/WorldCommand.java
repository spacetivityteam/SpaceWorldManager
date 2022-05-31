package net.spacetivity.world.commands;

import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.TextComponent;
import net.spacetivity.world.SpaceWorldManager;
import net.spacetivity.world.generation.WorldTemplate;
import net.spacetivity.world.inventory.WorldInventory;
import net.spacetivity.world.message.Message;
import net.spacetivity.world.message.MessageUtil;
import net.spacetivity.world.password.PasswordContainer;
import net.spacetivity.world.permission.PermissionChecker;
import net.spacetivity.world.settings.WorldSettings;
import net.spacetivity.world.settings.WorldSettingsFileManager;
import net.spacetivity.world.utils.PageConverter;
import net.spacetivity.world.utils.WorldUtils;
import org.apache.commons.io.FileUtils;
import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.command.*;
import org.bukkit.entity.Player;
import org.bukkit.generator.WorldInfo;
import org.bukkit.inventory.ItemStack;
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
            if (args[0].equalsIgnoreCase("item")) {

                if (PermissionChecker.notHasPermission(player, "swm.item")) {
                    MessageUtil.send(player, "messages.noPermissions");
                    return true;
                }

                ItemStack worldItem = SpaceWorldManager.getInstance().giveWorldItem(player);

                if (player.getInventory().contains(worldItem.getType())) {
                    MessageUtil.send(player, "world.item.alreadyInInventory");
                    return true;
                }

                player.getInventory().addItem(worldItem);

                return true;
            }

            if (args[0].equalsIgnoreCase("gui")) {

                if (PermissionChecker.notHasPermission(player, "swm.command.gui")) {
                    MessageUtil.send(player, "messages.noPermissions");
                    return true;
                }

                WorldInventory.getInventory(player).open(player);
                return true;
            }

            if (args[0].equalsIgnoreCase("templates")) {

                if (PermissionChecker.notHasPermission(player, "swm.command.templates")) {
                    MessageUtil.send(player, "messages.noPermissions");
                    return true;
                }

                player.sendMessage(SpaceWorldManager.PREFIX + "All available world-templates:");
                StringJoiner stringJoiner = new StringJoiner(", ");
                Arrays.stream(WorldTemplate.values()).map(worldTemplate -> worldTemplate.name().split("_")[0]).toList().forEach(stringJoiner::add);
                player.sendMessage(SpaceWorldManager.PREFIX + stringJoiner);
                return true;
            }

            if (args[0].equalsIgnoreCase("list")) {

                if (PermissionChecker.notHasPermission(player, "swm.command.list")) {
                    MessageUtil.send(player, "messages.noPermissions");
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

                if (PermissionChecker.notHasPermission(player, "swm.command.info")) {
                    MessageUtil.send(player, "messages.noPermissions");
                    return true;
                }

                World world = player.getWorld();
                player.sendMessage(SpaceWorldManager.PREFIX + "You are in the world: §f" + world.getName());
                if (worldUtils.isWorldLocked(worldUtils.getWorldFolder(world.getName())))
                    player.sendMessage(SpaceWorldManager.PREFIX + "This world is §clocked§7.");

                return true;
            }

        } else if (args.length == 2 && args[0].equalsIgnoreCase("list")) {

            if (PermissionChecker.notHasPermission(player, "swm.command.list")) {
                MessageUtil.send(player, "messages.noPermissions");
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

            if (PermissionChecker.notHasPermission(player, "swm.command.join")) {
                MessageUtil.send(player, "messages.noPermissions");
                return true;
            }

            final String worldName = args[1];

            if (!worldUtils.isWorldLoaded(worldName)) {
                MessageUtil.send(player, "world.command.create.isExist");
                return true;
            }

            checkIfPasswordMatches(args, player, worldName, unused -> {
                Location spawnLocation = Objects.requireNonNull(Bukkit.getWorld(worldName)).getSpawnLocation();
                player.teleport(spawnLocation);
                if (worldUtils.worldPasswordConfirmation.getIfPresent(player.getUniqueId()) != null)
                    worldUtils.worldPasswordConfirmation.invalidate(player.getUniqueId());
            });

        } else if ((args.length == 2 || args.length == 3) && args[0].equalsIgnoreCase("delete")) {

            if (PermissionChecker.notHasPermission(player, "swm.command.delete")) {
                MessageUtil.send(player, "messages.noPermissions");
                return true;
            }

            final String worldName = args[1];

            if (!worldUtils.isWorldLoaded(worldName)) {
                MessageUtil.send(player, "world.command.create.isExist");
                return true;
            }

            assert worldUtils.getMinecraftMainWorld().isPresent();
            World mainWorld = worldUtils.getMinecraftMainWorld().get();

            if (mainWorld.getName().equalsIgnoreCase(worldName)) {
                MessageUtil.send(player, "world.command.delete.mainWorld");
                return true;
            }

            if (worldUtils.worldDeleteConfirmation.getIfPresent(player.getUniqueId()) == null) {
                MessageUtil.send(player, "world.command.delete.confirm", worldName);
                MessageUtil.send(player, "world.command.delete.confirm.suffix");
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

                MessageUtil.send(player, "world.command.unload.successful", worldName);
                MessageUtil.send(player, "world.command.unload.successful.suffix", worldName);
                worldUtils.worldDeleteConfirmation.invalidate(player.getUniqueId());
            });

        } else if ((args.length == 2 || args.length == 3) && args[0].equalsIgnoreCase("import")) {

            if (PermissionChecker.notHasPermission(player, "swm.command.import")) {
                MessageUtil.send(player, "messages.noPermissions");
                return true;
            }

            String worldName = args[1];

            if (Bukkit.getWorld(worldName) != null) {
                MessageUtil.send(player, "world.command.import.isAlreadyLoaded");
                return true;
            }

            if (!worldUtils.isWorldFolderExisting(worldName)) {
                MessageUtil.send(player, "world.command.import.isNoWorldFolder");
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

            MessageUtil.send(player, "world.command.import.successful", worldName);

        } else if ((args.length == 2 || args.length == 3) && args[0].equalsIgnoreCase("lock")) {

            if (PermissionChecker.notHasPermission(player, "swm.command.lock")) {
                MessageUtil.send(player, "messages.noPermissions");
                return true;
            }

            String worldName = args[1];

            if (!worldUtils.isWorldLoaded(worldName)) {
                MessageUtil.send(player, "world.command.create.isExist");
                return true;
            }

            if (worldUtils.isWorldLocked(worldUtils.getWorldFolder(worldName))) {
                MessageUtil.send(player, "world.command.lock.isLocked");
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

            if (PermissionChecker.notHasPermission(player, "swm.command.unlock")) {
                MessageUtil.send(player, "messages.noPermissions");
                return true;
            }

            String worldName = args[1];

            if (!worldUtils.isWorldLoaded(worldName)) {
                MessageUtil.send(player, "world.command.create.isExist");
                return true;
            }

            if (!worldUtils.isWorldLocked(worldUtils.getWorldFolder(worldName))) {
                MessageUtil.send(player, "world.command.unlock.isNotLocked");
                return true;
            }

            checkIfPasswordMatches(args, player, worldName, unused -> {
                try {
                    worldUtils.deletePasswordFromWorld(worldName);
                } catch (IOException e) {
                    e.printStackTrace();
                }

                MessageUtil.send(player, "world.command.unlock.successful", worldName);
            });

        } else if ((args.length == 3 || args.length == 4) && args[0].equalsIgnoreCase("create")) {

            if (PermissionChecker.notHasPermission(player, "swm.command.create")) {
                MessageUtil.send(player, "messages.noPermissions");
                return true;
            }

            String worldName = args[1];

            Optional<World> newWorld;

            if (args.length == 3)
                newWorld = worldUtils.createWorld(player, worldName, args[2].toUpperCase(), false);
            else
                newWorld = worldUtils.createWorld(player, worldName, args[2].toUpperCase(), true, args[3]);

            if (args.length == 3)
                newWorld.ifPresent(world -> player.spigot().sendMessage(makeComponent(newWorld.get(), player)));

        } else if (args.length == 3) {
            Player target = Bukkit.getPlayer(args[1]);

            if (target == null) {
                MessageUtil.send(player, "messages.playerOffline");
                return true;
            }

            String worldName = args[2];

            if (!worldUtils.isWorldLoaded(worldName)) {
                MessageUtil.send(player, "world.command.create.isExist");
                return true;
            }

            String trustMode = args[0];

            if (trustMode.equalsIgnoreCase("trust")) {

                if (PermissionChecker.notHasPermission(player, "swm.command.trust")) {
                    MessageUtil.send(player, "messages.noPermissions");
                    return true;
                }

                WorldSettings worldSettings = worldSettingsFileManager.getWorldSettings(worldName);

                if (worldSettings.getTrustedBuilders().contains(target.getUniqueId().toString())) {
                    MessageUtil.send(player, "world.command.already.trust");
                    return true;
                }

                worldSettings.getTrustedBuilders().add(target.getUniqueId().toString());
                worldSettingsFileManager.updateSettingsForWorld(worldName, worldSettings);
                MessageUtil.send(player, "world.command.trust.player", player.getName(), worldName);

            } else if (trustMode.equalsIgnoreCase("untrust")) {

                if (PermissionChecker.notHasPermission(player, "swm.command.untrust")) {
                    MessageUtil.send(player, "messages.noPermissions");
                    return true;
                }

                WorldSettings worldSettings = worldSettingsFileManager.getWorldSettings(worldName);

                if (!worldSettings.getTrustedBuilders().contains(target.getUniqueId().toString())) {
                    MessageUtil.send(player, "world.command.player.isnotrusted");
                    return true;
                }

                worldSettings.getTrustedBuilders().remove(target.getUniqueId().toString());
                worldSettingsFileManager.updateSettingsForWorld(worldName, worldSettings);
                MessageUtil.send(player, "world.command.untrust.player", target.getName(), worldName);
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
            MessageUtil.send(player, "messages.denied.join");
            return;
        }

        if (args.length == 3 && worldLocked) {
            PasswordContainer passwordFromWorld = worldUtils.getPasswordFromWorld(worldFolder);
            String salt = passwordFromWorld.getSalt();
            String possiblePassword = SpaceWorldManager.getInstance().getHashingManager().createHashedPassword(args[2], salt);

            if (!possiblePassword.equalsIgnoreCase(passwordFromWorld.getHashedPassword())) {
                MessageUtil.send(player, "messages.denied.password");
            } else {
                MessageUtil.send(player, "messages.successful.password");
                response.accept(null);
            }
        }

    }

    private void sendUsage(Player player) {
        MessageUtil.send(player, "world.command.usage.title");
        MessageUtil.sendAndAppend(player, "world.command.usage.prefix", "swm create <Worldname> <Template> [Password]");
        MessageUtil.sendAndAppend(player, "world.command.usage.prefix", "swm delete <Worldname> [Password]");
        MessageUtil.sendAndAppend(player, "world.command.usage.prefix", "swm import <Worldname> [Password]");
        MessageUtil.sendAndAppend(player, "world.command.usage.prefix", "swm join <Worldname> [Password]");
        MessageUtil.sendAndAppend(player, "world.command.usage.prefix", "swm lock <WorldName> <Password>");
        MessageUtil.sendAndAppend(player, "world.command.usage.prefix", "swm unlock <Worldname> [Password]");
        MessageUtil.sendAndAppend(player, "world.command.usage.prefix", "swm trust <Player> <Worldname>");
        MessageUtil.sendAndAppend(player, "world.command.usage.prefix", "swm untrust <Player> <Worldname>");
        MessageUtil.sendAndAppend(player, "world.command.usage.prefix", "swm list <Page>");
        MessageUtil.sendAndAppend(player, "world.command.usage.prefix", "swm list");
        MessageUtil.sendAndAppend(player, "world.command.usage.prefix", "swm templates");
        MessageUtil.sendAndAppend(player, "world.command.usage.prefix", "swm info");
        MessageUtil.sendAndAppend(player, "world.command.usage.prefix", "swm gui");
        MessageUtil.sendAndAppend(player, "world.command.usage.prefix", "swm item");
    }

    @Nullable
    @Override
    public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String alias, @NotNull String[] args) {
        if (args.length == 1)
            return Arrays.asList("create", "delete", "import", "join", "lock", "unlock", "templates", "list", "info", "gui", "item");

        if (args.length == 2 && (args[0].equalsIgnoreCase("join")) || args[0].equalsIgnoreCase("delete")
                || args[0].equalsIgnoreCase("lock") || args[0].equalsIgnoreCase("unlock"))
            return Bukkit.getWorlds().stream().map(WorldInfo::getName).toList();

        if (args.length == 2 && args[0].equalsIgnoreCase("import"))
            return worldUtils.getAllWorldFiles().stream().map(File::getName).filter(name -> Bukkit.getWorld(name) == null).toList();

        if (args.length == 3 && args[0].equalsIgnoreCase("create"))
            return Arrays.stream(WorldTemplate.values()).map(worldTemplate -> worldTemplate.name().split("_")[0]).toList();

        return Collections.emptyList();
    }

    private TextComponent makeComponent(World newWorld, Player player) {
        final Optional<Message> optionalMessage = MessageUtil.get("world.join.click.message.prefix");
        final Optional<Message> optionalMessageBody = MessageUtil.get("world.join.click.message.body");
        final Optional<Message> optionalMessageSuffix = MessageUtil.get("world.join.click.message.suffix");

        TextComponent mainComponent = new TextComponent();
        mainComponent.setText(optionalMessage.isEmpty() ? "§7Click " : optionalMessage.get().getText());

        TextComponent firstSubComponent = new TextComponent();
        firstSubComponent.setText(optionalMessageBody.isEmpty() ? "§f§lHERE " : optionalMessageBody.get().getText());
        firstSubComponent.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/swm join " + newWorld.getName()));
        mainComponent.addExtra(firstSubComponent);

        TextComponent secondSubComponent = new TextComponent();
        secondSubComponent.setText(optionalMessageSuffix.isEmpty() ? "§7to teleport yourself to the new world." : optionalMessageSuffix.get().getText());
        mainComponent.addExtra(secondSubComponent);

        return mainComponent;
    }

}