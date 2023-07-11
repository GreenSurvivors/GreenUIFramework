package de.greensurvivors.greenui.menu.ui;

import org.bukkit.entity.HumanEntity;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public interface Menu extends Cloneable { //todo permission checks and disabling of menu items if necessary
    /**
     * initialises all important stuff that has to be done,
     * and opens the menu inventory for the player
     *
     * @param player the player who will see this menu
     */
    void open(@NotNull HumanEntity player);

    /**
     * cleanup at the moment before the menu inventory gets closed
     *
     * @return true if the inventory should be forced to stay open aka reopen
     */
    boolean onClose();

    /**
     * Event-handler when something gets clicked in the menu
     *
     * @param event the click event that was called
     */
    void onInventoryClick(InventoryClickEvent event);

    /**
     * set a (menu) item at the slot, replacing whatever items was there before
     * can be used to remove item, just set them null or {@link org.bukkit.Material#AIR}
     *
     * @param newItem the new item to insert
     * @param slotId  the slot the item will be set into
     * @return the itemStack that was replaced, might be null if the slot was empty
     */
    @Nullable ItemStack setItem(@Nullable ItemStack newItem, int slotId);

    /**
     * get the size of the connected inventory
     */
    int getSize();

    /**
     * get the (menu) item at the given slotId
     */
    ItemStack getItemAt(int slot);

    /**
     * get if a child menu should return to this when closed
     */
    boolean shouldReturnedTo();

    /**
     * @return returns if the inventory(/ies) is(/are) empty
     */
    boolean isEmpty();

    Menu clone();
}
