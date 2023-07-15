package de.greensurvivors.greenui.menu.items.modifiers;

import de.greensurvivors.greenui.Translations.Translator;
import de.greensurvivors.greenui.menu.helper.MenuUtils;
import de.greensurvivors.greenui.menu.items.BasicMenuItem;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.TradeSelectEvent;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;

/**
 * This Modifier can be disabled and doesn't response to {@link BasicMenuItem#onClick(InventoryClickEvent)} and {@link BasicMenuItem#onTradeSelect(TradeSelectEvent)} anymore.
 * The (de)activation has to be done manually by calling {@link DisableModifier#setActiveStat(boolean)} and will never change on its own.
 * If in deactive State this Modifier changes its Material to {@link MenuUtils#getDisabledMaterial()} to reflect the change.
 */
public class DisableModifier<E extends BasicMenuItem> extends BasicModifier<E> implements Cloneable {
    protected boolean active;

    public DisableModifier(@NotNull Plugin plugin, @NotNull Translator translator, @NotNull E menuItem) {
        this(plugin, translator, menuItem, true);
    }

    public DisableModifier(@NotNull Plugin plugin, @NotNull Translator translator, @NotNull E menuItem, boolean activeAtStart) {
        super(plugin, translator, menuItem);
        this.active = activeAtStart;
    }

    /**
     * called when this Item was clicked in a {@link de.greensurvivors.greenui.menu.ui.Menu}.
     * Only if in active state the embedded {@link BasicMenuItem#onClick(InventoryClickEvent)} will be called.
     *
     * @param event the click event that was called
     */
    @Override
    public void onClick(@NotNull InventoryClickEvent event) {
        if (active) {
            super.onClick(event);
        }
    }

    /**
     * called when a {@link de.greensurvivors.greenui.menu.recipes} Merchant recipe was clicked this item was part of
     * Only if in active state the embedded {@link BasicMenuItem#onTradeSelect(TradeSelectEvent)} will be called.
     *
     * @param event the click event that was called
     */
    @Override
    public void onTradeSelect(@NotNull TradeSelectEvent event) {
        if (active) {
            super.onTradeSelect(event);
        }
    }

    /**
     * get if this is active or not
     */
    public boolean getActiveStat() {
        return active;
    }

    /**
     * set the active state of this modifier.
     * note: this updates the Material of this Modifier as well
     */
    public void setActiveStat(boolean nowActive) { //todo lore
        active = nowActive;

        if (active) {
            this.setType(menuItem.getType());
        } else {
            this.setType(MenuUtils.getDisabledMaterial());
        }
    }

    @Override
    public @NotNull DisableModifier<E> clone() {
        DisableModifier<E> clone = (DisableModifier<E>) super.clone();
        clone.active = active;
        return clone;
    }
}
