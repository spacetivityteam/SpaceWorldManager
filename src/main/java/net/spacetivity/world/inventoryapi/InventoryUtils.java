package net.spacetivity.world.inventoryapi;

import net.spacetivity.world.SpaceWorldManager;
import net.spacetivity.world.inventoryapi.content.InventoryContents;
import net.spacetivity.world.inventoryapi.content.Pagination;
import net.spacetivity.world.inventoryapi.content.SlotPos;
import net.spacetivity.world.utils.WorldUtils;
import net.spacetivity.world.utils.item.ItemBuilder;
import org.bukkit.*;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;

public class InventoryUtils {

    public static String title(String mainPageName) {
        return ChatColor.DARK_GRAY + mainPageName;
    }

    public static String subTitle(String mainPageTitle, String page) {
        return title(mainPageTitle) + ChatColor.GRAY + "/" + ChatColor.GRAY + page;
    }

    public static void fillRectangle(InventoryContents contents, SlotPos start, SlotPos end, ClickableItem item) {
        fillRectangle(contents, start.getRow(), start.getColumn(), end.getRow(), end.getColumn(), item);
    }

    public static void fillRectangle(InventoryContents contents, int fromRow, int fromColumn, int toRow, int toColumn, ClickableItem item) {
        for (int row = fromRow; row <= toRow; ++row) {
            for (int column = fromColumn; column <= toColumn; ++column) {
                contents.set(row, column, item);
            }
        }
    }

    public static void backToMainPageItem(InventoryContents contents, SlotPos slotPos, Player player, SmartInventory inventoryToOpen) {
        contents.set(slotPos, ClickableItem.of(new ItemBuilder(Material.SLIME_BALL)
                .setDisplayName("§8Back §7(Click)")
                .build(), event -> {
            inventoryToOpen.open(player);
            player.playSound(player.getLocation(), Sound.BLOCK_LEVER_CLICK, 1, 1);
        }));
    }

    public static void loadNavigators(SlotPos previousPosition, SlotPos nextPosition, Player player, SmartInventory inventory, Pagination pagination) {
        inventory.getManager().getContents(player).ifPresent(contents -> {
            if (!pagination.isFirst()) contents.set(previousPosition, previousPageItem(inventory, player, pagination));
            if (!pagination.isLast()) contents.set(nextPosition, nextPageItem(inventory, player, pagination));
        });
    }

    private static ClickableItem previousPageItem(SmartInventory inventory, Player player, Pagination pagination) {
        return ClickableItem.of(new ItemBuilder(Material.RED_BANNER).setDisplayName("§b§lPrevious").build(),
                event -> inventory.open(player, pagination.previous().getPage()));
    }

    private static ClickableItem nextPageItem(SmartInventory inventory, Player player, Pagination pagination) {
        return ClickableItem.of(new ItemBuilder(Material.GREEN_BANNER).setDisplayName("§b§lNext").build(),
                event -> inventory.open(player, pagination.next().getPage()));
    }

    public static void updateClickedGameRuleItem(ItemStack itemToUpdate, Object newValue, Class<?> clazz) {
        if (itemToUpdate.getItemMeta() == null) return;
        if (itemToUpdate.getItemMeta().getLore() == null) return;
        if (itemToUpdate.getItemMeta().getLore().isEmpty()) return;

        ItemMeta itemMeta = itemToUpdate.getItemMeta();
        List<String> lore = new ArrayList<>();

        lore.add("§7Value: §f" + newValue);
        lore.add(" ");

        if (clazz.equals(Integer.class)) {
            lore.add("§8Left-click | +1");
            lore.add("§8Right-click | -1");
        } else {
            lore.add("§8Click to update");
        }

        itemMeta.setLore(lore);

        itemToUpdate.setItemMeta(itemMeta);
    }

    public static void updateClickedStateItem(ItemStack itemToUpdate, World world) {
        if (itemToUpdate.getItemMeta() == null) return;
        if (itemToUpdate.getItemMeta().getLore() == null) return;
        if (itemToUpdate.getItemMeta().getLore().isEmpty()) return;

        ItemMeta itemMeta = itemToUpdate.getItemMeta();
        List<String> lore = new ArrayList<>();

        WorldUtils worldUtils = SpaceWorldManager.getInstance().getWorldUtils();
        lore.add("§7> " + worldUtils.getState(world.getName()).getName());
        lore.add("§8" + worldUtils.getOtherState(world.getName()).getName());

        itemMeta.setLore(lore);
        itemToUpdate.setItemMeta(itemMeta);
    }

    public static void updateClickedSpawnItem(ItemStack itemToUpdate, World world) {
        if (itemToUpdate.getItemMeta() == null) return;
        if (itemToUpdate.getItemMeta().getLore() == null) return;
        if (itemToUpdate.getItemMeta().getLore().isEmpty()) return;

        ItemMeta itemMeta = itemToUpdate.getItemMeta();
        List<String> lore = new ArrayList<>();

        Location spawnLocation = world.getSpawnLocation();
        lore.add("§7X: §f" + spawnLocation.getX());
        lore.add("§7Y: §f" + spawnLocation.getYaw());
        lore.add("§7Z: §f" + spawnLocation.getZ());
        lore.add("§8Click to update");

        itemMeta.setLore(lore);
        itemToUpdate.setItemMeta(itemMeta);
    }

    public static void updateStationaryTimeItem(ItemStack itemToUpdate, boolean value) {
        if (itemToUpdate.getItemMeta() == null) return;
        if (itemToUpdate.getItemMeta().getLore() == null) return;
        if (itemToUpdate.getItemMeta().getLore().isEmpty()) return;

        ItemMeta itemMeta = itemToUpdate.getItemMeta();
        List<String> lore = new ArrayList<>(generateLore(value));

        itemMeta.setLore(lore);
        itemToUpdate.setItemMeta(itemMeta);
    }

    public static List<String> generateLore(Object value) {
        return List.of("§7Value: §f" + value);
    }

    public static List<String> generateLoreAsStrings(boolean value) {
        return List.of("§8Status: " + (value ? "§aon" : "§coff"));
    }
}
