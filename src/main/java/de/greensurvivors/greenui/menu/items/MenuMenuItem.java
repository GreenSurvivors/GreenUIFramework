package de.greensurvivors.greenui.menu.items;

import de.greensurvivors.greenui.menu.helper.OpenGreenUIEvent;
import de.greensurvivors.greenui.menu.ui.Menu;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

/**
 * This MenuItem opens a Menu when clicked
 */
public class MenuMenuItem extends BasicMenuItem implements Cloneable {
    protected @NotNull Menu menuToOpen;
    protected boolean shouldReturnToParent;

    public MenuMenuItem(@NotNull Plugin plugin, @NotNull Material displayMat, @NotNull Menu menuToOpen) {
        this(plugin, displayMat, 1, null, null, menuToOpen, true);
    }

    public MenuMenuItem(@NotNull Plugin plugin, @NotNull Material displayMat, int amount, @Nullable Component name, @Nullable List<Component> description, @NotNull Menu menuToOpen, boolean shouldReturnToParent) {
        super(plugin, displayMat, amount, name, description);

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
                    this.plugin, () -> (new OpenGreenUIEvent(event.getWhoClicked().getUniqueId(), menuToOpen)).callEvent()
            );
        }
    }

    @Override
    public @NotNull MenuMenuItem clone() {
        MenuMenuItem clone = (MenuMenuItem) super.clone();
        clone.menuToOpen = menuToOpen.clone();
        clone.shouldReturnToParent = shouldReturnToParent;

        return clone;
    }
}
