package de.greensurvivors.greenui.menu.ui;

import de.greensurvivors.greenui.menu.items.BasicMenuItem;
import org.bukkit.Bukkit;
import org.bukkit.entity.HumanEntity;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryView;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Field;
import java.util.UUID;

public class BasicCustomInvMenu implements Menu, Cloneable {
    protected final boolean shouldReturnedTo;
    protected @NotNull Inventory inventory;
    protected boolean allowModifyNonMenuItems;
    //used to update titles
    protected InventoryView view;
    protected UUID viewerUUID;
    protected @NotNull Plugin plugin;
    @Deprecated
    protected @Nullable String title;

    public BasicCustomInvMenu(@NotNull Plugin plugin, @NotNull Inventory inventory, boolean shouldReturnedTo, boolean allowModifyNonMenuItems) {
        this(plugin, inventory, shouldReturnedTo, allowModifyNonMenuItems, null);
    }

    public BasicCustomInvMenu(@NotNull Plugin plugin, @NotNull Inventory inventory, boolean shouldReturnedTo, boolean allowModifyNonMenuItems, @Nullable String title) {
        this.plugin = plugin;
        this.inventory = inventory;
        this.shouldReturnedTo = shouldReturnedTo;
        this.allowModifyNonMenuItems = allowModifyNonMenuItems;
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
        this.viewerUUID = player.getUniqueId();
        Bukkit.getScheduler().runTask(this.plugin, () -> {
            this.view = player.openInventory(this.inventory);

            if (this.view != null && this.title != null) {
                this.view.setTitle(title);
            }
        });
    }

    /**
     * cleanup at the moment before the menu inventory gets closed
     *
     * @return true if the inventory should be forced to stay open aka reopen
     */
    @Override
    public boolean onClose() {
        return false;
    }

    /**
     * Event-handler when something gets clicked in the menu
     *
     * @param event the click event that was called
     */
    @Override
    public void onInventoryClick(InventoryClickEvent event) {
        if (event.getRawSlot() >= inventory.getSize()) {
            if (inventory.getItem(event.getSlot()) instanceof BasicMenuItem menuItem) {
                menuItem.onClick(event);
            } else if (!this.allowModifyNonMenuItems) {
                event.setCancelled(true);
            }
        } else {
            event.setCancelled(true);
        }
    }

    /**
     * set a (menu) item at the slot, replacing whatever items was there before
     * can be used to remove item, just set them null or {@link org.bukkit.Material#AIR}
     *
     * @param newItem the new item to insert
     * @param slotId  the slot the item will be set into
     * @return the itemStack that was replaced, might be null if the slot was empty
     */
    public @Nullable ItemStack setItem(@Nullable ItemStack newItem, int slotId) {
        if (slotId < inventory.getSize()) {
            ItemStack old = inventory.getItem(slotId);

            if (old != null) {
                old = old.clone();
            }

            inventory.setItem(slotId, newItem);

            return old;
        }
        return null;
    }

    /**
     * set the title of the menu
     * please note: this is deprecated and might be replaced with a component method,
     * if paper ever decides to allowing setting component titles in {@link InventoryView}
     */
    @Deprecated
    public void setTitle(String title) {
        this.title = title;

        if (view != null) {
            // why paper?
            view.setTitle(title);
        }
    }

    /**
     * get the size of the connected inventory
     */
    @Override
    public int getSize() {
        return inventory.getSize();
    }

    /**
     * get the (menu) item at the given slotId
     */
    @Override
    public ItemStack getItemAt(int slotId) {
        return this.inventory.getItem(slotId);
    }

    /**
     * get if a child menu should return to this when closed
     */
    @Override
    public boolean shouldReturnedTo() {
        return shouldReturnedTo;
    }

    /**
     * @return returns if the inventory is empty
     */
    @Override
    public boolean isEmpty() {
        return inventory.isEmpty();
    }

    @Override
    public BasicCustomInvMenu clone() {
        try {
            BasicCustomInvMenu clone = (BasicCustomInvMenu) super.clone();
            clone.allowModifyNonMenuItems = allowModifyNonMenuItems;

            Field shouldReturnedField = BasicCustomInvMenu.class.getDeclaredField("shouldReturnedTo");
            shouldReturnedField.setAccessible(true);
            shouldReturnedField.set(clone, shouldReturnedTo);

            clone.allowModifyNonMenuItems = allowModifyNonMenuItems;
            clone.title = title;

            return clone;
        } catch (CloneNotSupportedException e) {
            throw new AssertionError();
        } catch (ReflectiveOperationException ex) {
            throw new AssertionError(ex);
        }
    }

    public void SetMaxStackSize(int maxStackSize) {
        this.inventory.setMaxStackSize(maxStackSize);
    }
}