package de.greensurvivors.greenui.menu.items.modifiers;

import de.greensurvivors.greenui.Translations.Translator;
import de.greensurvivors.greenui.menu.items.BasicMenuItem;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.TradeSelectEvent;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;

/**
 * A Modifier takes an MenuItem - maybe another Modifier - and adds new behavior to it.
 * This is just the framework for all the other Modifiers and does by itself nothing than
 * housing another MenuItem and call its {@link BasicMenuItem#onClick(InventoryClickEvent)} and {@link BasicMenuItem#onTradeSelect(TradeSelectEvent)}
 *
 * @param <E> The kind of MenuItem this Modifier holds
 */
public class BasicModifier<E extends BasicMenuItem> extends BasicMenuItem implements Cloneable {
    protected @NotNull E menuItem;

    public BasicModifier(@NotNull Plugin plugin, @NotNull Translator translator, @NotNull E menuItem) {
        super(plugin, translator, menuItem);
        this.menuItem = menuItem;
    }

    /**
     * called when this Item was clicked in a {@link de.greensurvivors.greenui.menu.ui.Menu}.
     * As a MenuItem we probably want to do something, like open a menu
     *
     * @param event the click event that was called
     */
    @Override
    public void onClick(@NotNull InventoryClickEvent event) {
        menuItem.onClick(event);

        // the embedded might have changed, so we want to update ourselves
        copyEmbedded();
    }

    /**
     * called when a {@link de.greensurvivors.greenui.menu.recipes} Merchant recipe was clicked this item was part of
     * As a MenuItem we probably want to do something, like open a menu
     *
     * @param event the click event that was called
     */
    @Override
    public void onTradeSelect(@NotNull TradeSelectEvent event) {
        menuItem.onTradeSelect(event);
        // the embedded might have changed, so we want to update ourselves
        copyEmbedded();
    }

    /**
     * updates this item to look like the embedded MenuItem
     */
    public void copyEmbedded() {
        if (menuItem instanceof BasicModifier<?> basicModifier) {
            basicModifier.copyEmbedded();
        }

        super.setType(menuItem.getType());
        super.setAmount(menuItem.getAmount());
        super.setItemMeta(menuItem.getItemMeta().clone());
    }

    /**
     * get the embedded MenuItem
     */
    public @NotNull E getMenuItem() {
        return menuItem;
    }

    /**
     * change the embedded MenuItem to another
     */
    public void setMenuItem(@NotNull E menuItem) {
        this.menuItem = menuItem;

        copyEmbedded();
    }

    @Override
    public @NotNull BasicModifier<E> clone() {
        BasicModifier<E> clone = (BasicModifier<E>) super.clone();
        clone.menuItem = menuItem;
        return clone;
    }
}
