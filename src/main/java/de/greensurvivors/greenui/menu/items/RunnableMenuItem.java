package de.greensurvivors.greenui.menu.items;

import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.TradeSelectEvent;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

/**
 * This MenuItem just calls a runnable when clicked
 * please note: this does not create perfekt clones because of the runnable
 */
public class RunnableMenuItem extends BasicMenuItem {
    protected @NotNull Runnable runnable;

    public RunnableMenuItem(@NotNull Plugin plugin, @NotNull Material displayMat, @NotNull Runnable runnable) {
        this(plugin, displayMat, 1, null, null, runnable);
    }

    public RunnableMenuItem(@NotNull Plugin plugin, @NotNull Material displayMat, int amount, @Nullable Component name, @Nullable List<Component> description, @NotNull Runnable runnable) {
        super(plugin, displayMat, amount, name, description);

        this.runnable = runnable;
    }

    /**
     * called when this Item was clicked.
     * calls the linked runnable
     *
     * @param event the click event that was called
     */
    @Override
    public void onClick(@NotNull InventoryClickEvent event) {
        super.onClick(event);

        switch (event.getClick()) {
            //runs the runnable in another sync task to prevent future bugs
            case LEFT, DOUBLE_CLICK, SHIFT_LEFT ->
                    Bukkit.getScheduler().runTask(this.plugin, () -> this.runnable.run());
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

        Bukkit.getScheduler().runTask(this.plugin, () -> this.runnable.run());
    }

    @Override
    @Deprecated
    public @NotNull RunnableMenuItem clone() {
        RunnableMenuItem clone = (RunnableMenuItem) super.clone();
        clone.runnable = runnable;

        return clone;
    }
}
