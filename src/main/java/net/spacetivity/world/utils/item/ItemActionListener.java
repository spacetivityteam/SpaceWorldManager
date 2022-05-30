package net.spacetivity.world.utils.item;

import net.spacetivity.world.SpaceWorldManager;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;

public class ItemActionListener implements Listener {

    @EventHandler
    public void onInteract(PlayerInteractEvent event) {
        if (event.getItem() == null) return;
        if (!event.getAction().equals(Action.RIGHT_CLICK_BLOCK) && !event.getAction().equals(Action.RIGHT_CLICK_AIR))
            return;
        SpaceWorldManager.INTERACTIVE_ITEMS.stream().filter(item -> item.getItemStack().equals(event.getItem()))
                .findAny().ifPresent(itemBuilder -> itemBuilder.getAction().accept(event));
    }
}
