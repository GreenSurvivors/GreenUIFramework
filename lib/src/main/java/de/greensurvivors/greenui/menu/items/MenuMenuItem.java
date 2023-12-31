package de.greensurvivors.greenui.menu.items;

import de.greensurvivors.greenui.menu.MenuManager;
import de.greensurvivors.greenui.menu.helper.OpenGreenUIEvent;
import de.greensurvivors.greenui.menu.ui.Menu;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.TradeSelectEvent;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

/**
 * This MenuItem opens a Menu when clicked
 */
public class MenuMenuItem extends BasicMenuItem implements Cloneable {
    protected @NotNull Menu menuToOpen;
    protected boolean shouldReturnToParent;

    public MenuMenuItem(@NotNull MenuManager manager, @NotNull Material displayMat, @NotNull Menu menuToOpen) {
        this(manager, displayMat, 1, null, null, menuToOpen, true);
    }

    public MenuMenuItem(@NotNull MenuManager manager, @NotNull Material displayMat, int amount, @Nullable Component name, @Nullable List<Component> description, @NotNull Menu menuToOpen, boolean shouldReturnToParent) {
        super(manager, displayMat, amount, name, description);

        this.menuToOpen = menuToOpen;
        this.shouldReturnToParent = shouldReturnToParent;
    }

    public void setDisplayItem(@NotNull ItemStack newItemStack) {
        super.setType(newItemStack.getType());
        super.setItemMeta(newItemStack.getItemMeta());
    }

    /**
     * called when this Item was clicked.
     * Opens a linked menu
     *
     * @param event the click event that was called
     */
    @Override
    public void onClick(@NotNull InventoryClickEvent event) {
        super.onClick(event);

        switch (event.getClick()) {
            case LEFT, DOUBLE_CLICK, SHIFT_LEFT -> Bukkit.getScheduler().runTask(
                    this.manager.getPlugin(), () -> {
                        (new OpenGreenUIEvent(event.getWhoClicked().getUniqueId(), menuToOpen)).callEvent();

                        menuToOpen.open(event.getWhoClicked());
                    }
            );
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
        Bukkit.getScheduler().runTask(this.manager.getPlugin(), () -> (new OpenGreenUIEvent(event.getWhoClicked().getUniqueId(), menuToOpen)).callEvent());
    }

    @Override
    public @NotNull MenuMenuItem clone() {
        MenuMenuItem clone = (MenuMenuItem) super.clone();
        clone.menuToOpen = menuToOpen.clone();
        clone.shouldReturnToParent = shouldReturnToParent;

        return clone;
    }
}
