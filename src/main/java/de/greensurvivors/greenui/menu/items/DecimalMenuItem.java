package de.greensurvivors.greenui.menu.items;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

/**
 * MenuItem to receive a decimal value from the user.
 * The user can click to in/decrease the value, with two configurable step-sizes.
 */
public class DecimalMenuItem extends BasicMenuItem implements Cloneable {
    protected @NotNull DecimalFormat form = new DecimalFormat("#.##");
    // optional upper / lower bounds
    protected @Nullable Double min, max;
    // big steps
    protected int intStepSize;
    // finer steps
    protected double fractionalStepSize;
    // called whenever the value changes
    protected @NotNull Consumer<@NotNull Double> decimalConsumer;
    // the state this menuItem is in
    protected double value;

    public DecimalMenuItem(@NotNull Plugin plugin, @NotNull Material displayMat, @NotNull Consumer<Double> decimalConsumer) {
        this(plugin, displayMat, 1, null, decimalConsumer, 0, null, null, 1, 0.1);
    }

    public DecimalMenuItem(@NotNull Plugin plugin, @NotNull Material displayMat, int amount, @Nullable Component name, @NotNull Consumer<Double> decimalConsumer,
                           double startingValue, @Nullable Double min, @Nullable Double max, int intStepSize, double fractionalStepSize) {
        super(plugin, displayMat, amount, name, null);

        this.decimalConsumer = decimalConsumer;
        this.value = startingValue;
        this.min = min;
        this.max = max;
        this.intStepSize = intStepSize;
        this.fractionalStepSize = fractionalStepSize;

        //stay in bounds
        if (min != null) {
            value = Math.max(min, value);
        }
        if (max != null) {
            value = Math.min(max, value);
        }

        //update result
        updateLore();
        decimalConsumer.accept(value);
    }

    /**
     * updates the lore to reflect changed Values
     */
    public void updateLore() {
        List<Component> newLore = new ArrayList<>();
        Component loreLine = Component.empty();

        if (min != null) {
            loreLine = loreLine.append(Component.text(min).append(Component.text(" < ")).color(NamedTextColor.GREEN));
        }

        loreLine = loreLine.append(Component.text(form.format(value)).color(NamedTextColor.YELLOW));

        if (max != null) {
            loreLine = loreLine.append(Component.text(" < ").append(Component.text(max)).color(NamedTextColor.GREEN));
        }

        newLore.add(loreLine);
        this.lore(newLore);
    }

    /**
     * called when this Item was clicked.
     * In/decreases the decimal value
     *
     * @param event the click event that was called
     */
    @Override
    public void onClick(@NotNull InventoryClickEvent event) {
        super.onClick(event);

        switch (event.getClick()) {
            case LEFT -> value += intStepSize;
            case SHIFT_LEFT -> value += fractionalStepSize;
            case RIGHT -> value -= intStepSize;
            case SHIFT_RIGHT -> value -= fractionalStepSize;
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
        Bukkit.getScheduler().runTask(this.plugin, () -> this.decimalConsumer.accept(value));
    }

    /**
     * get the current value
     */
    public double getValue() {
        return value;
    }

    /**
     * set the current value
     */
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
        decimalConsumer.accept(value);
    }

    @Override
    public @NotNull DecimalMenuItem clone() {
        DecimalMenuItem clone = (DecimalMenuItem) super.clone();
        clone.decimalConsumer = this.decimalConsumer;
        clone.min = this.min;
        clone.max = this.max;
        clone.value = this.value;
        clone.intStepSize = this.intStepSize;
        clone.fractionalStepSize = this.fractionalStepSize;
        clone.form = (DecimalFormat) this.form.clone();
        return clone;
    }
}
