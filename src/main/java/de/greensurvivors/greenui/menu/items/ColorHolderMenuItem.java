package de.greensurvivors.greenui.menu.items;

import de.greensurvivors.greenui.Translations.Translator;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextColor;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.TradeSelectEvent;
import org.bukkit.inventory.meta.ColorableArmorMeta;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;

import java.util.function.Consumer;

/**
 * This MenuItem just echos it's set color when clicked
 */
public class ColorHolderMenuItem extends BasicMenuItem implements Cloneable {
    // called whenever the item was clicked
    protected @NotNull Consumer<@NotNull TextColor> colorConsumer;
    // the state of this item
    protected @NotNull TextColor color; // don't let the IDE fool you. it's getting set via this.setColor(color) in constructor.

    /**
     * @param displayMat should be one of {@link Material#LEATHER_HELMET}, {@link Material#LEATHER_CHESTPLATE}, {@link Material#LEATHER_LEGGINGS} or {@link Material#LEATHER_BOOTS}
     *                   the result will be a leather item colored with the given color. If no valid material is given, this will return a colored {@link Material#LEATHER_CHESTPLATE}
     */
    public ColorHolderMenuItem(@NotNull Plugin plugin, @NotNull Translator translator, @NotNull Material displayMat, int amount, @NotNull TextColor color, Consumer<TextColor> colorConsumer) {
        super(plugin, translator,
                switch (displayMat) {
                    case LEATHER_HELMET, LEATHER_CHESTPLATE, LEATHER_LEGGINGS, LEATHER_BOOTS -> displayMat;
                    default -> Material.LEATHER_CHESTPLATE;
                },
                amount,
                Component.text(color.asHexString()).color(color),
                null
        );

        this.setColor(color);
        this.colorConsumer = colorConsumer;
    }

    /**
     * called when this Item was clicked.
     * Accepts the current color
     *
     * @param event the click event that was called
     */
    @Override
    public void onClick(@NotNull InventoryClickEvent event) {
        super.onClick(event);

        switch (event.getClick()) {
            case LEFT, DOUBLE_CLICK, SHIFT_LEFT ->
                    Bukkit.getScheduler().runTask(this.plugin, () -> this.colorConsumer.accept(this.color));
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
        Bukkit.getScheduler().runTask(this.plugin, () -> this.colorConsumer.accept(this.color));
    }

    /**
     * get the consumer of this item
     */
    public @NotNull Consumer<TextColor> getColorConsumer() {
        return colorConsumer;
    }

    /**
     * change the consumer of this item
     */
    public void setColorConsumer(@NotNull Consumer<TextColor> colorConsumer) {
        this.colorConsumer = colorConsumer;
    }

    /**
     * get the color this item displays
     */
    public @NotNull TextColor getColor() {
        return color;
    }

    /**
     * set the color this item displays
     */
    public void setColor(@NotNull TextColor color) {
        if (super.getItemMeta() instanceof ColorableArmorMeta meta) {
            meta.setColor(org.bukkit.Color.fromRGB(color.red(), color.green(), color.blue()));
            super.setItemMeta(meta);
        }

        this.color = color;
    }

    @Override
    public @NotNull ColorHolderMenuItem clone() {
        ColorHolderMenuItem clone = (ColorHolderMenuItem) super.clone();
        clone.color = TextColor.color(color.value());
        clone.colorConsumer = this.colorConsumer;
        return clone;
    }
}
