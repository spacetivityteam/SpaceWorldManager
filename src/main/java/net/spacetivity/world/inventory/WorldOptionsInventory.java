package net.spacetivity.world.inventory;

import lombok.AllArgsConstructor;
import net.spacetivity.world.inventoryapi.ClickableItem;
import net.spacetivity.world.inventoryapi.InventoryUtils;
import net.spacetivity.world.inventoryapi.SmartInventory;
import net.spacetivity.world.inventoryapi.content.InventoryContents;
import net.spacetivity.world.inventoryapi.content.InventoryProvider;
import net.spacetivity.world.inventoryapi.content.SlotPos;
import net.spacetivity.world.utils.item.ItemBuilder;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemFlag;

@AllArgsConstructor
public class WorldOptionsInventory implements InventoryProvider {

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

        contents.set(1, 0, ClickableItem.empty(ItemBuilder.builder(Material.COMPASS, "§b§lSpawnpoint").build()));
        contents.set(1, 1, ClickableItem.empty(ItemBuilder.builder(Material.CLOCK, "§b§lTime").build()));

        contents.set(1, 2, ClickableItem.of(ItemBuilder.builder(Material.NAME_TAG, "§b§lGamerules").build(), event ->
                WorldGameRuleInventory.getInventory(player, world).open(player)));

        contents.set(1, 3, ClickableItem.empty(ItemBuilder.builder(Material.IRON_SWORD, "§b§lPVP").addItemFlag(ItemFlag.HIDE_ATTRIBUTES).build()));

        contents.set(1, 7, ClickableItem.empty(ItemBuilder.builder(Material.PLAYER_HEAD, "§b§lTrusted players").build()));
        contents.set(1, 8, ClickableItem.empty(ItemBuilder.builder(Material.COMPARATOR, "§b§lStatus").build()));
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
