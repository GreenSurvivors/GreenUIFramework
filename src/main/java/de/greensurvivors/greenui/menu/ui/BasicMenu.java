package de.greensurvivors.greenui.menu.ui;

import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;


public class BasicMenu extends BasicCustomInvMenu implements Menu, Cloneable {
    public BasicMenu(@NotNull Plugin plugin, boolean shouldReturnedTo, boolean allowModifyNonMenuItems) {
        this(plugin, shouldReturnedTo, allowModifyNonMenuItems, "", 6);
    }

    public BasicMenu(@NotNull Plugin plugin, boolean shouldReturnedTo, boolean allowModifyNonMenuItems, @Nullable String title, int rows) {
        super(plugin, title != null ? Bukkit.createInventory(null, makeSize(rows), title) : Bukkit.createInventory(null, makeSize(rows)),
                shouldReturnedTo, allowModifyNonMenuItems);
    }

    private static int makeSize(int rows) {
        return 9 * Math.min(6, Math.max(2, rows));
    }

    @Override
    public BasicMenu clone() {
        return (BasicMenu) super.clone();
    }
}
