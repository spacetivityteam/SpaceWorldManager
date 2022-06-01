package net.spacetivity.world.inventory;

import lombok.AllArgsConstructor;
import net.spacetivity.world.SpaceWorldManager;
import net.spacetivity.world.inventoryapi.ClickableItem;
import net.spacetivity.world.inventoryapi.InventoryUtils;
import net.spacetivity.world.inventoryapi.SmartInventory;
import net.spacetivity.world.inventoryapi.content.InventoryContents;
import net.spacetivity.world.inventoryapi.content.InventoryProvider;
import net.spacetivity.world.inventoryapi.content.SlotPos;
import net.spacetivity.world.message.MessageUtil;
import net.spacetivity.world.utils.WorldUtils;
import net.spacetivity.world.utils.item.ItemBuilder;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.List;
import java.util.Objects;

@AllArgsConstructor
public class WorldOptionsInventory implements InventoryProvider {

    private final WorldUtils worldUtils = SpaceWorldManager.getInstance().getWorldUtils();

    private Player player;
    private World world;

    public static SmartInventory getInventory(Player player, World world) {
        return SmartInventory.builder()
                .provider(new WorldOptionsInventory(player, world))
                .size(3, 9)
                .title(InventoryUtils.subTitle("Worlds", world.getName()))
                .build();
    }

    @Override
    public void init(Player player, InventoryContents contents) {
        setPlaceholders(contents);
        InventoryUtils.backToMainPageItem(contents, SlotPos.of(0, 4), player, WorldInventory.getInventory(player));

        Location spawnLocation = world.getSpawnLocation();

        ItemStack spawnItem = ItemBuilder.builder(Material.COMPASS, "§b§lSpawnpoint")
                .setLores(List.of(
                        "§7X: §f" + spawnLocation.getX(),
                        "§7Y: §f" + spawnLocation.getYaw(),
                        "§7Z: §f" + spawnLocation.getZ(),
                        "§8Click to update"
                ))
                .build();

        contents.set(1, 0, ClickableItem.of(spawnItem, event -> {
            if (!player.getWorld().getName().equalsIgnoreCase(world.getName())) {
                MessageUtil.send(player, "messages.world.updateSpawn.isIn");
                return;
            }

            world.setSpawnLocation(player.getLocation());
            MessageUtil.send(player,"messages.world.updateSpawn", world.getName());
            InventoryUtils.updateClickedSpawnItem(Objects.requireNonNull(event.getCurrentItem()), world);
            player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1, 1);
        }));

        contents.set(1, 1, ClickableItem.of(ItemBuilder.builder(Material.CLOCK, "§b§lTime").build(), event ->
                WorldTimeInventory.getInventory(player, world).open(player)));

        contents.set(1, 2, ClickableItem.of(ItemBuilder.builder(Material.NAME_TAG, "§b§lGamerules").build(), event ->
                WorldGameRuleInventory.getInventory(player, world).open(player)));


        contents.set(1, 3, ClickableItem.of(ItemBuilder.builder(Material.ENDER_PEARL, "§b§lJoin World").build(), event -> {
            player.closeInventory();
            player.performCommand("swm join " + world.getName());
            player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1, 1);
        }));

        contents.set(1, 4, ClickableItem.of(ItemBuilder.builder(Material.PLAYER_HEAD, "§b§lTrusted players").build(), event ->
                TrustedPlayerInventory.getInventory(player, world).open(player)));

        ItemStack stateItem = ItemBuilder.builder(Material.COMPARATOR, "§b§lStatus")
                .setLores(List.of("§7> " + worldUtils.getState(world.getName()).getName(), "§8" + worldUtils.getOtherState(world.getName()).getName()))
                .build();

        contents.set(1, 8, ClickableItem.of(stateItem, event -> {
            worldUtils.updateStatus(world.getName());
            InventoryUtils.updateClickedStateItem(Objects.requireNonNull(event.getCurrentItem()), world);
            player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1, 1);
        }));
    }

    private void setPlaceholders(InventoryContents contents) {
        contents.fillRow(0, ClickableItem.empty(ItemBuilder.placeHolder(Material.CYAN_STAINED_GLASS_PANE)));
        contents.fillRow(2, ClickableItem.empty(ItemBuilder.placeHolder(Material.CYAN_STAINED_GLASS_PANE)));
        contents.set(0, 0, ClickableItem.empty(ItemBuilder.placeHolder(Material.BLUE_STAINED_GLASS_PANE)));
        contents.set(0, 8, ClickableItem.empty(ItemBuilder.placeHolder(Material.BLUE_STAINED_GLASS_PANE)));
        contents.set(2, 0, ClickableItem.empty(ItemBuilder.placeHolder(Material.BLUE_STAINED_GLASS_PANE)));
        contents.set(2, 8, ClickableItem.empty(ItemBuilder.placeHolder(Material.BLUE_STAINED_GLASS_PANE)));
    }
}
