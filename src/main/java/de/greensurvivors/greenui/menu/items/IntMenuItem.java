package de.greensurvivors.greenui.menu.items;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public class IntMenuItem extends BasicMenuItem implements Cloneable {
    protected @Nullable Integer min, max;
    protected int value;
    protected @NotNull Consumer<Integer> consumer;

    public IntMenuItem(@NotNull Plugin plugin, @NotNull Material displayMat, @NotNull Consumer<Integer> consumer) {
        this(plugin, displayMat, 1, null, consumer, 0, null, null);
    }

    public IntMenuItem(@NotNull Plugin plugin, @NotNull Material displayMat, int amount, @Nullable Component name, @NotNull Consumer<Integer> consumer, int startingValue, @Nullable Integer min, @Nullable Integer max) {
        super(plugin, displayMat, amount, name, null);

        this.consumer = consumer;
        this.value = startingValue;
        this.min = min;
        this.max = max;

        //stay in bounds
        if (min != null) {
            value = Math.max(min, value);
        }
        if (max != null) {
            value = Math.min(max, value);
        }

        //update result
        updateLore();
        consumer.accept(value);
    }

    public void updateLore() {
        List<Component> newLore = new ArrayList<>();
        Component loreLine = Component.empty();

        if (min != null) {
            loreLine = loreLine.append(Component.text(min).append(Component.text(" < ")).color(NamedTextColor.GREEN));
        }

        loreLine = loreLine.append(Component.text(value).color(NamedTextColor.YELLOW));

        if (max != null) {
            loreLine = loreLine.append(Component.text(" < ").append(Component.text(max)).color(NamedTextColor.GREEN));
        }

        newLore.add(loreLine);
        this.lore(newLore);
    }

    /**
     * called when this Item was clicked.
     * in/decreases the integer value
     *
     * @param event the click event that was called
     */
    @Override
    public void onClick(@NotNull InventoryClickEvent event) {
        super.onClick(event);

        switch (event.getClick()) {
            case LEFT -> value++;
            case SHIFT_LEFT -> value += 10;
            case RIGHT -> value--;
            case SHIFT_RIGHT -> value -= 10;
            case DOUBLE_CLICK -> {
            } //todo let the user directly input, maybe via anvil?
        }

        //stay in bounds
        if (min != null) {
            value = Math.max(min, value);
        }
        if (max != null) {
            value = Math.min(max, value);
        }

        //update this
        updateLore();
        Bukkit.getScheduler().runTask(this.plugin, () -> this.consumer.accept(value));
    }

    public int getValue() {
        return value;
    }

    public void setValue(int newValue) {
        this.value = newValue;

        //stay in bounds
        if (min != null) {
            value = Math.max(min, value);
        }
        if (max != null) {
            value = Math.min(max, value);
        }

        //update result
        updateLore();
        consumer.accept(value);
    }

    @Override
    public @NotNull IntMenuItem clone() {
        IntMenuItem clone = (IntMenuItem) super.clone();
        // TODO: copy mutable state here, so the clone can't change the internals of the original
        return clone;
    }
}
