package de.greensurvivors.greenui.menu.ui;

import de.greensurvivors.greenui.menu.MenuManager;
import de.greensurvivors.greenui.menu.helper.DirectIntractable;
import de.greensurvivors.greenui.menu.helper.MenuUtils;
import de.greensurvivors.greenui.menu.items.BasicMenuItem;
import de.greensurvivors.greenui.menu.recipes.BasicMenuRecipe;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.HumanEntity;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.inventory.TradeSelectEvent;
import org.bukkit.inventory.*;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * The merchant inventory will be only created by the server if a player opens it.
 * So until then we use an anvil inventory as placeholder, since it also has 2 slots and a result slot.
 * As soon as {@link TradeMenu#open(HumanEntity)} will be called the old inventory instance will be become invalid,
 * so be aware of it!
 * Also, the Inventory doesn't get recycled. While we cache the last inventory, these caches become invalid as soon as a player
 * opens the inventory again.
 */
public class TradeMenu implements Menu, Cloneable {
    protected @NotNull List<@NotNull MerchantRecipe> recipes;
    protected @NotNull Merchant merchant;
    protected Inventory inventory;
    protected boolean shouldReturnedTo;
    protected boolean allowModifyNonMenuItems;
    protected @NotNull MenuUtils.MenuClosingResult closingResult = MenuUtils.MenuClosingResult.CLOSE;
    //used to update titles
    protected InventoryView view;
    protected @Nullable TextComponent title;
    protected @NotNull Plugin plugin;
    protected @Nullable DirectIntractable intractableWaiting;

    public TradeMenu(@NotNull Plugin plugin, boolean shouldReturnedTo, boolean allowModifyNonMenuItems) {
        this(plugin, shouldReturnedTo, allowModifyNonMenuItems, null, List.of());
    }

    public TradeMenu(@NotNull Plugin plugin, boolean shouldReturnedTo, boolean allowModifyNonMenuItems, @Nullable TextComponent title, @NotNull List<@NotNull MerchantRecipe> recipes) {
        this.plugin = plugin;
        this.shouldReturnedTo = shouldReturnedTo;
        this.allowModifyNonMenuItems = allowModifyNonMenuItems;
        this.title = title;
        // make sure the list is mutable
        this.recipes = new ArrayList<>(recipes);

        this.merchant = Bukkit.createMerchant(title);
        this.merchant.getRecipes().clear();
        this.merchant.setRecipes(this.recipes);

        // this is just a temporary placeholder since there is no way without nms to create a merchant inventory
        // before a player had opened it
        this.inventory = Bukkit.createInventory(null, InventoryType.ANVIL);
    }

    /**
     * initialises all important stuff that has to be done,
     * and opens the menu inventory for the player,
     * therefore the AnvilInventory from the constructor becomes invalid
     *
     * @param player the player who will see this menu
     */
    @Override
    public void open(@NotNull HumanEntity player) {
        Bukkit.getScheduler().runTask(this.plugin, () -> {
            // reset closing result
            this.closingResult = MenuUtils.MenuClosingResult.CLOSE;

            this.view = player.openMerchant(this.merchant, true);

            if (view != null) {
                // map items
                Arrays.stream(MenuUtils.TwoCraftSlots.values()).forEach(slot -> this.inventory.setItem(slot.getId(), this.inventory.getItem(slot.getId())));

                // change inventory to be the correct one.
                this.inventory = view.getTopInventory();

                if (this.title != null) {
                    this.view.setTitle(title.content()); // why just string?!
                }
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
        return closingResult;
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
        } else {
            event.setCancelled(true);
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
        return false;
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
        return false;
    }

    /**
     * This tells the intractable to stop waiting for direct input
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
     * Event-handler when a recipe gets selected in the menu
     *
     * @param event the trade select event that was called
     */
    public void onTradeSelect(TradeSelectEvent event) {
        if (merchant.getRecipe(event.getIndex()) instanceof BasicMenuRecipe menuRecipe) {
            menuRecipe.onSelect(event);
        } else if (!this.allowModifyNonMenuItems) {
            event.setCancelled(true);
        }
    }

    /**
     * set a (menu) item at the slot, replacing whatever items was there before
     * can be used to remove item, just set them null or {@link Material#AIR}
     *
     * @param newItem the new item to insert
     * @param slotId  the slot the item will be set into
     * @return the itemStack that was replaced, might be null if the slot was empty
     */
    @Override
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
    public void setDirectListener(DirectIntractable intractable) {
        this.intractableWaiting = intractable;
    }

    /**
     * set how the Menu should respond to getting closed
     */
    @Override
    public void setClosingResult(@NotNull MenuUtils.MenuClosingResult closingResult) {
        this.closingResult = closingResult;
    }

    /**
     * @return returns if the inventory(/ies) is(/are) empty
     * Please note: this does not mean it has no recipes!
     */
    @Override
    public boolean isEmpty() {
        return inventory.isEmpty();
    }

    @Override
    public @NotNull TradeMenu clone() {
        try {
            TradeMenu clone = (TradeMenu) super.clone();
            clone.allowModifyNonMenuItems = allowModifyNonMenuItems;
            clone.shouldReturnedTo = this.shouldReturnedTo;
            clone.title = title;

            clone.merchant = this.merchant;

            clone.recipes = new ArrayList<>(this.recipes.stream().map(
                    recipe -> new MerchantRecipe(recipe.getResult(), recipe.getUses(),
                            recipe.getMaxUses(), recipe.hasExperienceReward(),
                            recipe.getVillagerExperience(), recipe.getPriceMultiplier(),
                            recipe.getDemand(), recipe.getSpecialPrice(),
                            recipe.shouldIgnoreDiscounts())
            ).toList());

            return clone;
        } catch (CloneNotSupportedException e) {
            throw new AssertionError();
        }
    }
}
