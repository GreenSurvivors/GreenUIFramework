package de.greensurvivors.greenui.menu.ui;

import de.greensurvivors.greenui.menu.MenuManager;
import net.kyori.adventure.text.TextComponent;
import org.bukkit.Bukkit;
import org.bukkit.inventory.Inventory;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * basic chest-like menu
 */
public class BasicMenu extends BasicCustomInvMenu implements Menu, Cloneable {
    public BasicMenu(@NotNull MenuManager manager, boolean shouldReturnedTo, boolean allowModifyNonMenuItems) {
        this(manager, shouldReturnedTo, allowModifyNonMenuItems, null, 6);
    }

    public BasicMenu(@NotNull MenuManager manager, boolean shouldReturnedTo, boolean allowModifyNonMenuItems, @Nullable TextComponent title, int rows) {
        super(manager, makeInv(title, rows), shouldReturnedTo, allowModifyNonMenuItems);
    }

    /**
     * just a helper to make the constructor super() call look neater
     */
    protected static Inventory makeInv(@Nullable TextComponent title, int rows) {
        final int size = 9 * Math.min(6, Math.max(2, rows));

        if (title == null) {
            return Bukkit.createInventory(null, size);
        } else {
            return Bukkit.createInventory(null, size, title);
        }
    }

    @Override
    public @NotNull BasicMenu clone() {
        return (BasicMenu) super.clone();
    }
}
