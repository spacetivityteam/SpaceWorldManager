package net.spacetivity.world.inventory;

import lombok.AllArgsConstructor;
import net.spacetivity.world.inventoryapi.ClickableItem;
import net.spacetivity.world.inventoryapi.InventoryUtils;
import net.spacetivity.world.inventoryapi.SmartInventory;
import net.spacetivity.world.inventoryapi.content.*;
import net.spacetivity.world.utils.item.ItemBuilder;
import org.bukkit.GameRule;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemFlag;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;

@AllArgsConstructor
public class WorldGameRuleInventory implements InventoryProvider {

    private Player player;
    private World world;

    public static SmartInventory getInventory(Player player, World world) {
        return SmartInventory.builder()
                .provider(new WorldGameRuleInventory(player, world))
                .size(6, 9)
                .title(InventoryUtils.subTitle("Worlds", "Gamerules"))
                .build();
    }

    @Override
    public void init(Player player, InventoryContents contents) {
        setPlaceholders(contents);

        contents.set(0, 3, ClickableItem.empty(ItemBuilder.builder(Material.MAP, "§b§l" + world.getName())
                .addItemFlag(ItemFlag.HIDE_ATTRIBUTES)
                .build()));

        InventoryUtils.backToMainPageItem(contents, SlotPos.of(0, 5), player, WorldOptionsInventory.getInventory(player, world));

        Pagination pagination = contents.pagination();

        loadAllGameRules(clickableItems -> {
            if (clickableItems.isEmpty()) {
                contents.set(SlotPos.of(2, 4), ClickableItem.empty(new ItemBuilder(Material.BARRIER)
                        .setDisplayName("§cNo gamerules found!")
                        .build()));
            } else {
                pagination.setItems(clickableItems.toArray(new ClickableItem[0]));
                pagination.setItemsPerPage(36);
                pagination.addToIterator(contents.newIterator(SlotIterator.Type.HORIZONTAL, 1, 0));
            }

            InventoryUtils.loadNavigators(SlotPos.of(0, 1), SlotPos.of(0, 7), player, getInventory(player, world), pagination);
        });
    }

    private void loadAllGameRules(Consumer<List<ClickableItem>> result) {
        List<GameRule<?>> gameRules = Arrays.stream(GameRule.values()).toList();
        List<ItemBuilder> itemBuilders = new ArrayList<>();
        List<ClickableItem> items = new ArrayList<>();
        for (GameRule<?> color : gameRules) getDisplayItem(color, itemBuilders::add);
        itemBuilders.forEach(itemBuilder -> {
            items.add(ClickableItem.of(itemBuilder.build(), event -> {

                GameRule<?> gameRule = GameRule.getByName(itemBuilder.getData("gameRule", String.class));
                assert gameRule != null;
                Object gameRuleValue = world.getGameRuleValue(gameRule);

                if (gameRuleValue instanceof Integer) {
                    ClickType clickType = event.getClick();
                    int amount = Integer.parseInt(String.valueOf(gameRuleValue));
                    int newAmount = 0;

                    if (clickType.isLeftClick()) {
                        newAmount = amount + 1;
                        world.setGameRule((GameRule<Integer>) gameRule, newAmount);
                    } else if (clickType.isRightClick()) {
                        if (amount <= 0) {
                            player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_BASS, 10, 10);
                            return;
                        }

                        newAmount = amount - 1;
                        world.setGameRule((GameRule<Integer>) gameRule, newAmount);
                    }

                    InventoryUtils.updateClickedGameRuleItem(Objects.requireNonNull(event.getCurrentItem()), newAmount, Integer.class);

                } else if (gameRuleValue instanceof Boolean value) {
                    world.setGameRule((GameRule<Boolean>) gameRule, !value);
                    InventoryUtils.updateClickedGameRuleItem(Objects.requireNonNull(event.getCurrentItem()), !value, Boolean.class);
                }

                player.playSound(player.getLocation(), Sound.BLOCK_DISPENSER_DISPENSE, 5, 1);
            }));
        });
        result.accept(items);
    }

    public void getDisplayItem(GameRule<?> gameRule, Consumer<ItemBuilder> result) {
        List<String> lore = new ArrayList<>();

        lore.add("§7Value: §f" + world.getGameRuleValue(gameRule));
        lore.add(" ");

        if (gameRule.getType().equals(Integer.class)) {
            lore.add("§8Left-click | +1");
            lore.add("§8Right-click | -1");
        } else {
            lore.add("§8Click to update");
        }

        ItemBuilder itemBuilder = ItemBuilder.builder(Material.NAME_TAG, "§b§l" + gameRule.getName())
                .setLores(lore)
                .addItemFlag(ItemFlag.HIDE_ATTRIBUTES);

        itemBuilder.setData("gameRule", gameRule.getName());

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
