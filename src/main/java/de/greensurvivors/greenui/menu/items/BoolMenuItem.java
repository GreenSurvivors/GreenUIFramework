package de.greensurvivors.greenui.menu.items;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Material;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.function.Consumer;

public class BoolMenuItem extends BasicMenuItem implements Cloneable {
    protected final @NotNull Consumer<Boolean> consumer;
    private final @NotNull ItemStack TRUE_STACK = new ItemStack(Material.LIME_CONCRETE, 1);
    private final @NotNull ItemStack FALSE_STACK = new ItemStack(Material.RED_CONCRETE, 1);
    protected boolean stateNow;

    /**
     * in contrast to most other menuItems, this handles its own displayitem from the startup
     *
     * @param name
     * @param startingValue
     * @param consumer
     */
    public BoolMenuItem(@NotNull Plugin plugin, @Nullable Component name, boolean startingValue, @NotNull Consumer<Boolean> consumer) {
        super(plugin);

        this.consumer = consumer;
        this.stateNow = startingValue;

        //set up meta for both sides
        ItemMeta meta = TRUE_STACK.getItemMeta();
        meta.lore(List.of(Component.text("true").color(NamedTextColor.GREEN)));
        if (name != null) {
            meta.displayName(name);
        }
        TRUE_STACK.setItemMeta(meta);

        meta = FALSE_STACK.getItemMeta();
        meta.lore(List.of(Component.text("false").color(NamedTextColor.RED)));
        if (name != null) {
            meta.displayName(name);
        }
        FALSE_STACK.setItemMeta(meta);

        updateState();
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

    private void updateState() {
        this.consumer.accept(stateNow);

        if (this.stateNow) {
            this.setType(TRUE_STACK.getType());
            this.setItemMeta(TRUE_STACK.getItemMeta());
        } else {
            this.setType(FALSE_STACK.getType());
            this.setItemMeta(FALSE_STACK.getItemMeta());
        }
    }

    public boolean getState() {
        return stateNow;
    }

    public void setState(boolean newState) {
        this.stateNow = newState;
        updateState();
    }

    @Override
    public @NotNull BoolMenuItem clone() {
        BoolMenuItem clone = (BoolMenuItem) super.clone();
        // TODO: copy mutable state here, so the clone can't change the internals of the original
        return clone;
    }
}
