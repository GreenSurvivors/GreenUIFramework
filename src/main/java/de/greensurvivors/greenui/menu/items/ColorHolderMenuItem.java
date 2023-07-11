package de.greensurvivors.greenui.menu.items;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextColor;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.meta.ColorableArmorMeta;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;

import java.util.function.Consumer;

/**
 * This MenuItem just echos it's set color when clicked
 */
public class ColorHolderMenuItem extends BasicMenuItem implements Cloneable {
    private final Consumer<TextColor> resultingColor;
    private TextColor color;

    /**
     * @param displayMat should be one of {@link Material#LEATHER_HELMET}, {@link Material#LEATHER_CHESTPLATE}, {@link Material#LEATHER_LEGGINGS} or {@link Material#LEATHER_BOOTS}
     *                   the result will be a leather item colored with the given color. If no valid material is given, this will return a colored {@link Material#LEATHER_CHESTPLATE}
     */
    public ColorHolderMenuItem(@NotNull Plugin plugin, @NotNull Material displayMat, int amount, TextColor color, Consumer<TextColor> resultingColor) {
        super(plugin,
                switch (displayMat) {
                    case LEATHER_HELMET, LEATHER_CHESTPLATE, LEATHER_LEGGINGS, LEATHER_BOOTS -> displayMat;
                    default -> Material.LEATHER_CHESTPLATE;
                },
                amount,
                Component.text(color.asHexString()).color(color),
                null
        );

        this.setColor(color);
        this.resultingColor = resultingColor;
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
                    Bukkit.getScheduler().runTask(this.plugin, () -> this.resultingColor.accept(this.color));
        }
    }

    public TextColor getColor() {
        return color;
    }

    public void setColor(TextColor color) {
        if (super.getItemMeta() instanceof ColorableArmorMeta meta) {
            meta.setColor(org.bukkit.Color.fromRGB(color.red(), color.green(), color.blue()));
            super.setItemMeta(meta);
        }

        this.color = color;
    }

    @Override
    public @NotNull ColorHolderMenuItem clone() {
        ColorHolderMenuItem clone = (ColorHolderMenuItem) super.clone();
        // TODO: copy mutable state here, so the clone can't change the internals of the original
        return clone;
    }
}
