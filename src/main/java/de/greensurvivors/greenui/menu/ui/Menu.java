package de.greensurvivors.greenui.menu.ui;

import de.greensurvivors.greenui.menu.helper.DirectIntractable;
import de.greensurvivors.greenui.menu.helper.MenuUtils;
import org.bukkit.entity.HumanEntity;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public interface Menu extends DirectIntractable, Cloneable {
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
     * @return {@link MenuUtils.MenuClosingResult#STAY_OPEN} if the inventory should be forced to stay open aka reopen
     */
    @NotNull MenuUtils.MenuClosingResult onClose();

    /**
     * Event-handler when something gets clicked in the menu
     *
     * @param event the click event that was called
     */
    void onInventoryClick(InventoryClickEvent event);

    /**
     * closes the open InventoryView of this menu
     * Note: don't call this directly on InventoryInteract Events, use {@link org.bukkit.scheduler.BukkitScheduler#runTask(Plugin, Runnable)}
     */
    void close();

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
     * get if non MenuItems are allowed to be interacted with
     */
    boolean allowModifyNonMenuItems();

    /**
     * Some MenuItems implement {@link DirectIntractable} and want to get direct input from the player.
     * In order to not relay the data from DirectIntractable to every item, the one that listens right now has to be registered.
     *
     * @param intractable a MenuItem implementing DirectIntractable
     */
    void setDirectListener(DirectIntractable intractable);

    /**
     * set how the Menu should respond to getting closed
     */
    void setClosingResult(MenuUtils.MenuClosingResult closingResult);

    /**
     * @return returns if the inventory(/ies) is(/are) empty
     */
    boolean isEmpty();

    @NotNull Menu clone();
}
