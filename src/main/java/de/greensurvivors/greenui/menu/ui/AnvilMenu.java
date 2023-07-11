package de.greensurvivors.greenui.menu.ui;

import de.greensurvivors.greenui.menu.items.BasicMenuItem;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.HumanEntity;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.AnvilInventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.function.Consumer;

/**
 * please note: in order to edit the ItemName in {@link Slots#RESULT} there has to be an item in {@link Slots#LEFT},
 * AnvilMenu will add a paper itself, if a startingText was given
 */
public class AnvilMenu extends BasicMenu implements Menu, Cloneable {
    protected @NotNull Consumer<ItemStack> resultItemConsumer;
    protected @Nullable String startingText;

    public AnvilMenu(@NotNull Plugin plugin, boolean shouldReturnedTo, @NotNull Consumer<ItemStack> resultItemConsumer) {
        this(plugin, shouldReturnedTo, false, null, resultItemConsumer);
    }

    public AnvilMenu(@NotNull Plugin plugin, boolean shouldReturnedTo, boolean allowModifyNonMenuItems, @Nullable String startingText, @NotNull Consumer<ItemStack> resultItemConsumer) {
        super(plugin, shouldReturnedTo, allowModifyNonMenuItems);
        this.resultItemConsumer = resultItemConsumer;
        this.startingText = startingText;

        if (this.title != null) {
            this.inventory = Bukkit.createInventory(null, InventoryType.ANVIL, this.title);
        } else {
            this.inventory = Bukkit.createInventory(null, InventoryType.ANVIL);
        }
    }

    /**
     * initialises all important stuff that has to be done,
     * and opens the menu inventory for the player
     *
     * @param player the player who will see this menu
     */
    @Override
    public void open(@NotNull HumanEntity player) {
        super.open(player);

        if (startingText != null) {
            ItemStack left = ((AnvilInventory) inventory).getFirstItem();

            if (left == null) {
                left = new BasicMenuItem(this.plugin, Material.PAPER);
            }

            ItemMeta meta = left.getItemMeta();
            meta.displayName(Component.text(startingText));

            left.setItemMeta(meta);
            ((AnvilInventory) inventory).setFirstItem(left);
        }
    }

    /**
     * Event-handler when something gets clicked in the menu
     * in case the result slot gets clicked, the result item is deemed to be accepted and the menu closes
     *
     * @param event the click event that was called
     */
    @Override
    public void onInventoryClick(InventoryClickEvent event) {
        if (event.getRawSlot() == Slots.RESULT.getId()) {
            resultItemConsumer.accept(((AnvilInventory) inventory).getResult());
        }

        super.onInventoryClick(event);

        Bukkit.getScheduler().runTask(this.plugin, () -> this.view.close());
    }

    /**
     * set the item name the player starts with
     */
    public void setStartingText(@Nullable String newText) {
        this.startingText = newText;
    }

    @Override
    public @NotNull AnvilMenu clone() {
        AnvilMenu clone = (AnvilMenu) super.clone();

        clone.resultItemConsumer = resultItemConsumer;
        clone.startingText = startingText;

        return clone;
    }

    public enum Slots {
        LEFT(0),
        RIGHT(1),
        RESULT(2);

        private final int id;

        Slots(int id) {
            this.id = id;
        }

        public int getId() {
            return id;
        }
    }
}
