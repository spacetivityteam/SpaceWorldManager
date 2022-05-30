package net.spacetivity.world;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import lombok.Getter;
import lombok.SneakyThrows;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;
import net.spacetivity.world.commands.StopLagCommand;
import net.spacetivity.world.commands.WorldCommand;
import net.spacetivity.world.configuration.ConfigurationFileManager;
import net.spacetivity.world.inventory.WorldInventory;
import net.spacetivity.world.inventoryapi.InventoryManager;
import net.spacetivity.world.listener.WorldHandleListener;
import net.spacetivity.world.listener.WorldProtectionListener;
import net.spacetivity.world.message.Message;
import net.spacetivity.world.message.MessageFileManager;
import net.spacetivity.world.message.MessageUtil;
import net.spacetivity.world.password.HashingManager;
import net.spacetivity.world.permission.PermissionChecker;
import net.spacetivity.world.scoreboardapi.PlayerTagManager;
import net.spacetivity.world.scoreboardapi.SidebarManager;
import net.spacetivity.world.settings.WorldSettingsFileManager;
import net.spacetivity.world.utils.*;
import net.spacetivity.world.utils.item.ItemActionListener;
import net.spacetivity.world.utils.item.ItemBuilder;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitTask;

import java.io.File;
import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;

@Getter
public final class SpaceWorldManager extends JavaPlugin {

    public static final String PREFIX = "§3NWM §7| ";
    public static final String NO_PERMISSION = PREFIX + "§cYou are not permitted to execute this action.";
    public static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    public static final List<ItemBuilder> INTERACTIVE_ITEMS = new CopyOnWriteArrayList<>();

    @Getter
    private static SpaceWorldManager instance;

    private FileUtils fileUtils;
    private WorldUtils worldUtils;
    private HashingManager hashingManager;
    private ConfigurationFileManager configurationFileManager;
    private MessageFileManager messageFileManager;
    private WorldSettingsFileManager worldSettingsFileManager;
    private PageConverter pageConverter;
    private BukkitTask worldInformationTask;
    private SidebarManager sidebarManager;
    private PlayerTagManager playerTagManager;
    private InventoryManager inventoryManager;

    @Override
    public void onEnable() {
        instance = this;
        this.fileUtils = new FileUtils();
        this.hashingManager = new HashingManager();
        this.configurationFileManager = new ConfigurationFileManager();
        this.configurationFileManager.createFile();
        this.messageFileManager = new MessageFileManager();
        this.messageFileManager.createMessagesFile();
        this.worldSettingsFileManager = new WorldSettingsFileManager();
        this.worldUtils = new WorldUtils();
        this.pageConverter = new PageConverter();
        this.sidebarManager = new SidebarManager();
        this.playerTagManager = new PlayerTagManager();
        this.inventoryManager = new InventoryManager(this);
        this.inventoryManager.init();

        startWorldLoadingProcesses();

        new WorldCommand(this);
        new StopLagCommand(this);

        Bukkit.getPluginManager().registerEvents(new WorldHandleListener(), this);
        Bukkit.getPluginManager().registerEvents(new WorldProtectionListener(worldSettingsFileManager), this);
        Bukkit.getPluginManager().registerEvents(new ItemActionListener(), this);

        if (configurationFileManager.getConfig().isShowCurrentWorldActionBar()) showWorldInformationActionBar();
    }

    @SneakyThrows
    @Override
    public void onDisable() {
        if (configurationFileManager.getConfig().isSaveOnShutdown())
            Bukkit.getWorlds().forEach(World::save);

        if (configurationFileManager.getConfig().isShowCurrentWorldActionBar())
            worldInformationTask.cancel();

        Material material = Material.valueOf(configurationFileManager.getConfig().getWorldItemMaterial());
        Bukkit.getOnlinePlayers().stream().filter(player -> player.getInventory().contains(material))
                .toList().forEach(player -> player.getInventory().remove(material));
    }

    public ItemStack giveWorldItem(Player player) {
        String worldItemMaterial = configurationFileManager.getConfig().getWorldItemMaterial();
        Material material = (worldItemMaterial == null ? Material.NETHER_STAR : Material.valueOf(worldItemMaterial));
        Optional<Message> message = MessageUtil.get("world.item.displayName");

        return ItemBuilder.builder(material, message.isEmpty() ? "§b§lWorld Manager" : message.get().getText())
                .addItemFlag(ItemFlag.HIDE_ATTRIBUTES)
                .addItemFlag(ItemFlag.HIDE_ENCHANTS)
                .addItemFlag(ItemFlag.HIDE_POTION_EFFECTS)
                .onInteract(event -> {
                    if (PermissionChecker.hasPermission(player, "swm.command.gui"))
                        WorldInventory.getInventory(player).open(player);
                })
                .build();
    }

    private void startWorldLoadingProcesses() {
        Set<String> worldNames = new HashSet<>();

        for (File worldFile : Objects.requireNonNull(Bukkit.getServer().getWorldContainer().listFiles()))
            if (worldFile.isDirectory()) worldNames.add(worldFile.getName());

        worldNames.forEach(worldName -> {
            WorldLoadingProcess worldLoadingProcess = new WorldLoadingProcess(worldName);
            Thread worldLoadingThread = new Thread(worldLoadingProcess);
            worldLoadingThread.start();
        });
    }

    private void showWorldInformationActionBar() {
        worldInformationTask = Bukkit.getScheduler().runTaskTimerAsynchronously(this, () -> {
            Bukkit.getOnlinePlayers().forEach(player -> {
                World world = player.getWorld();
                boolean worldLocked = worldUtils.isWorldLocked(world.getName());
                player.spigot().sendMessage(ChatMessageType.ACTION_BAR, new TextComponent("§b§lCurrent world: " + world.getName() + (worldLocked ? " §f§l(Locked)" : "")));
            });
        }, 0L, 10L);
    }
}
