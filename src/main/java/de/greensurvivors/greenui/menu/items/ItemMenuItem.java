package de.greensurvivors.greenui.menu.items;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TranslatableComponent;
import org.bukkit.Material;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.function.Consumer;

public class ItemMenuItem extends BasicMenuItem implements Cloneable {
    protected @NotNull Consumer<@Nullable ItemStack> consumer;
    protected final static @NotNull Material emptyMaterial = Material.STRUCTURE_VOID;
    //translations are registered at the MenuManager
    protected final static @NotNull TranslatableComponent EMPTY = Component.translatable().key("menu.item_menu_item.empty").fallback("empty").append().build(); //todo translation

    public ItemMenuItem(@NotNull Plugin plugin, @NotNull Consumer<ItemStack> consumer) {
        super(plugin, emptyMaterial, 1, EMPTY, null);

        this.consumer = consumer;
    }

    /**
     * called when this Item was clicked.
     * accepts a new Item
     *
     * @param event the click event that was called
     */
    public void onClick(@NotNull InventoryClickEvent event) {

        if (this.getType() == Material.STRUCTURE_VOID && this.displayName().equals(EMPTY)){
            // if self is "empty" representation, only accept swaps; no places, pickup, moves, drops, collections
            // placing an item here would only increase the amount (since the slot holds an item in reality) and that's no good for being "empty"
            // swaps however may be handled as places
            switch (event.getAction()) {
                case // Modified other slots
                     PICKUP_ALL,
                     MOVE_TO_OTHER_INVENTORY,
                     COLLECT_TO_CURSOR,
                     HOTBAR_SWAP, // always just a move to hotbar since this slot is never empty
                     // Modified cursor and clicked
                     PICKUP_SOME,
                     PICKUP_HALF,
                     PICKUP_ONE,
                     PLACE_ALL,
                     PLACE_SOME,
                     PLACE_ONE,
                     // Modified clicked only
                     DROP_ALL_SLOT,
                     DROP_ONE_SLOT,
                     // whatever happened here, we can't support it.
                     UNKNOWN -> {
                        event.setCancelled(true);
                }
                // Modified other slots
                case HOTBAR_MOVE_AND_READD -> {
                    int hotbarSlot = event.getHotbarButton();
                    if (hotbarSlot > 0) {
                        ItemStack swappedStack = event.getWhoClicked().getInventory().getItem(hotbarSlot);

                        if (swappedStack != null && !swappedStack.getType().isAir()) {
                            this.setType(swappedStack.getType());
                            this.setAmount(swappedStack.getAmount());
                            this.setItemMeta(swappedStack.getItemMeta());
                        }

                        consumer.accept(getCleanCopy ());
                        event.setCancelled(true);
                    }
                }
                // update with cursor
                case SWAP_WITH_CURSOR -> {
                    ItemStack onCursor = event.getCursor();
                    if (onCursor != null) {
                        this.setType(onCursor.getType());
                        this.setAmount(onCursor.getAmount());
                        this.setItemMeta(onCursor.getItemMeta());
                    }

                    consumer.accept(getCleanCopy ());
                    event.setCancelled(true);
                }
                // Modified cursor only and nothing we don't care about
                case DROP_ALL_CURSOR,
                     DROP_ONE_CURSOR,
                     CLONE_STACK,
                     NOTHING -> {
                }
            }
        } else {
            //todo
            switch (event.getAction()) {
                // Modified other slots
                case MOVE_TO_OTHER_INVENTORY -> { //cancel, set self to none but update other slot nevertheless
                    event.getWhoClicked().getInventory().addItem(getCleanCopy());
                    setEmpty();

                    this.consumer.accept(null);
                    event.setCancelled(true);
                }
                case PICKUP_ALL -> { // cancel, set self to none, update other slots and cursor nevertheless (geht in den curser, außer der hat bereits item & slot acceptiert keine Items wie ein result slot, dann wandert es zum anderen Inv)
                    if (event.getView().getSlotType(event.getSlot()) == InventoryType.SlotType.RESULT){
                        event.getWhoClicked().getInventory().addItem(getCleanCopy());
                    } else {
                        event.getView().setCursor(getCleanCopy());
                    }

                    setEmpty();
                    event.setCancelled(true);
                }
                case HOTBAR_MOVE_AND_READD -> { //cancel, set self to none, update hotbar & other slot (hotbarslot wird getauscht)
                    int hotbarSlot = event.getHotbarButton();

                    if (hotbarSlot > 0){
                        ItemStack hotbarCopy = event.getWhoClicked().getInventory().getItem(hotbarSlot);

                        if (hotbarCopy != null){
                            hotbarCopy = hotbarCopy.clone();
                        }

                        event.getWhoClicked().getInventory().setItem(hotbarSlot, getCleanCopy());

                        if (hotbarCopy != null){
                            this.setType(hotbarCopy.getType());
                            this.setAmount(hotbarCopy.getAmount());
                            this.setItemMeta(hotbarCopy.getItemMeta());

                            this.consumer.accept(getCleanCopy());
                        } else {
                            setEmpty();
                            consumer.accept(null);
                        }
                    }

                    event.setCancelled(true);
                }
                case COLLECT_TO_CURSOR -> {// cancel, other items might be menu items as well
                    event.setCancelled(true);
                }
                case HOTBAR_SWAP -> { // cancel accept new item stack and update hotbar (hotbar slot is empty)
                    int hotbarSlot = event.getHotbarButton();

                    if (hotbarSlot > 0){
                        event.getWhoClicked().getInventory().setItem(hotbarSlot, getCleanCopy());

                        setEmpty();
                        consumer.accept(null);
                    }

                    event.setCancelled(true);
                }
                // Modified cursor and clicked
                case PICKUP_SOME -> { // cancel, test if this would be empty and set self to none if true; else update self
                    // taken from {@link net.minecraft.server.network.ServerGamePacketListenerImpl#handleContainerClick(ServerboundContainerClickPacket)}
                    int toPlace = event.getClick() == ClickType.LEFT ? event.getCursor().getAmount() : 1;
                    toPlace = Math.min(toPlace, this.getMaxStackSize() - this.getAmount());
                    toPlace = Math.min(toPlace, event.getClickedInventory().getMaxStackSize() - this.getAmount());

                    // should be negative
                    toPlace = Math.abs(toPlace);

                    ItemStack copy = getCleanCopy();
                    copy.setAmount(toPlace);

                    this.setAmount(this.getAmount() - toPlace);
                    event.getView().setCursor(copy);

                    this.consumer.accept(getCleanCopy());
                    event.setCancelled(true);
                }
                case PICKUP_HALF -> {
                    float half = (float)this.getAmount() / 2.0F;

                    ItemStack copy = getCleanCopy();
                    copy.setAmount((int)Math.ceil(half));

                    this.setAmount((int)Math.floor(half));
                    event.getView().setCursor(copy);

                    this.consumer.accept(getCleanCopy());
                    event.setCancelled(true);
                }
                case PICKUP_ONE -> {
                    if (this.getAmount() > 1){
                        if (this.isSimilar(event.getCursor())){
                            event.getCursor().setAmount(event.getCursor().getAmount() + 1);
                        } else {
                            ItemStack clone = getCleanCopy();
                            clone.setAmount(1);
                            event.getView().setCursor(clone);
                        }

                        this.setAmount(this.getAmount() -1);
                        this.consumer.accept(getCleanCopy());
                    } else {
                        if (this.isSimilar(event.getCursor())){
                            event.getCursor().setAmount(event.getCursor().getAmount() + 1);
                        } else {
                            ItemStack clone = getCleanCopy();
                            clone.setAmount(1);
                            event.getView().setCursor(clone);
                        }

                        setEmpty();
                        consumer.accept(null);
                    }

                    event.setCancelled(true);
                }
                case PLACE_ALL -> {
                    if (event.getCursor() != null) {
                        int newAmount = event.getCursor().getAmount();

                        //remove ItemStack from cursor
                        event.getCursor().setType(Material.AIR);
                        event.getCursor().setAmount(0);

                        this.setAmount(this.getAmount() + newAmount);
                        this.consumer.accept(getCleanCopy ());
                    }
                    event.setCancelled(true);
                }
                case PLACE_SOME -> { // remove cursor itemstack if empty, update amounts
                    if (event.getCursor() != null) {
                        // taken from {@link net.minecraft.server.network.ServerGamePacketListenerImpl#handleContainerClick(ServerboundContainerClickPacket)}
                        int toPlace = event.getClick() == ClickType.LEFT ? event.getCursor().getAmount() : 1;
                        toPlace = Math.min(toPlace, this.getMaxStackSize() - this.getAmount());
                        if (event.getClickedInventory() != null) {
                            toPlace = Math.min(toPlace, event.getClickedInventory().getMaxStackSize() - this.getAmount());
                        }

                        // remove cursor itemStack if empty
                        if (event.getCursor().getAmount() <= toPlace) {
                            event.getCursor().setType(Material.AIR);
                        }

                        // update amounts after transfer
                        event.getCursor().setAmount(event.getCursor().getAmount() - toPlace);
                        this.setAmount(this.getAmount() + toPlace);

                        this.consumer.accept(getCleanCopy ());
                    }

                    event.setCancelled(true);
                }
                case PLACE_ONE -> {
                    if (event.getCursor() != null) {
                        if (event.getCursor().getAmount() <= 1) { //remove from cursor if there was only one present
                            event.getCursor().setType(Material.AIR);
                        } else {
                            event.getCursor().setAmount(event.getCursor().getAmount() - 1);
                        }

                        this.setAmount(this.getAmount() + 1);
                        this.consumer.accept(getCleanCopy ());
                    }

                    event.setCancelled(true);
                }
                case SWAP_WITH_CURSOR -> {
                    if (event.getCursor() != null) {
                        ItemStack cursorClone = event.getCursor().clone();

                        event.getView().setCursor(getCleanCopy());

                        this.setType(cursorClone.getType());
                        this.setAmount(cursorClone.getAmount());
                        this.setItemMeta(cursorClone.getItemMeta());

                        this.consumer.accept(this);
                    } else {
                        event.getView().setCursor(getCleanCopy());

                        setEmpty();
                        this.consumer.accept(null);
                    }

                    event.setCancelled(true);
                }
                // Modified clicked only
                case DROP_ALL_SLOT -> { // cancel, set self to none, & drop item
                    event.getWhoClicked().getWorld().dropItemNaturally(event.getWhoClicked().getLocation(), getCleanCopy ());
                    setEmpty();

                    this.consumer.accept(null);
                    event.setCancelled(true);
                }
                case DROP_ONE_SLOT -> { // cancel, test if this would be empty and set self to none if true; drop item
                    // we have to make a copy with amount of one, in case the amount of this is higher than one,
                    // so we drop only one but don't change this
                    ItemStack clone = this.clone();
                    clone.setAmount(1);
                    event.getWhoClicked().getWorld().dropItemNaturally(event.getWhoClicked().getLocation(), clone);

                    if (this.getAmount() <= 1) {
                        setEmpty();
                        this.consumer.accept(null);
                    } else {
                        this.consumer.accept(this);
                    }

                    event.setCancelled(true);
                }
                // Modified cursor only or Nothing
                case DROP_ALL_CURSOR, // don't care
                        DROP_ONE_CURSOR,
                        CLONE_STACK,
                        NOTHING -> {
                }
                case UNKNOWN -> { // whatever happened we can't support it
                    event.setCancelled(true);
                }
            }
        }


        //event.getHotbarButton() > 0
    }

    protected void setEmpty(){
        this.setType(emptyMaterial);
        this.setAmount(1);

        // get clean itemMeta
        this.setItemMeta((new ItemStack(emptyMaterial)).getItemMeta());
    }

    /**
     * gets a copy, that is NOT a MenuItem
     * @return
     */
    protected ItemStack getCleanCopy (){
        ItemStack result = new ItemStack(this.getType(), this.getAmount());
        result.setItemMeta(this.getItemMeta());

        return result;
    }

    @Override
    public @NotNull ItemMenuItem clone() {
        ItemMenuItem clone = (ItemMenuItem) super.clone();
        // TODO: copy mutable state here, so the clone can't change the internals of the original
        return clone;
    }
}
