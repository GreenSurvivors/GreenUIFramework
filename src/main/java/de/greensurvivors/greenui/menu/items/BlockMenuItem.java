package de.greensurvivors.greenui.menu.items;

import de.greensurvivors.greenui.menu.MenuManager;
import de.greensurvivors.greenui.menu.helper.DirectIntractable;
import de.greensurvivors.greenui.menu.helper.MenuUtils;
import de.greensurvivors.greenui.menu.ui.Menu;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.TradeSelectEvent;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.function.Consumer;

public class BlockMenuItem extends BasicMenuItem implements DirectIntractable, Cloneable {
    protected @NotNull Consumer<Block> blockConsumer;
    protected @NotNull Menu parent;

    public BlockMenuItem(@NotNull Plugin plugin, @NotNull Menu parent, @NotNull Material displayMat, int amount, @Nullable Component name, @Nullable List<Component> description, @NotNull Consumer<Block> blockConsumer) {
        super(plugin, displayMat, amount, name, description);

        this.blockConsumer = blockConsumer;
        this.parent = parent;
    }

    /**
     * called when this Item was clicked.
     * Opens a linked menu
     *
     * @param event the click event that was called
     */
    @Override
    public void onClick(@NotNull InventoryClickEvent event) {
        super.onClick(event);

        switch (event.getClick()) {
            case LEFT, DOUBLE_CLICK, SHIFT_LEFT -> {
                parent.setDirectListener(this);

                // close the menu this Item is in and wait for a interactEvent to happen
                parent.setClosingResult(MenuUtils.MenuClosingResult.REOPEN_LATER);
                Bukkit.getScheduler().runTask(this.plugin, () -> parent.close());
            }
        }
    }

    /**
     * called when a {@link de.greensurvivors.greenui.menu.recipes} Merchant recipe was clicked this item was part of
     * As a MenuItem we probably want to do something, like open a menu
     *
     * @param event the click event that was called
     */
    public void onTradeSelect(@NotNull TradeSelectEvent event) {
        super.onTradeSelect(event);
        parent.setDirectListener(this);

        // close the menu this Item is in and wait for a interactEvent to happen
        parent.setClosingResult(MenuUtils.MenuClosingResult.REOPEN_LATER);
        Bukkit.getScheduler().runTask(this.plugin, () -> parent.close());
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
        Bukkit.getScheduler().runTask(this.plugin, () -> {
            // in the same tick so if the consumer may change the item in this slot it doesn't flash
            updateDisplay(block);

            this.blockConsumer.accept(block);
        });

        return true;
    }

    /**
     * Event-handler when the menu is closed right now and awaits player input
     * The input given by this event is provided by writing a message into chat.
     *
     * @param message the chat message
     * @return always false since we are expecting a block
     */
    @Override
    public boolean onChat(@NotNull Component message) {
        return false;
    }

    /**
     * This tells the intractable to stop waiting for direct input
     * Note: Does not cancel the reopenTimer of the MenuManger,
     * rather is meant for Menus to replace one queued DirectIntractable with another.
     */
    @Override
    public void cancel() {
    }

    public @NotNull Consumer<Block> getBlockConsumer() {
        return this.blockConsumer;
    }

    public void setBlockConsumer(@NotNull Consumer<Block> newConsumer) {
        this.blockConsumer = newConsumer;
    }

    /**
     * updates how the item looks in order to represent what block was interacted with
     */
    protected void updateDisplay(Block block) {
        if (block.getType().isItem()) {
            this.setType(block.getType());
        }

        this.lore(List.of(Component.text(block.getBlockData().getAsString())));
    }

    @Override
    public @NotNull BlockMenuItem clone() {
        BlockMenuItem clone = (BlockMenuItem) super.clone();
        clone.parent = parent; // same instance - everything else would be useless
        clone.blockConsumer = blockConsumer;
        return clone;
    }
}
