package de.greensurvivors.greenui.menu.items;

import de.greensurvivors.greenui.menu.helper.ItemStackInfo;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public class CyclicMenuItem extends BasicMenuItem implements Cloneable {
    protected static final int LORE_ENTRIES = 5; // has to be uneven

    protected @NotNull List<ItemStackInfo> itemStackInfos;
    protected @NotNull Consumer<ItemStackInfo> changeConsumer;
    protected int index = 0;

    /**
     * @param display
     * @param changeConsumer
     */
    public CyclicMenuItem(@NotNull Plugin plugin, @NotNull List<ItemStackInfo> display, @NotNull Consumer<ItemStackInfo> changeConsumer) {
        super(plugin);
        this.itemStackInfos = display;
        this.changeConsumer = changeConsumer;

        updateDisplay();
    }

    protected int modInfosIndex(int i) {
        return Math.floorMod(i, itemStackInfos.size());
    }

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
        Bukkit.getScheduler().runTask(this.plugin, () -> this.changeConsumer.accept(this.itemStackInfos.get(index)));
    }

    public @NotNull List<ItemStackInfo> getItemStackInfos() {
        return itemStackInfos;
    }

    public void addItemInfo(@NotNull ItemStackInfo newInfo) {
        this.itemStackInfos.add(newInfo);
    }

    public void removeItemInfo(@NotNull ItemStackInfo removedInfo) {
        this.itemStackInfos.remove(removedInfo);
    }

    @Override
    public @NotNull CyclicMenuItem clone() {
        CyclicMenuItem clone = (CyclicMenuItem) super.clone();
        // TODO: copy mutable state here, so the clone can't change the internals of the original
        return clone;
    }
}