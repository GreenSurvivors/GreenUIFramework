package de.greensurvivors.greenui.menu.items;

import de.greensurvivors.greenui.menu.MenuManager;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.TradeSelectEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.function.Consumer;

/**
 * MenuItem to receive a bool from the user
 * In contrast to most other menuItems, this handles its own displayitem from the startup
 */
public class BoolMenuItem extends BasicMenuItem implements Cloneable {
    // material to display both possible states
    protected @NotNull ItemStack trueStack;
    protected @NotNull ItemStack falseStack;
    // called whenever the state changes
    protected @NotNull Consumer<@NotNull Boolean> boolConsumer;
    // the state this item is in
    protected boolean stateNow;

    public BoolMenuItem(@NotNull MenuManager manager, @Nullable Component name, boolean startingValue, @NotNull Consumer<Boolean> boolConsumer) {
        this(manager, startingValue, boolConsumer, makeDefaultTrue(name), makeDefaultFalse(name));
    }

    public BoolMenuItem(@NotNull MenuManager manager, boolean startingValue, @NotNull Consumer<Boolean> boolConsumer, @NotNull ItemStack trueStack, @NotNull ItemStack falseStack) {
        super(manager);

        this.boolConsumer = boolConsumer;
        this.stateNow = startingValue;
        this.trueStack = trueStack;
        this.falseStack = falseStack;

        updateState();
    }

    /**
     * create a default representation how "true" should look.
     */
    protected static @NotNull ItemStack makeDefaultTrue(@Nullable Component name) {
        ItemStack result = new ItemStack(Material.LIME_CONCRETE, 1);

        ItemMeta meta = result.getItemMeta();
        meta.lore(List.of(Component.text("true").color(NamedTextColor.GREEN)));
        if (name != null) {
            meta.displayName(name);
        }
        result.setItemMeta(meta);

        return result;
    }

    /**
     * create a default representation how "false" should look.
     */
    protected static @NotNull ItemStack makeDefaultFalse(@Nullable Component name) {
        ItemStack result = new ItemStack(Material.LIME_CONCRETE, 1);

        ItemMeta meta = result.getItemMeta();
        meta.lore(List.of(Component.text("false").color(NamedTextColor.RED)));
        if (name != null) {
            meta.displayName(name);
        }
        result.setItemMeta(meta);

        return result;
    }

    /**
     * called when this Item was clicked.
     * toggles the bool state and update this item to communicate the change
     *
     * @param event the click event that was called
     */
    @Override
    public void onClick(@NotNull InventoryClickEvent event) {
        super.onClick(event);

        switch (event.getClick()) {
            case LEFT, DOUBLE_CLICK, SHIFT_LEFT -> this.stateNow = !this.stateNow;
        }
        // consumer is called whenever the state gets updated
        updateState();
    }

    /**
     * called when a {@link de.greensurvivors.greenui.menu.recipes} Merchant recipe was clicked this item was part of
     * As a MenuItem we probably want to do something, like open a menu
     *
     * @param event the click event that was called
     */
    public void onTradeSelect(@NotNull TradeSelectEvent event) {
        super.onTradeSelect(event);

        this.stateNow = !this.stateNow;

        // consumer is called whenever the state gets updated
        updateState();
    }

    /**
     * called when the state this item is in was changed.
     * the display will reflect this change and the consumer will be called
     */
    protected void updateState() {
        Bukkit.getScheduler().runTask(this.manager.getPlugin(), () -> this.boolConsumer.accept(stateNow));

        if (this.stateNow) {
            this.setType(trueStack.getType());
            this.setItemMeta(trueStack.getItemMeta());
        } else {
            this.setType(falseStack.getType());
            this.setItemMeta(falseStack.getItemMeta());
        }
    }

    /**
     * get the consumer of this item
     */
    public @NotNull Consumer<Boolean> getBoolConsumer() {
        return boolConsumer;
    }

    /**
     * set the consumer of this item
     */
    public void setBoolConsumer(@NotNull Consumer<Boolean> boolConsumer) {
        this.boolConsumer = boolConsumer;
    }

    /**
     * get the current state of this item
     */
    public boolean getState() {
        return stateNow;
    }

    /**
     * set the current state of this item
     */
    public void setState(boolean newState) {
        this.stateNow = newState;
        updateState();
    }

    public @NotNull ItemStack getTrueStack() {
        return this.trueStack;
    }

    public void setTrueStack(@NotNull ItemStack newTrueStack) {
        this.trueStack = newTrueStack;
    }

    public @NotNull ItemStack getFalseStack() {
        return this.falseStack;
    }

    public void setFalseStack(@NotNull ItemStack newFalseStack) {
        this.falseStack = newFalseStack;
    }

    @Override
    public @NotNull BoolMenuItem clone() {
        BoolMenuItem clone = (BoolMenuItem) super.clone();
        clone.boolConsumer = this.boolConsumer;
        clone.stateNow = this.stateNow;

        return clone;
    }
}
