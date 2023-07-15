package de.greensurvivors.greenui.menu.items.modifiers;

import de.greensurvivors.greenui.Translations.Translator;
import de.greensurvivors.greenui.menu.items.BasicMenuItem;
import org.bukkit.Bukkit;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.TradeSelectEvent;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitTask;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * once this Modifier was used via {@link BasicMenuItem#onClick(InventoryClickEvent)} or {@link BasicMenuItem#onTradeSelect(TradeSelectEvent)}
 * it will deactivate itself and start a timer to set itself active again.
 */
public class CooldownModifier<E extends BasicMenuItem> extends DisableModifier<E> implements Cloneable {
    protected long cooldownTicks;
    protected @Nullable BukkitTask runningTask = null;

    /**
     * @param plugin
     * @param menuItem
     * @param cooldownTicks time in Ticks
     */
    public CooldownModifier(@NotNull Plugin plugin, @NotNull Translator translator, @NotNull E menuItem, long cooldownTicks) {
        this(plugin, translator, menuItem, cooldownTicks, true);
    }

    /**
     * @param plugin
     * @param menuItem
     * @param cooldownTicks
     * @param activeAtStart ime in Ticks
     */
    public CooldownModifier(@NotNull Plugin plugin, @NotNull Translator translator, @NotNull E menuItem, long cooldownTicks, boolean activeAtStart) {
        super(plugin, translator, menuItem, activeAtStart);
        this.cooldownTicks = cooldownTicks;
    }


    /**
     * called when this Item was clicked in a {@link de.greensurvivors.greenui.menu.ui.Menu}.
     * when called, it will deactivate itself and start a cooldown to set itself active again.
     *
     * @param event the click event that was called
     */
    @Override
    public void onClick(@NotNull InventoryClickEvent event) {
        super.onClick(event);

        if (active) {
            setActiveStat(false);
            runningTask = Bukkit.getScheduler().runTaskLater(plugin, () -> setActiveStat(true), cooldownTicks);
        }
    }

    /**
     * called when a {@link de.greensurvivors.greenui.menu.recipes} Merchant recipe was clicked this item was part of
     * when called, it will deactivate itself and start a cooldown to set itself active again.
     *
     * @param event the click event that was called
     */
    @Override
    public void onTradeSelect(@NotNull TradeSelectEvent event) {
        super.onTradeSelect(event);

        if (active) {
            setActiveStat(false);
            runningTask = Bukkit.getScheduler().runTaskLater(plugin, () -> setActiveStat(true), cooldownTicks);
        }
    }

    /**
     * cancels a possible running Task for resetting the state
     *
     * @param nowActive
     */
    @Override
    public void setActiveStat(boolean nowActive) {
        super.setActiveStat(nowActive);

        if (runningTask != null) {
            runningTask.cancel();

            runningTask = null;
        }
    }

    @Override
    public @NotNull CooldownModifier<E> clone() {
        CooldownModifier<E> clone = (CooldownModifier<E>) super.clone();
        clone.cooldownTicks = cooldownTicks;
        return clone;
    }
}
