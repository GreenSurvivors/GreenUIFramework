package de.greensurvivors.greenui.menu.helper;

import org.bukkit.Material;

public class MenuDefaults {
    /**
     * Material, if you want to use a save button
     */
    public static Material getSaveMaterial() {
        return Material.CHEST;
    }

    /**
     * Material to indicate additional information
     */
    public static Material getInfoMaterial() {
        return Material.LIGHT;
    }

    /**
     * Material to fill slots without doing anything
     */
    public static Material getFillerMaterial() {
        return Material.GRAY_STAINED_GLASS_PANE;
    }

    /**
     * Material to indicate something or someone gets deleted.
     * Like if you want to remove a value from a string
     */
    public static Material getDestroyMaterial() {
        return Material.TNT;
    }

    /**
     * Material something gets created. like creating a new page
     */
    public static Material getCreateMaterial() {
        return Material.CRAFTING_TABLE;
    }

    /**
     * Material to indicate this MenuItem is currently unnavigable
     */
    public static Material getDisabledMaterial() {
        return Material.BARRIER;
    }

    /**
     * Next / last page and scrolling
     */
    public static Material getPageMaterial() {
        return Material.ARROW;
    }

    /**
     * you want to search for something or filter your input?
     */
    public static Material getFilterMaterial() {
        return Material.SPYGLASS;
    }
}
