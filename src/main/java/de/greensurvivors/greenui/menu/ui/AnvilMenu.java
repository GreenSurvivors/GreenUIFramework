package de.greensurvivors.greenui.menu.ui;

import de.greensurvivors.greenui.menu.helper.MenuUtils;
import de.greensurvivors.greenui.menu.items.BasicMenuItem;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.HumanEntity;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.AnvilInventory;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.function.Consumer;

/**
 * please note: in order to edit the ItemName in {@link MenuUtils.TwoCraftSlots#RESULT} there has to be an item in {@link MenuUtils.TwoCraftSlots#LEFT},
 * AnvilMenu will add a paper itself, if a startingText was given
 */
public class AnvilMenu extends BasicCustomInvMenu implements Menu, Cloneable {
    protected @NotNull Consumer<@NotNull ItemStack> itemConsumer;
    protected @Nullable String startingText;

    public AnvilMenu(@NotNull Plugin plugin, boolean shouldReturnedTo, @NotNull Consumer<@NotNull ItemStack> itemConsumer) {
        this(plugin, shouldReturnedTo, false, null, null, itemConsumer);
    }

    public AnvilMenu(@NotNull Plugin plugin, boolean shouldReturnedTo, boolean allowModifyNonMenuItems, @Nullable TextComponent title, @Nullable String startingText, @NotNull Consumer<@NotNull ItemStack> itemConsumer) {
        super(plugin, makeInv(title), shouldReturnedTo, allowModifyNonMenuItems);
        this.itemConsumer = itemConsumer;
        this.startingText = startingText;
        this.title = title;
    }

    /**
     * initialises all important stuff that has to be done,
     * and opens the menu inventory for the player
     *
     * @param player the player who will see this menu
     */
    @Override
    public void open(@NotNull HumanEntity player) {
        super.open(player);

        if (startingText != null) {
            ItemStack left = ((AnvilInventory) inventory).getFirstItem();

            if (left == null) {
                left = new BasicMenuItem(this.plugin, Material.PAPER);
            }

            ItemMeta meta = left.getItemMeta();
            meta.displayName(Component.text(startingText));

            left.setItemMeta(meta);
            ((AnvilInventory) inventory).setFirstItem(left);
        }
    }

    /**
     * just a helper to make the constructor super() call look neater
     */
    protected static Inventory makeInv(@Nullable TextComponent title) {
        if (title == null) {
            return Bukkit.createInventory(null, InventoryType.ANVIL);
        } else {
            return Bukkit.createInventory(null, InventoryType.ANVIL, title);
        }
    }

    /**
     * Event-handler when something gets clicked in the menu
     * in case the result slot gets clicked, the result item is deemed to be accepted and the menu closes
     *
     * @param event the click event that was called
     */
    @Override
    public void onInventoryClick(InventoryClickEvent event) {
        super.onInventoryClick(event);

        if (event.getRawSlot() == MenuUtils.TwoCraftSlots.RESULT.getId() && ((AnvilInventory) inventory).getResult() != null) {
            itemConsumer.accept(((AnvilInventory) inventory).getResult());

            Bukkit.getScheduler().runTask(this.plugin, () -> this.view.close());
            event.setCancelled(true);
        }
    }

    /**
     * set the item name the player starts with.
     * Please note: does nothing after the player has already opened this
     */
    public void setStartingText(@Nullable String newText) {
        this.startingText = newText;
    }

    @Override
    public @NotNull AnvilMenu clone() {
        AnvilMenu clone = (AnvilMenu) super.clone();

        clone.itemConsumer = itemConsumer;
        clone.startingText = startingText;

        return clone;
    }
}
