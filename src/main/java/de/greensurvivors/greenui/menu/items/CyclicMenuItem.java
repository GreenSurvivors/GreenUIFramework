package de.greensurvivors.greenui.menu.items;

import de.greensurvivors.greenui.menu.helper.ItemStackInfo;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.TradeSelectEvent;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Collectors;

/**
 * This menuItem has a list of given values it can be in.
 * everytime it was clicked, it will go up / down this list.
 */
public class CyclicMenuItem extends BasicMenuItem implements Cloneable {
    protected static final int LORE_ENTRIES = 5; // has to be uneven to  display an even number of items before / after, and the state we are in, in between

    // holds the list of display items to circle through
    protected @NotNull List<@NotNull ItemStackInfo> itemStackInfos;
    // called whenever the index was changed
    protected @NotNull Consumer<@NotNull ItemStackInfo> infoConsumer;
    // holds the index of the current info taken from itemStackInfos
    protected int index = 0;

    public CyclicMenuItem(@NotNull Plugin plugin, @NotNull List<@NotNull ItemStackInfo> display, @NotNull Consumer<@NotNull ItemStackInfo> infoConsumer) {
        super(plugin);
        this.itemStackInfos = display;
        this.infoConsumer = infoConsumer;

        updateDisplay();
    }

    /**
     * ensures the given index in bounds of the possible indexes of itemStackInfos.
     * It works with modulo, so it will roll over.
     * <p>
     * Please note: this just does math, it will NOT set the current index!
     */
    protected int modInfosIndex(int i) {
        return Math.floorMod(i, itemStackInfos.size());
    }

    /**
     * updates the display depending on {@link CyclicMenuItem#index}
     */
    protected void updateDisplay() {
        if (!itemStackInfos.isEmpty()) {
            ItemStackInfo elementNow = itemStackInfos.get(0);
            super.setType(elementNow.material());
            super.setAmount(elementNow.amount());

            ItemMeta meta = super.getItemMeta();

            //update name
            if (elementNow.name() != null) {
                meta.displayName(elementNow.name());
            } else {
                meta.displayName(Component.text(elementNow.material().name()));
            }

            // make the lore a list of the last and upcoming items
            List<Component> newLore = new ArrayList<>();
            final int dist = (LORE_ENTRIES - 1) / 2;

            for (int i = -dist; i <= dist; i++) {
                ItemStackInfo info = itemStackInfos.get(modInfosIndex(i));
                newLore.add(info.name() == null ? Component.text(info.material().name()) : info.name());
            }

            meta.lore(newLore);
            super.setItemMeta(meta);
        }
    }

    /**
     * called when this Item was clicked.
     * will circle through to the next or last item in list, display it and calls the defined consumer
     *
     * @param event the click event that was called
     */
    @Override
    public void onClick(@NotNull InventoryClickEvent event) {
        super.onClick(event);

        switch (event.getClick()) {
            case LEFT, SHIFT_LEFT -> index = modInfosIndex(++index);
            case RIGHT, SHIFT_RIGHT -> index = modInfosIndex(--index);
            case DOUBLE_CLICK -> {
            } //todo let the player write the name of option
        }
        updateDisplay();
        Bukkit.getScheduler().runTask(this.plugin, () -> this.infoConsumer.accept(this.itemStackInfos.get(index)));
    }

    /**
     * called when a {@link de.greensurvivors.greenui.menu.recipes} Merchant recipe was clicked this item was part of
     * As a MenuItem we probably want to do something, like open a menu
     *
     * @param event the click event that was called
     */
    public void onTradeSelect(@NotNull TradeSelectEvent event) {
        super.onTradeSelect(event);

        index = modInfosIndex(++index);
        updateDisplay();
        Bukkit.getScheduler().runTask(this.plugin, () -> this.infoConsumer.accept(this.itemStackInfos.get(index)));
    }

    public @NotNull List<ItemStackInfo> getItemStackInfos() {
        return itemStackInfos;
    }

    /**
     * add another entry
     */
    public void addItemInfo(@NotNull ItemStackInfo newInfo) {
        this.itemStackInfos.add(newInfo);
    }

    /**
     * remove an entry from the list
     */
    public void removeItemInfo(@NotNull ItemStackInfo removedInfo) {
        this.itemStackInfos.remove(removedInfo);
    }

    @Override
    public @NotNull CyclicMenuItem clone() {
        CyclicMenuItem clone = (CyclicMenuItem) super.clone();

        clone.itemStackInfos = this.itemStackInfos.stream().map(ItemStackInfo::clone).collect(Collectors.toCollection(ArrayList::new));
        clone.infoConsumer = this.infoConsumer;
        clone.index = this.index;
        return clone;
    }
}