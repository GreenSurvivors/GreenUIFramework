package de.greensurvivors.greenui.menu.ui;

import de.greensurvivors.greenui.Translations.Translator;
import de.greensurvivors.greenui.menu.MenuManager;
import de.greensurvivors.greenui.menu.helper.DirectIntractable;
import de.greensurvivors.greenui.menu.helper.MenuUtils;
import de.greensurvivors.greenui.menu.items.BasicMenuItem;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import org.bukkit.Bukkit;
import org.bukkit.block.Block;
import org.bukkit.entity.HumanEntity;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryView;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * most basic menu. You have to provide an Inventory yourself.
 */
public class BasicCustomInvMenu implements Menu, Cloneable {
    protected @NotNull Inventory inventory;
    protected boolean shouldReturnedTo;
    protected boolean allowModifyNonMenuItems;
    protected @NotNull MenuUtils.MenuClosingResult closingResult = MenuUtils.MenuClosingResult.CLOSE;
    //used to update titles
    protected @Nullable InventoryView view;
    protected @Nullable TextComponent title;
    protected @NotNull Plugin plugin;
    protected @Nullable DirectIntractable intractableWaiting = null;
    protected final @NotNull Translator translator;

    public BasicCustomInvMenu(@NotNull Plugin plugin, @NotNull Translator translator, @NotNull Inventory inventory, boolean shouldReturnedTo, boolean allowModifyNonMenuItems) {
        this(plugin, translator, inventory, shouldReturnedTo, allowModifyNonMenuItems, null);
    }

    public BasicCustomInvMenu(@NotNull Plugin plugin, @NotNull Translator translator, @NotNull Inventory inventory, boolean shouldReturnedTo, boolean allowModifyNonMenuItems, @Nullable TextComponent title) {
        this.plugin = plugin;
        this.translator = translator;
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
        Bukkit.getScheduler().runTask(this.plugin, () -> {
            // reset closing result
            closingResult = MenuUtils.MenuClosingResult.CLOSE;

            this.view = player.openInventory(this.inventory);

            if (this.view != null && this.title != null) {
                this.view.setTitle(title.content()); // why just string?!
            }
        });
    }

    /**
     * cleanup at the moment before the menu inventory gets closed
     *
     * @return {@link MenuUtils.MenuClosingResult#STAY_OPEN} if the inventory should be forced to stay open aka reopen
     */
    @Override
    public @NotNull MenuUtils.MenuClosingResult onClose() {
        return this.closingResult;
    }

    /**
     * Event-handler when something gets clicked in the menu
     *
     * @param event the click event that was called
     */
    @Override
    public void onInventoryClick(InventoryClickEvent event) {
        if (event.getRawSlot() < inventory.getSize()) {
            if (inventory.getItem(event.getSlot()) instanceof BasicMenuItem menuItem) {
                menuItem.onClick(event);
            } else if (!this.allowModifyNonMenuItems) {
                event.setCancelled(true);
            }
        }
    }

    /**
     * Event-handler when the menu is closed right now and awaits player input
     * The input given by this event is provided by clicking a block.
     *
     * @param block the interacted block
     * @return true, if the menu accepts the clicked block as input
     * Note: This doesn't necessarily mean the input is valid,
     * but it is feedback for {@link MenuManager}
     * to reopen this menu.
     */
    @Override
    public boolean onBlockInteract(@NotNull Block block) {
        if (intractableWaiting != null) {
            return intractableWaiting.onBlockInteract(block);
        } else {
            // just accept it to get reopened
            return true;
        }
    }

    /**
     * Event-handler when the menu is closed right now and awaits player input
     * The input given by this event is provided by writing a message into chat.
     *
     * @param message the chat message
     * @return true, if the menu accepts the message as input
     * Note: This doesn't necessarily mean the input is valid,
     * but it is feedback for {@link MenuManager}
     * to reopen this menu.
     */
    @Override
    public boolean onChat(@NotNull Component message) {
        if (intractableWaiting != null) {
            return intractableWaiting.onChat(message);
        } else {
            // just accept it to get reopened
            return true;
        }
    }

    /**
     * This tells the intractable to stop waiting for direct input
     * Note: Does not cancel the reopenTimer of the MenuManger,
     * rather is meant for Menus to replace one queued DirectIntractable with another.
     */
    @Override
    public void cancel() {
        if (this.intractableWaiting != null) {
            this.intractableWaiting.cancel();
        }
    }

    /**
     * closes the open InventoryView of this menu
     * Note: don't call this directly on events, use {@link org.bukkit.scheduler.BukkitScheduler#runTask(Plugin, Runnable)}
     */
    @Override
    public void close() {
        if (this.view != null) {
            this.view.close();
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
     */
    public void setTitle(TextComponent title) {
        this.title = title;

        if (view != null) {
            // why paper?
            view.setTitle(title.content());
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
     * get if non MenuItems are allowed to be interacted with
     */
    @Override
    public boolean allowModifyNonMenuItems() {
        return this.allowModifyNonMenuItems;
    }

    /**
     * Some MenuItems implement {@link DirectIntractable} and want to get direct input from the player.
     * In order to not relay the data from DirectIntractable to every item, the one that listens right now has to be registered.
     *
     * @param intractable a MenuItem implementing DirectIntractable
     */
    @Override
    public void setDirectListener(@NotNull DirectIntractable intractable) {
        this.intractableWaiting = intractable;
    }

    public void setClosingResult(MenuUtils.MenuClosingResult closingResult) {
        this.closingResult = closingResult;
    }

    /**
     * @return returns if the inventory is empty
     */
    @Override
    public boolean isEmpty() {
        return inventory.isEmpty();
    }

    @Override
    public @NotNull BasicCustomInvMenu clone() {
        try {
            BasicCustomInvMenu clone = (BasicCustomInvMenu) super.clone();
            clone.allowModifyNonMenuItems = allowModifyNonMenuItems;
            clone.shouldReturnedTo = this.shouldReturnedTo;
            clone.title = title;

            return clone;
        } catch (CloneNotSupportedException e) {
            throw new AssertionError();
        }
    }

    public void SetMaxStackSize(int maxStackSize) {
        this.inventory.setMaxStackSize(maxStackSize);
    }
}
