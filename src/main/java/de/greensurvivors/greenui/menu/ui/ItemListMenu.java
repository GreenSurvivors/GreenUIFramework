package de.greensurvivors.greenui.menu.ui;

import de.greensurvivors.greenui.Translations.Translator;
import net.kyori.adventure.text.TextComponent;
import org.bukkit.Bukkit;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.UnknownNullability;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

/**
 * in this menu one can add and remove items;
 * no {@link de.greensurvivors.greenui.menu.items.BasicMenuItem#onClick(InventoryClickEvent)} will be called except for the most bottom row.
 */
public class ItemListMenu extends BasicMultiPageMenu implements Cloneable {
    protected @NotNull Consumer<@NotNull List<@NotNull ItemStack>> itemListConsumer;

    public ItemListMenu(@NotNull Plugin plugin, @NotNull Translator translator, boolean shouldReturnToParent, @NotNull Consumer<@NotNull List<@NotNull ItemStack>> itemListConsumer) {
        this(plugin, translator, shouldReturnToParent, null, 6, itemListConsumer);
    }

    public ItemListMenu(@NotNull Plugin plugin, @NotNull Translator translator, boolean shouldReturnToParent, @Nullable TextComponent title, int rows, @NotNull Consumer<@NotNull List<@NotNull ItemStack>> itemListConsumer) {
        super(plugin, translator, shouldReturnToParent, true, title, rows);

        this.itemListConsumer = itemListConsumer;
    }

    /**
     * Event-handler when something gets clicked in the menu
     * the event handler of the current page will be called,
     * ignores all ClickEvents, but the ones of the last row
     *
     * @param event the click event that was called
     */
    @Override
    public void onInventoryClick(InventoryClickEvent event) {
        if (event.getRawSlot() >= this.getSize() - 9 && event.getRawSlot() < this.getSize()) {
            super.onInventoryClick(event);

            Bukkit.getScheduler().runTask(this.plugin, () -> itemListConsumer.accept(getAllItems(true)));

            // don't allow modification of the last row
            event.setCancelled(true);
        }
    }


    /**
     * if allSlots ist set to true, null values in the itemStacks are allowed
     * does NOT contain items from the last row!
     *
     * @param allSlots if empty slots should be contained (true) as null or jumped over (false)
     * @return list containing all the items in this Menu
     */
    public @NotNull List<@UnknownNullability ItemStack> getAllItems(boolean allSlots) {
        ArrayList<ItemStack> itemStacks = new ArrayList<>();
        for (int pageNumer = 0; pageNumer < pages.size(); pageNumer++) {
            for (int slotId = 0; slotId < getSize() - 9; slotId++) {
                ItemStack itemStack = getItemAt(pageNumer, slotId);

                if (allSlots || itemStack != null) {
                    itemStacks.add(itemStack);
                }
            }
        }

        return itemStacks;
    }

    @Override
    public @NotNull ItemListMenu clone() {
        ItemListMenu clone = (ItemListMenu) super.clone();
        clone.itemListConsumer = this.itemListConsumer;
        return clone;
    }
}
