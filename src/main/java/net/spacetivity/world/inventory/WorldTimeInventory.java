package net.spacetivity.world.inventory;

import lombok.AllArgsConstructor;
import net.spacetivity.world.SpaceWorldManager;
import net.spacetivity.world.inventoryapi.ClickableItem;
import net.spacetivity.world.inventoryapi.InventoryUtils;
import net.spacetivity.world.inventoryapi.SmartInventory;
import net.spacetivity.world.inventoryapi.content.InventoryContents;
import net.spacetivity.world.inventoryapi.content.InventoryProvider;
import net.spacetivity.world.inventoryapi.content.SlotPos;
import net.spacetivity.world.message.Message;
import net.spacetivity.world.message.MessageUtil;
import net.spacetivity.world.utils.item.ItemBuilder;
import org.bukkit.GameRule;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.List;
import java.util.Objects;

@AllArgsConstructor
public class WorldTimeInventory implements InventoryProvider {

    private Player player;
    private World world;

    public static SmartInventory getInventory(Player player, World world) {
        return SmartInventory.builder()
                .provider(new WorldTimeInventory(player, world))
                .size(3, 9)
                .title(InventoryUtils.subTitle("Worlds", world.getName() + " (Time)"))
                .build();
    }

    @Override
    public void init(Player player, InventoryContents contents) {
        setPlaceholders(contents);
        InventoryUtils.backToMainPageItem(contents, SlotPos.of(0, 4), player, WorldOptionsInventory.getInventory(player, world));

        contents.set(1, 2, ClickableItem.of(ItemBuilder.builder(Material.CLOCK, "§b§lDay").build(), event -> {
            world.setTime(1000);
            MessageUtil.send(player, "messages.world.updateTimeToDay");
            player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1, 1);
        }));

        @SuppressWarnings("ConstantConditions") boolean value = world.getGameRuleValue(GameRule.DO_DAYLIGHT_CYCLE);
        ItemStack stationaryTimeItem = ItemBuilder.builder(Material.NAME_TAG, "§b§lAllow day/night cycle")
                .setLores(List.of("§7Value: §f" + value))
                .build();

        contents.set(1, 4, ClickableItem.of(stationaryTimeItem, event -> {
            //noinspection ConstantConditions
            world.setGameRule(GameRule.DO_DAYLIGHT_CYCLE, !(boolean) world.getGameRuleValue(GameRule.DO_DAYLIGHT_CYCLE));
            //noinspection ConstantConditions
            InventoryUtils.updateStationaryTimeItem(Objects.requireNonNull(event.getCurrentItem()), world.getGameRuleValue(GameRule.DO_DAYLIGHT_CYCLE));
            player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1, 1);
        }));

        contents.set(1, 6, ClickableItem.of(ItemBuilder.builder(Material.CLOCK, "§b§lNight").build(), event -> {
            world.setTime(16000);
            MessageUtil.send(player, "messages.world.updateTimeToNight");
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