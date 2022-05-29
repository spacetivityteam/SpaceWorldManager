package net.spacetivity.world.inventory;

import net.spacetivity.world.inventoryapi.InventoryUtils;
import net.spacetivity.world.inventoryapi.SmartInventory;
import net.spacetivity.world.inventoryapi.content.InventoryContents;
import net.spacetivity.world.inventoryapi.content.InventoryProvider;
import org.bukkit.entity.Player;

public class WorldGameRuleInventory implements InventoryProvider {

    private Player player;

    public WorldGameRuleInventory(Player player) {
        this.player = player;
    }

    public static SmartInventory getInventory(Player player) {
        return SmartInventory.builder()
                .provider(new WorldGameRuleInventory(player))
                .size(6, 9)
                .title(InventoryUtils.title("Gamerules"))
                .build();
    }

    @Override
    public void init(Player player, InventoryContents contents) {

    }


}
