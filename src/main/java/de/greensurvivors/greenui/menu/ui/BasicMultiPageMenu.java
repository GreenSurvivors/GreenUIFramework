package de.greensurvivors.greenui.menu.ui;

import de.greensurvivors.greenui.menu.MenuManager;
import de.greensurvivors.greenui.menu.helper.DirectIntractable;
import de.greensurvivors.greenui.menu.helper.MenuUtils;
import de.greensurvivors.greenui.menu.helper.OpenGreenUIEvent;
import de.greensurvivors.greenui.menu.items.RunnableMenuItem;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import org.bukkit.Bukkit;
import org.bukkit.block.Block;
import org.bukkit.entity.HumanEntity;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.InventoryView;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class BasicMultiPageMenu implements Menu, Cloneable { //todo optional filter
    protected final int rows;
    protected boolean shouldReturnToParent;
    protected @NotNull HashMap<@NotNull Integer, @NotNull BasicMenu> pages = new HashMap<>();
    protected boolean allowModifyNonMenuItems;
    protected TextComponent title;
    //used to update titles
    protected @Nullable InventoryView view = null;
    protected @NotNull Plugin plugin;
    protected @NotNull MenuUtils.MenuClosingResult closingResult = MenuUtils.MenuClosingResult.CLOSE;
    protected int openPage = 0;
    protected @Nullable DirectIntractable intractableWaiting = null;

    public BasicMultiPageMenu(@NotNull Plugin plugin, boolean shouldReturnToParent) {
        this(plugin, shouldReturnToParent, false, null, 6);
    }

    public BasicMultiPageMenu(@NotNull Plugin plugin, boolean shouldReturnToParent, boolean allowModifyNonMenuItems, @Nullable TextComponent title, int rows) {
        this.plugin = plugin;
        this.shouldReturnToParent = shouldReturnToParent;
        this.allowModifyNonMenuItems = allowModifyNonMenuItems;
        this.rows = Math.min(6, Math.max(rows, 2));
        this.title = title;
    }

    /**
     * initialises all important stuff that has to be done,
     * and opens the menu inventory for the player at the first page
     *
     * @param player the player who will see this menu
     */
    public void open(@NotNull HumanEntity player) {
        closingResult = MenuUtils.MenuClosingResult.CLOSE;

        this.open(player, 0);
    }

    /**
     * initialises all important stuff that has to be done,
     * and opens the menu inventory for the player
     * at the specific page
     *
     * @param player the player who will see this menu
     */
    public void open(@NotNull HumanEntity player, int pageNumber) {
        final int rangedPageNumber = Math.max(0, Math.min(pages.size() - 1, pageNumber));
        this.openPage = rangedPageNumber;
        BasicMenu menu = pages.get(rangedPageNumber);

        if (rangedPageNumber > 0) {
            menu.setItem(new RunnableMenuItem(this.plugin, MenuUtils.getPageMaterial(), rangedPageNumber /* start counting from 1 in UI*/, Component.text("<-"), null, () -> {
                this.open(player, rangedPageNumber - 1);
            }), getSize() - 9 - 1);
        }

        if (rangedPageNumber < pages.size()) {
            menu.setItem(new RunnableMenuItem(this.plugin, MenuUtils.getPageMaterial(), rangedPageNumber + 2/* start counting from 1 in UI*/, Component.text("->"), null, () -> {
                this.open(player, rangedPageNumber + 1);
            }), getSize() - 1);
        }

        menu.open(player);
        if (this.view != null && this.title != null) {
            this.view.setTitle(title.content());
        }

        Bukkit.getScheduler().runTask(this.plugin, () -> (new OpenGreenUIEvent(player.getUniqueId(), menu)).callEvent());
    }

    /**
     * cleanup at the moment before the menu inventory gets closed
     *
     * @return {@link MenuUtils.MenuClosingResult#STAY_OPEN} if the inventory should be forced to stay open aka reopen
     */
    @Override
    public @NotNull MenuUtils.MenuClosingResult onClose() {
        return closingResult;
    }

    /**
     * Event-handler when something gets clicked in the menu
     * the event handler of the current page will be called
     *
     * @param event the click event that was called
     */
    @Override
    public void onInventoryClick(InventoryClickEvent event) {
        pages.get(this.openPage).onInventoryClick(event);
    }

    /**
     * Event-handler when the menu is closed right now and awaits player input
     * The input given by this event is provided by clicking a block.
     *
     * @param block the interacted block
     * @return true, if the menu accepts the clicked block as input
     * Note: This doesn't necessarily mean the input is valid,
     * but it is feedback for {@link MenuManager}
     * to reopen this menu.
     */
    @Override
    public boolean onBlockInteract(@NotNull Block block) {
        if (intractableWaiting != null) {
            return intractableWaiting.onBlockInteract(block);
        } else {
            // just accept it to get reopened
            return true;
        }
    }

    /**
     * Event-handler when the menu is closed right now and awaits player input
     * The input given by this event is provided by writing a message into chat.
     *
     * @param message the chat message
     * @return true, if the menu accepts the message as input
     * Note: This doesn't necessarily mean the input is valid,
     * but it is feedback for {@link MenuManager}
     * to reopen this menu.
     */
    @Override
    public boolean onChat(@NotNull Component message) {
        if (intractableWaiting != null) {
            return intractableWaiting.onChat(message);
        } else {
            // just accept it to get reopened
            return true;
        }
    }

    /**
     * This tells the intractable to stop waiting for direct input
     * Note: Does not cancel the reopenTimer of the MenuManger,
     * rather is meant for Menus to replace one queued DirectIntractable with another.
     */
    @Override
    public void cancel() {
        if (this.intractableWaiting != null) {
            this.intractableWaiting.cancel();
        }
    }

    /**
     * closes the open InventoryView of this menu
     * Note: don't call this directly on events, use {@link org.bukkit.scheduler.BukkitScheduler#runTask(Plugin, Runnable)}
     */
    @Override
    public void close() {
        if (this.view != null) {
            this.view.close();
        }
    }

    /**
     * makes sure the page exists, creating it if necessary
     */
    private void ensurePageExists(int pageNumber) {
        if (pageNumber >= pages.size()) {
            int diff = pages.size() - pageNumber;
            int start = pages.size();

            for (int i = 0; i <= diff; i++) {
                pages.put(start + i, new BasicMenu(plugin, false, allowModifyNonMenuItems, (TextComponent) title, rows));
            }
        }
    }

    /**
     * set the title of all pages
     */
    public void setTitle(TextComponent title) {
        this.title = title;

        if (view != null) {
            // why paper?
            view.setTitle(title.content());
        }
    }

    /**
     * set a (menu) item at the slot at the FIRST page, replacing whatever items was there before
     * can be used to remove item, just set them null or {@link org.bukkit.Material#AIR}
     * please note: in the bottom row the left/right most item may be replaced with a page button
     *
     * @param newItem the new item to insert
     * @param slotId  the slot the item will be set into
     * @return the itemStack that was replaced, might be null if the slot was empty
     */
    @Override
    public @Nullable ItemStack setItem(@Nullable ItemStack newItem, int slotId) {
        return setItem(newItem, 0, slotId);
    }

    /**
     * set a (menu) item at the slot at the specified page, replacing whatever items was there before
     * can be used to remove items, just set them null or {@link org.bukkit.Material#AIR}
     * please note: in the bottom row the left/right most item may be replaced with a page button
     *
     * @param newItem    the new item to insert
     * @param pageNumber the page to insert to
     * @param slotId     the slot the item will be set into
     * @return the itemStack that was replaced, might be null if the slot was empty
     */
    public @Nullable ItemStack setItem(@Nullable ItemStack newItem, int pageNumber, int slotId) {
        slotId = Math.max(0, Math.min(getSize() - 1, slotId));

        ensurePageExists(pageNumber);

        return pages.get(pageNumber).setItem(newItem, slotId);
    }

    /**
     * set a list of (menu) items all at once, starting at the first slot at the first page
     * please note: the bottom-most row will not be automatically filled, so this method acts as if this row doesn't exist.
     * if you specifically want to set items in this row use {@link #setItem(ItemStack, int, int)}
     *
     * @param listToSet the new items to insert
     * @return a map containing all replaced items with the keys page, slotId
     */
    public @NotNull Map<@NotNull Integer, @NotNull Map<@NotNull Integer, @NotNull ItemStack>> setItems(@NotNull List<ItemStack> listToSet) {
        return setItems(listToSet, 0, 0);
    }

    /**
     * set a list of (menu) items all at once, starting at the specified slot at the specified page
     * please note: the bottom-most row will not be automatically filled, so this method acts as if this row doesn't exist.
     * if you specifically want to set items in this row use {@link #setItem(ItemStack, int, int)}
     *
     * @param listToSet  the new items to insert
     * @param pageNumber the page to start inserting to, will overflow into the next pages if needed
     * @param startingId from 0 to {@link #getSize()} / 9 - 9 -1 are valid values
     * @return a map containing all replaced items with the keys page, slotId
     */
    public @NotNull Map<@NotNull Integer, @NotNull Map<@NotNull Integer, @NotNull ItemStack>> setItems(@NotNull List<ItemStack> listToSet, int pageNumber, int startingId) {
        startingId = Math.max(0, startingId);

        final int MAX_INDEX = getSize() / 9 - 9/*one row*/ - 1/*starting index is 0*/;

        if (startingId > MAX_INDEX) {
            startingId = 0;
            pageNumber++;
        }

        ensurePageExists(pageNumber);

        Map<@NotNull Integer, @NotNull Map<@NotNull Integer, @NotNull ItemStack>> result = new HashMap<>();
        result.put(pageNumber, new HashMap<>());

        Iterator<ItemStack> listToSetIt = listToSet.listIterator();
        Menu pageNow = pages.get(pageNumber);
        for (int pageNumberNow = pageNumber, idNow = startingId; listToSetIt.hasNext(); idNow++) {
            if (idNow > MAX_INDEX) {
                idNow = 0;
                pageNumberNow++;

                ensurePageExists(pageNumberNow);
                pageNow = pages.get(pageNumberNow);

                result.put(pageNumberNow, new HashMap<>());
            }

            ItemStack replaced = pageNow.setItem(listToSetIt.next(), idNow);

            if (replaced != null) {
                result.get(pageNumberNow).put(idNow, replaced);
            }
        }

        //clean empty maps where for a page no items where replaced
        result.entrySet().removeIf(entry -> entry.getValue().isEmpty());

        return result;
    }

    /**
     * get the size of the connected inventory
     */
    @Override
    public int getSize() {
        return 9 * rows;
    }

    /**
     * get the (menu) item at the given slot from the first page
     */
    @Override
    public ItemStack getItemAt(int slotId) {
        return getItemAt(0, slotId);
    }

    /**
     * get the (menu) item from the given page and slot
     */
    public ItemStack getItemAt(int pageNumber, int slotId) {
        if (pageNumber < 0 || pageNumber >= pages.size()) {
            return null;
        }

        return pages.get(pageNumber).getItemAt(slotId);
    }

    /**
     * get if a child menu should return to this when closed
     */
    @Override
    public boolean shouldReturnedTo() {
        return shouldReturnToParent;
    }

    /**
     * get if non MenuItems are allowed to be interacted with
     */
    @Override
    public boolean allowModifyNonMenuItems() {
        return this.allowModifyNonMenuItems;
    }

    /**
     * Some MenuItems implement {@link DirectIntractable} and want to get direct input from the player.
     * In order to not relay the data from DirectIntractable to every item, the one that listens right now has to be registered.
     *
     * @param intractable a MenuItem implementing DirectIntractable
     */
    @Override
    public void setDirectListener(@NotNull DirectIntractable intractable) {
        this.intractableWaiting = intractable;
    }

    /**
     * set how the Menu should respond to getting closed
     */
    @Override
    public void setClosingResult(@NotNull MenuUtils.MenuClosingResult closingResult) {
        this.closingResult = closingResult;
    }

    /**
     * @return returns if all inventories are completely empty
     */
    @Override
    public boolean isEmpty() {
        for (BasicMenu menu : pages.values()) {
            if (!menu.isEmpty()) {
                return false;
            }
        }

        return true;
    }

    /**
     * removes empty pages
     */
    public void trimPages() {
        final HashMap<Integer, BasicMenu> result = new HashMap<>();

        int idIt = 0, newNextId = 0;
        for (Iterator<BasicMenu> menuIterator = pages.values().iterator(); menuIterator.hasNext(); idIt++) {
            BasicMenu menu = menuIterator.next();

            if (!menu.isEmpty()) {
                result.put(newNextId, menu);
                newNextId++;
            } else if (this.openPage > idIt) {
                this.openPage--;
            }
        }

        this.pages = result;

        if (!this.pages.isEmpty() && this.view != null) {
            HumanEntity player = view.getPlayer();

            //does the player still has the same inventory open?
            if (player.getOpenInventory() == this.view) {
                BasicMenu menu = pages.get(openPage);

                if (this.openPage > 0) {
                    menu.setItem(new RunnableMenuItem(this.plugin, MenuUtils.getPageMaterial(), this.openPage/* start counting from 1 in UI*/, Component.text("<-"), null, () -> {
                        this.open(player, this.openPage - 1);
                    }), getSize() - 9 - 1);
                }

                if (this.openPage < pages.size()) {
                    menu.setItem(new RunnableMenuItem(this.plugin, MenuUtils.getPageMaterial(), this.openPage + 2/* start counting from 1 in UI*/, Component.text("->"), null, () -> {
                        this.open(player, this.openPage + 1);
                    }), getSize() - 1);
                }
            }
        }
    }

    @Override
    public @NotNull BasicMultiPageMenu clone() {
        try {
            BasicMultiPageMenu clone = (BasicMultiPageMenu) super.clone();

            Field rowsField = BasicMultiPageMenu.class.getDeclaredField("rows");
            rowsField.setAccessible(true);
            rowsField.set(clone, rows);

            clone.shouldReturnToParent = this.shouldReturnToParent;
            clone.pages = new HashMap<>(pages.entrySet().stream().collect(Collectors.toMap(Map.Entry::getKey, e -> e.getValue().clone())));
            clone.allowModifyNonMenuItems = allowModifyNonMenuItems;
            clone.title = title;

            return clone;

        } catch (CloneNotSupportedException e) {
            throw new AssertionError();
        } catch (ReflectiveOperationException ex) {
            throw new AssertionError(ex);
        }
    }
}
