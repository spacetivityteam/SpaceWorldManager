package net.spacetivity.world.utils.item;

import com.google.gson.Gson;
import lombok.Getter;
import net.spacetivity.world.SpaceWorldManager;
import net.spacetivity.world.utils.SkullBuilder;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.util.io.BukkitObjectInputStream;
import org.bukkit.util.io.BukkitObjectOutputStream;
import org.yaml.snakeyaml.external.biz.base64Coder.Base64Coder;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ThreadLocalRandom;
import java.util.function.Consumer;

@Getter
public class ItemBuilder {

    protected ItemStack itemStack;
    private ItemMeta itemMeta;
    private Consumer<PlayerInteractEvent> action;

    public static ItemStack placeHolder(Material material) {
        return new ItemBuilder(material).setDisplayName(" ").build();
    }

    public static ItemStack of(Material material, String displayName) {
        return new ItemBuilder(material).setDisplayName(displayName).build();
    }

    public static ItemBuilder builder(Material material, String displayName) {
        return new ItemBuilder(material).setDisplayName(displayName);
    }

    public static ItemBuilder skull(String displayName, String value) {
        return new SkullBuilder()
                .setOwnerWithGameProfile(value)
                .setDisplayName(displayName);
    }

    public ItemBuilder(ItemStack itemStack) {
        this.itemStack = new ItemStack(itemStack);
        this.itemMeta = itemStack.getItemMeta();
        if (itemMeta != null) {
            if (!itemMeta.getPersistentDataContainer().has(new NamespacedKey(SpaceWorldManager.getInstance(), "id"), PersistentDataType.INTEGER)) {
                itemMeta.getPersistentDataContainer().set(new NamespacedKey(SpaceWorldManager.getInstance(), "id"), PersistentDataType.INTEGER,
                        ThreadLocalRandom.current().nextInt(5000000) + 1000);
                this.itemStack.setItemMeta(itemMeta);
            }
        }
    }

    public ItemBuilder(Material material) {
        this.itemStack = new ItemStack(material);
        this.itemMeta = itemStack.getItemMeta();
        if (itemMeta != null) {
            if (!itemMeta.getPersistentDataContainer().has(new NamespacedKey(SpaceWorldManager.getInstance(), "id"), PersistentDataType.INTEGER)) {
                itemMeta.getPersistentDataContainer().set(new NamespacedKey(SpaceWorldManager.getInstance(), "id"), PersistentDataType.INTEGER,
                        ThreadLocalRandom.current().nextInt(5000000) + 1000);
                this.itemStack.setItemMeta(itemMeta);
            }
        }
    }

    public void setData(String key, Object data) {
        ItemMeta itemMeta = this.itemStack.getItemMeta();
        if (itemMeta != null) {
            NamespacedKey namespacedKey = new NamespacedKey(SpaceWorldManager.getInstance(), key);
            if (!itemMeta.getPersistentDataContainer().has(namespacedKey, PersistentDataType.STRING)) {
                Gson gson = SpaceWorldManager.GSON;
                itemMeta.getPersistentDataContainer().set(namespacedKey, PersistentDataType.STRING, gson.toJson(data));
                this.itemStack.setItemMeta(itemMeta);
            }
        }
    }

    public boolean hasData(String key) {
        ItemMeta itemMeta = this.itemStack.getItemMeta();
        if (itemMeta == null) {
            return false;
        } else {
            NamespacedKey namespacedKey = new NamespacedKey(SpaceWorldManager.getInstance(), key);
            return itemMeta.getPersistentDataContainer().has(namespacedKey, PersistentDataType.STRING);
        }
    }

    public <T> T getData(String key, Class<T> tClass) {
        ItemMeta itemMeta = this.itemStack.getItemMeta();
        if (itemMeta == null) {
            return null;
        } else {
            PersistentDataContainer dataContainer = itemMeta.getPersistentDataContainer();
            NamespacedKey namespacedKey = new NamespacedKey(SpaceWorldManager.getInstance(), key);
            Gson gson = SpaceWorldManager.GSON;
            String jsonString = dataContainer.getOrDefault(namespacedKey, PersistentDataType.STRING, "");
            return gson.fromJson(jsonString, tClass);
        }
    }

    public int getId() {
        ItemMeta itemMeta = this.itemStack.getItemMeta();
        if (itemMeta == null) {
            return 0;
        } else {
            PersistentDataContainer dataContainer = itemMeta.getPersistentDataContainer();
            NamespacedKey namespacedKey = new NamespacedKey(SpaceWorldManager.getInstance(), "id");
            return dataContainer.getOrDefault(namespacedKey, PersistentDataType.INTEGER, 0);
        }
    }

    public ItemBuilder onInteract(Consumer<PlayerInteractEvent> action) {
        this.action = action;
        SpaceWorldManager.INTERACTIVE_ITEMS.add(this);
        return this;
    }

    public ItemBuilder setDisplayName(String name) {
        this.itemMeta = this.itemStack.getItemMeta();
        assert this.itemMeta != null;
        this.itemMeta.setDisplayName(name);
        this.itemStack.setItemMeta(this.itemMeta);
        return this;
    }

    public ItemBuilder setType(Material material) {
        this.itemMeta = this.itemStack.getItemMeta();
        this.itemStack.setType(material);
        this.itemStack.setItemMeta(this.itemMeta);
        return this;
    }


    public ItemBuilder addLore(String lore) {
        this.itemMeta = this.itemStack.getItemMeta();
        assert this.itemMeta != null;
        List<String> lores = this.itemMeta.getLore();
        assert lores != null;
        lores.add(lore);
        this.itemMeta.setLore(lores);
        this.itemStack.setItemMeta(this.itemMeta);
        return this;
    }

    public ItemBuilder setAmount(int amount) {
        this.itemMeta = this.itemStack.getItemMeta();
        this.itemStack.setAmount(amount);
        this.itemStack.setItemMeta(this.itemMeta);
        return this;
    }

    public ItemBuilder setItemMeta(ItemMeta meta) {
        this.itemMeta = this.itemStack.getItemMeta();
        this.itemStack.setItemMeta(meta);
        return this;
    }


    public ItemBuilder addEnchant(Enchantment enchantment, int strength) {
        this.itemMeta = this.itemStack.getItemMeta();
        assert this.itemMeta != null;
        this.itemMeta.addEnchant(enchantment, strength, true);
        this.itemStack.setItemMeta(this.itemMeta);
        return this;
    }

    public ItemBuilder addEnchants(Map<Enchantment, Integer> enchantments) {
        this.itemMeta = this.itemStack.getItemMeta();

        if (!enchantments.isEmpty())
            enchantments.keySet().forEach(enchantment -> this.itemMeta.addEnchant(enchantment, enchantments.get(enchantment), true));

        this.itemStack.setItemMeta(this.itemMeta);
        return this;
    }

    public ItemBuilder addItemFlag(ItemFlag itemflag) {
        this.itemMeta = this.itemStack.getItemMeta();
        Objects.requireNonNull(this.itemMeta).addItemFlags(itemflag);
        this.itemStack.setItemMeta(this.itemMeta);
        return this;
    }

    public ItemBuilder setLores(List<String> lore) {
        this.itemMeta = this.itemStack.getItemMeta();
        Objects.requireNonNull(this.itemMeta).setLore(lore);
        this.itemStack.setItemMeta(this.itemMeta);
        return this;
    }

    public ItemBuilder setUnbreakable() {
        this.itemMeta = this.itemStack.getItemMeta();
        Objects.requireNonNull(this.itemMeta).setUnbreakable(true);
        this.itemStack.setItemMeta(this.itemMeta);
        return this;
    }

    public ItemBuilder setGlow() {
        this.itemMeta = this.itemStack.getItemMeta();
        assert this.itemMeta != null;
        this.itemStack.addUnsafeEnchantment(Enchantment.DURABILITY, 1);
        this.itemStack.setItemMeta(this.itemMeta);
        return this;
    }

    public String toBase64() {
        this.itemMeta = this.itemStack.getItemMeta();
        try {
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            BukkitObjectOutputStream dataOutput = new BukkitObjectOutputStream(outputStream);
            dataOutput.writeObject(this.itemStack);
            dataOutput.close();
            return Base64Coder.encodeLines(outputStream.toByteArray());
        } catch (Exception e) {
            throw new IllegalStateException("Unable to save item stack", e);
        }
    }

    public ItemBuilder fromBase64(String from) {
        this.itemMeta = this.itemStack.getItemMeta();
        try {
            ByteArrayInputStream inputStream = new ByteArrayInputStream(Base64Coder.decodeLines(from));
            BukkitObjectInputStream dataInput = new BukkitObjectInputStream(inputStream);
            this.itemStack = ((ItemStack) dataInput.readObject());
            dataInput.close();
        } catch (java.io.IOException | ClassNotFoundException e) {
            e.printStackTrace();
        }
        return this;
    }

    public ItemStack build() {
        return this.itemStack.clone();
    }

}