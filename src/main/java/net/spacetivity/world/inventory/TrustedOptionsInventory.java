package net.spacetivity.world.inventory;

import lombok.AllArgsConstructor;
import net.spacetivity.world.SpaceWorldManager;
import net.spacetivity.world.inventoryapi.ClickableItem;
import net.spacetivity.world.inventoryapi.InventoryUtils;
import net.spacetivity.world.inventoryapi.SmartInventory;
import net.spacetivity.world.inventoryapi.content.InventoryContents;
import net.spacetivity.world.inventoryapi.content.InventoryProvider;
import net.spacetivity.world.inventoryapi.content.SlotPos;
import net.spacetivity.world.settings.WorldSettings;
import net.spacetivity.world.utils.item.ItemBuilder;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.entity.Player;

@AllArgsConstructor
public class TrustedOptionsInventory implements InventoryProvider {

    private Player player;
    private World world;
    private OfflinePlayer trustedPlayer;

    public static SmartInventory getInventory(Player player, World world, OfflinePlayer trustedPlayer) {
        return SmartInventory.builder()
                .provider(new TrustedOptionsInventory(player, world, trustedPlayer))
                .size(1, 9)
                .title(InventoryUtils.subTitle("Worlds", "Trusted/" + trustedPlayer.getName()))
                .build();
    }

    @Override
    public void init(Player player, InventoryContents contents) {
        setPlaceholders(contents);
        InventoryUtils.backToMainPageItem(contents, SlotPos.of(0, 7), player, TrustedPlayerInventory.getInventory(player, world));

        contents.set(0, 1, ClickableItem.empty(ItemBuilder.builder(Material.PLAYER_HEAD, "§b§l" + trustedPlayer.getName()).build()));

        contents.set(0, 4, ClickableItem.of(ItemBuilder.builder(Material.REDSTONE, "§b§lUntrust player").build(), event -> {
            WorldSettings settings = SpaceWorldManager.getInstance().getWorldUtils().getSettings(world.getName());
            settings.getTrustedBuilders().remove(trustedPlayer.getUniqueId().toString());
            SpaceWorldManager.getInstance().getWorldSettingsFileManager().updateSettingsForWorld(world.getName(), settings);
            TrustedPlayerInventory.getInventory(player, world).open(player);
            player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1, 1);
        }));
    }

    private void setPlaceholders(InventoryContents contents) {
        contents.fillRow(0, ClickableItem.empty(ItemBuilder.placeHolder(Material.CYAN_STAINED_GLASS_PANE)));
        contents.set(0, 0, ClickableItem.empty(ItemBuilder.placeHolder(Material.BLUE_STAINED_GLASS_PANE)));
        contents.set(0, 8, ClickableItem.empty(ItemBuilder.placeHolder(Material.BLUE_STAINED_GLASS_PANE)));
    }
}
