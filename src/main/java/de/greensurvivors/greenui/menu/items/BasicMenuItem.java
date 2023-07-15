package de.greensurvivors.greenui.menu.items;

import de.greensurvivors.greenui.menu.MenuManager;
import de.greensurvivors.greenui.menu.helper.MenuUtils;
import net.kyori.adventure.text.Component;
import org.bukkit.Material;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.TradeSelectEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

/**
 * framework for all MenuItems,
 * it does nothing, so it is perfekt for filler or disabled items (you have to set the material for disabled yourself)!
 * <p>
 * In order to get recognized as a special items by the menus, all kinds of MenuItems have to expand this class.
 */
public class BasicMenuItem extends ItemStack implements Cloneable {
    protected @NotNull MenuManager manager;

    /**
     * new basic menu item, defaults to {@link MenuUtils#getFillerMaterial()}
     */
    public BasicMenuItem(@NotNull MenuManager manager) {
        this(manager, MenuUtils.getFillerMaterial());
    }

    public BasicMenuItem(@NotNull MenuManager manager, @NotNull Material material) {
        super(material);

        this.manager = manager;
    }

    /**
     * copy the given itemStack
     */
    public BasicMenuItem(@NotNull MenuManager manager, @NotNull ItemStack displayItemStack) {
        super(displayItemStack.getType());
        super.setAmount(displayItemStack.getAmount());
        super.setItemMeta(displayItemStack.getItemMeta().clone());

        this.manager = manager;
    }

    public BasicMenuItem(@NotNull MenuManager manager, @NotNull Material displayMat, int amount, @Nullable Component name, @Nullable List<Component> description) {
        super(displayMat, amount);
        this.manager = manager;

        ItemMeta meta = super.getItemMeta();

        if (name != null) {
            meta.displayName(name);
        }

        if (description != null) {
            meta.lore(description);
        }

        super.setItemMeta(meta);
    }

    /**
     * called when this Item was clicked in a {@link de.greensurvivors.greenui.menu.ui.Menu}.
     * As a MenuItem we probably want to do something, like open a menu
     *
     * @param event the click event that was called
     */
    public void onClick(@NotNull InventoryClickEvent event) {
        event.setCancelled(true);
    }

    /**
     * called when a {@link de.greensurvivors.greenui.menu.recipes} Merchant recipe was clicked this item was part of
     * As a MenuItem we probably want to do something, like open a menu
     *
     * @param event the click event that was called
     */
    public void onTradeSelect(@NotNull TradeSelectEvent event) {
    }

    @Override
    public @NotNull BasicMenuItem clone() {
        return new BasicMenuItem(this.manager, super.clone());
    }
}
