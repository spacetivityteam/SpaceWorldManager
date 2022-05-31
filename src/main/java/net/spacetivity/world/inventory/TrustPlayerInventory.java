package net.spacetivity.world.inventory;

import lombok.AllArgsConstructor;
import net.spacetivity.world.SpaceWorldManager;
import net.spacetivity.world.inventoryapi.ClickableItem;
import net.spacetivity.world.inventoryapi.InventoryUtils;
import net.spacetivity.world.inventoryapi.SmartInventory;
import net.spacetivity.world.inventoryapi.content.*;
import net.spacetivity.world.settings.WorldSettings;
import net.spacetivity.world.utils.WorldUtils;
import net.spacetivity.world.utils.item.ItemBuilder;
import org.bukkit.*;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemFlag;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.function.Consumer;

@AllArgsConstructor
public class TrustPlayerInventory implements InventoryProvider {

    private Player player;
    private World world;

    public static SmartInventory getInventory(Player player, World world) {
        return SmartInventory.builder()
                .provider(new TrustPlayerInventory(player, world))
                .size(6, 9)
                .title(InventoryUtils.subTitle("Worlds", world.getName() + " (Trust player)"))
                .build();
    }

    @Override
    public void init(Player player, InventoryContents contents) {
        setPlaceholders(contents);
        InventoryUtils.backToMainPageItem(contents, SlotPos.of(0, 4), player, WorldOptionsInventory.getInventory(player, world));

        Pagination pagination = contents.pagination();

        loadAllPlayers(clickableItems -> {
            if (clickableItems.isEmpty()) {
                contents.set(SlotPos.of(2, 4), ClickableItem.empty(new ItemBuilder(Material.BARRIER).setDisplayName("§cNo players found!").build()));
            } else {
                pagination.setItems(clickableItems.toArray(new ClickableItem[0]));
                pagination.setItemsPerPage(36);
                pagination.addToIterator(contents.newIterator(SlotIterator.Type.HORIZONTAL, 1, 0));
            }

            InventoryUtils.loadNavigators(SlotPos.of(0, 1), SlotPos.of(0, 7), player, getInventory(player, world), pagination);
        });
    }

    private void loadAllPlayers(Consumer<List<ClickableItem>> result) {
        List<ItemBuilder> itemBuilders = new ArrayList<>();
        List<ClickableItem> items = new ArrayList<>();

        WorldUtils worldUtils = SpaceWorldManager.getInstance().getWorldUtils();
        WorldSettings settings = worldUtils.getSettings(world.getName());

        List<OfflinePlayer> offlinePlayers = Arrays.stream(Bukkit.getOfflinePlayers())
                .filter(offlinePlayer -> !settings.getTrustedBuilders().contains(offlinePlayer.getUniqueId().toString()))
                .toList();

        for (OfflinePlayer offlinePlayer : offlinePlayers) getDisplayItem(offlinePlayer, itemBuilders::add);

        itemBuilders.forEach(itemBuilder -> items.add(ClickableItem.of(itemBuilder.build(), event -> {
            OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(itemBuilder.getData("player", UUID.class));
            settings.getTrustedBuilders().add(offlinePlayer.getUniqueId().toString());
            SpaceWorldManager.getInstance().getWorldSettingsFileManager().updateSettingsForWorld(world.getName(), settings);
            player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1, 1);
            TrustedPlayerInventory.getInventory(player, world).open(player);
        })));

        result.accept(items);
    }

    private void getDisplayItem(OfflinePlayer offlinePlayer, Consumer<ItemBuilder> result) {
        ItemBuilder itemBuilder = ItemBuilder.builder(Material.PLAYER_HEAD, "§b§l" + offlinePlayer.getName())
                .setLores(List.of("§8Click to trust"))
                .addItemFlag(ItemFlag.HIDE_ATTRIBUTES);

        itemBuilder.setData("player", offlinePlayer.getUniqueId());

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
