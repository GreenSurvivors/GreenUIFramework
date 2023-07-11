package de.greensurvivors.greenui.menu.ui;

import de.greensurvivors.greenui.menu.helper.MenuDefaults;
import de.greensurvivors.greenui.menu.items.ColorHolderMenuItem;
import de.greensurvivors.greenui.menu.items.RunnableMenuItem;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.util.HSVLike;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.function.Consumer;

public class ColorPickerMenu extends BasicMenu {
    //7 or bigger
    protected static final int HUE_AMOUNT = 25;
    protected final @NotNull ColorHolderMenuItem[] hueItems = new ColorHolderMenuItem[HUE_AMOUNT];
    protected final @NotNull Consumer<TextColor> result;
    protected int scrollIndex = 0;

    public ColorPickerMenu(@NotNull Plugin plugin, boolean shouldReturnToParent, @NotNull Consumer<TextColor> result) {
        this(plugin, shouldReturnToParent, false, "", 6, result);
    }

    public ColorPickerMenu(@NotNull Plugin plugin, boolean shouldReturnToParent, boolean allowModifyNonMenuItems, @Nullable String title, int rows, @NotNull Consumer<TextColor> result) {
        super(plugin, shouldReturnToParent, allowModifyNonMenuItems, title, rows);

        this.result = result;

        //init hue values
        for (int i = 0; i < HUE_AMOUNT; i++) {
            TextColor textColor = TextColor.color(HSVLike.hsvLike(i * (1.0f / (HUE_AMOUNT - 1)), 1.0f, 1.0f));
            hueItems[i] = (new ColorHolderMenuItem(this.plugin, Material.LEATHER_CHESTPLATE, 1, textColor, this::updateDisplay));
        }

        //set color selector up
        for (int i = 0; i < 5 * 9; i++) {
            setItem(new ColorHolderMenuItem(this.plugin, Material.LEATHER_CHESTPLATE, 1, NamedTextColor.BLACK, resultingColor -> {
                this.result.accept(resultingColor);
                Bukkit.getScheduler().runTask(this.plugin, () -> super.view.close());
            }), i);
        }

        //set hue scroller up
        setItem(new RunnableMenuItem(this.plugin, MenuDefaults.getPageMaterial(), 1, Component.text("<-"), null,
                () -> {
                    scrollIndex = Math.floorMod(scrollIndex - 3, HUE_AMOUNT);
                    setHueBar(scrollIndex);
                }), 5 * 9 - 1);
        setItem(new RunnableMenuItem(this.plugin, MenuDefaults.getPageMaterial(), 1, Component.text("->"), null,
                () -> {
                    scrollIndex = Math.floorMod(scrollIndex + 3, HUE_AMOUNT);
                    setHueBar(scrollIndex);
                }), 6 * 9 - 1);
        setHueBar(0);

        updateDisplay(hueItems[0].getColor());
    }

    private void setHueBar(int startingFrom) {
        for (int i = 0; i < 7; i++) {
            setItem(hueItems[Math.floorMod(i + startingFrom, HUE_AMOUNT)], 5 * 9 + i);
        }
    }

    private void updateDisplay(TextColor color) {
        float hue = HSVLike.fromRGB(color.red(), color.blue(), color.green()).h();

        for (int y = 0; y < 5; y++) {
            for (int x = 0; x < 9; x++) {
                float saturation = 1.0f - (y * (1.0f / 4));
                float brightness = 1.0f - (x * (1.0f / 8));
                HSVLike targetColor = HSVLike.hsvLike(hue, saturation, brightness);

                ((ColorHolderMenuItem) getItemAt(y * 9 + x)).setColor(TextColor.color(targetColor));
            }
        }
    }
}