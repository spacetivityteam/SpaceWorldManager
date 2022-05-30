package net.spacetivity.world.inventory;

import lombok.AllArgsConstructor;
import net.spacetivity.world.SpaceWorldManager;
import net.spacetivity.world.inventoryapi.ClickableItem;
import net.spacetivity.world.inventoryapi.InventoryUtils;
import net.spacetivity.world.inventoryapi.SmartInventory;
import net.spacetivity.world.inventoryapi.content.*;
import net.spacetivity.world.permission.PermissionChecker;
import net.spacetivity.world.utils.item.ItemBuilder;
import net.spacetivity.world.utils.WorldUtils;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemFlag;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

@AllArgsConstructor
public class WorldInventory implements InventoryProvider {

    private final WorldUtils worldUtils = SpaceWorldManager.getInstance().getWorldUtils();

    private Player player;

    public static SmartInventory getInventory(Player player) {
        return SmartInventory.builder().provider(new WorldInventory(player)).size(6, 9).title(InventoryUtils.title("Worlds")).build();
    }

    @Override
    public void init(Player player, InventoryContents contents) {
        setPlaceholders(contents);
        WorldUtils worldUtils = SpaceWorldManager.getInstance().getWorldUtils();
        contents.set(0, 4, ClickableItem.empty(ItemBuilder.builder(Material.CHORUS_FLOWER, "§b§lWorlds").setLores(List.of("§7Loaded: §f" + worldUtils.getAllWorldFiles().stream().filter(worldUtils::isWorldLoaded).toList().size(), "§7Unloaded: §f" + worldUtils.getAllWorldFiles().stream().filter(file -> !worldUtils.isWorldLoaded(file)).toList().size(), "§7Locked: §f" + worldUtils.getAllWorldFiles().stream().filter(worldUtils::isWorldLocked).toList().size())).build()));

        Pagination pagination = contents.pagination();

        loadAllWorlds(clickableItems -> {
            if (clickableItems.isEmpty()) {
                contents.set(SlotPos.of(2, 4), ClickableItem.empty(new ItemBuilder(Material.BARRIER).setDisplayName("§cNo worlds found!").build()));
            } else {
                pagination.setItems(clickableItems.toArray(new ClickableItem[0]));
                pagination.setItemsPerPage(36);
                pagination.addToIterator(contents.newIterator(SlotIterator.Type.HORIZONTAL, 1, 0));
            }

            InventoryUtils.loadNavigators(SlotPos.of(0, 1), SlotPos.of(0, 7), player, getInventory(player), pagination);
        });
    }

    private void loadAllWorlds(Consumer<List<ClickableItem>> result) {
        List<World> worlds;
        List<World> allWorlds = Bukkit.getWorlds();

        /* If player has the bypass permission (or op / *) he can manage all worlds)
        If not, he can only manage the worlds he created. */
        if (PermissionChecker.hasByPassPermission(player))
            worlds = allWorlds;
        else
            worlds = allWorlds.stream().filter(world -> worldUtils.getCreator(world.getName()).equalsIgnoreCase(player.getName())).toList();

        List<ItemBuilder> itemBuilders = new ArrayList<>();
        List<ClickableItem> items = new ArrayList<>();
        for (World world : worlds) getDisplayItem(world, itemBuilders::add);

        itemBuilders.forEach(itemBuilder -> items.add(ClickableItem.of(itemBuilder.build(), event ->
                WorldOptionsInventory.getInventory(player, Bukkit.getWorld(itemBuilder.getData("world", String.class))).open(player))));
        result.accept(items);
    }

    public void getDisplayItem(World world, Consumer<ItemBuilder> result) {
        String creator = worldUtils.getCreator(world.getName());
        ItemBuilder itemBuilder = ItemBuilder.builder(Material.WRITABLE_BOOK, "§b§l" + world.getName())
                .setLores(List.of("§8Click for more options", "§8Created by §7" + (creator == null ? "Server" : creator)))
                .addItemFlag(ItemFlag.HIDE_ATTRIBUTES);

        itemBuilder.setData("world", world.getName());

        result.accept(itemBuilder);
    }

    private void setPlaceholders(InventoryContents contents) {
        contents.fillRow(0, ClickableItem.empty(ItemBuilder.placeHolder(Material.CYAN_STAINED_GLASS_PANE)));
        contents.fillRow(5, ClickableItem.empty(ItemBuilder.placeHolder(Material.CYAN_STAINED_GLASS_PANE)));
        contents.set(0, 0, ClickableItem.empty(ItemBuilder.placeHolder(Material.BLUE_STAINED_GLASS_PANE)));
        contents.set(0, 8, ClickableItem.empty(ItemBuilder.placeHolder(Material.BLUE_STAINED_GLASS_PANE)));
        contents.set(5, 0, ClickableItem.empty(ItemBuilder.placeHolder(Material.BLUE_STAINED_GLASS_PANE)));
        contents.set(5, 8, ClickableItem.empty(ItemBuilder.placeHolder(Material.BLUE_STAINED_GLASS_PANE)));
    }
}
