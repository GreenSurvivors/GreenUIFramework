package de.greensurvivors.greenui.menu;

import de.greensurvivors.greenui.menu.helper.OpenGreenUIEvent;
import de.greensurvivors.greenui.menu.ui.Menu;
import de.greensurvivors.greenui.menu.ui.TradeMenu;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.event.inventory.TradeSelectEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.plugin.Plugin;

import java.util.HashMap;
import java.util.Stack;
import java.util.UUID;

/**
 * keeps track of all open GreenUIs and keeps them working by providing event calls
 */
public class MenuManager implements Listener {
    private final Plugin plugin;
    // Stores all currently open inventories by all players, using a stack system we can easily add or remove child inventories.
    private final HashMap<UUID, Stack<Menu>> activeMenus = new HashMap<>();

    public MenuManager(Plugin plugin) {
        this.plugin = plugin;

        Bukkit.getPluginManager().registerEvents(this, plugin);
    }

    @EventHandler
    private void onOpenMenu(OpenGreenUIEvent event) {
        activeMenus.computeIfAbsent(event.getViewer(), k -> new Stack<>());
        Stack<Menu> menuStack = activeMenus.get(event.getViewer());

        // If we add another menu on top of a menu that should be removed, remove this menu first.
        if (menuStack.size() > 0 && !menuStack.peek().shouldReturnedTo()) {
            menuStack.pop().onClose();
        }

        // add to stack
        menuStack.push(event.getMenu());
    }

    @EventHandler(ignoreCancelled = true)
    private void onTradeSelect(TradeSelectEvent event) {
        UUID playerId = event.getWhoClicked().getUniqueId();
        if (!activeMenus.containsKey(playerId)) {
            return;
        }

        Menu menu = activeMenus.get(playerId).peek();

        if (menu instanceof TradeMenu tradeMenu) {
            tradeMenu.onTradeSelect(event);

            if (event.getWhoClicked() instanceof Player player) {
                // I have no idea why InventoryClickEvent encourages to call this while the method itself doesn't,
                // however calling this should do no harm and in most cases we want changed the inventory anyway
                //todo test if necessary
                player.updateInventory();
            }
        } else {
            // todo -> mayday! we are out of sync!
        }
    }

    @EventHandler(ignoreCancelled = true)
    private void onClickMenu(final InventoryClickEvent event) {
        UUID playerId = event.getWhoClicked().getUniqueId();
        if (!activeMenus.containsKey(playerId)) {
            return;
        }

        Menu menu = activeMenus.get(playerId).peek();

        if (menu != null && event.getRawSlot() < menu.getSize()) {
            menu.onInventoryClick(event);

            if (event.getWhoClicked() instanceof Player player) {
                // I have no idea why InventoryClickEvent encourages to call this while the method itself doesn't,
                // however calling this should do no harm and in most cases we want changed the inventory anyway
                //todo test if necessary
                player.updateInventory();
            }
        }
    }

    @EventHandler(ignoreCancelled = true)
    private void onDragMenu(final InventoryDragEvent event) { //todo
        UUID playerId = event.getWhoClicked().getUniqueId();
        if (!activeMenus.containsKey(playerId))
            return;

        Menu menu = activeMenus.get(playerId).peek();

        // I'm not dealing with something that may output a new Material for every slot
        // todo
        if (event.getOldCursor().getType() == Material.BUNDLE) {
            event.setCancelled(true);
            return;
        }

        //temp until the rest is working
        event.setCancelled(true);

        /*
        //contains the getResultItem as if one had put every item one by one into the menu, key is raw
        HashMap<Integer, InventoryClickEvent> resultHashMap = new HashMap<>(event.getRawSlots().size());
        //contains how many items went into this raw slot
        HashMap<Integer, Integer> changePerSlot = new HashMap<>(event.getRawSlots().size());

        ItemStack newCursor = event.getCursor();

        for (int slot : event.getRawSlots()){
            if (slot < menu.getSize()){
                ItemStack oldItem = event.getView().getItem(slot);
                ItemStack changingItem = event.getNewItems().get(slot).clone();

                if (oldItem != null && oldItem.getType() == changingItem.getType()){
                    changingItem = changingItem.add(-oldItem.getAmount());
                }

                changePerSlot.put(slot, changingItem.getAmount());

                InventoryClickEvent newEvent = new InventoryClickEvent(event.getView(), event.getView().getSlotType(slot), slot, ClickType.LEFT, event.getType() == DragType.SINGLE ? InventoryAction.PLACE_ONE : InventoryAction.PLACE_SOME);
                menu.onInventoryClick(newEvent);
                resultHashMap.put(slot, newEvent);
            }
        }

        boolean cancled = true;
        for (Map.Entry<Integer, InventoryClickEvent> entry : resultHashMap.entrySet()){
            if (entry.getValue().isCancelled()){ //don't get fooled this is another event
                changePerSlot.get(entry.getKey());

                if (newCursor == null){
                    newCursor = event.getNewItems().get(entry.getKey()).clone();
                    newCursor.setAmount(0);
                }

                newCursor.add(changePerSlot.get(entry.getKey()));
                event.getRawSlots().remove(entry.getKey());
            } else {
                cancled = false;
            }

            if (entry.getValue().getCurrentItem() != null) {
                event.getNewItems().put(entry.getKey(), entry.getValue().getCurrentItem());
            }
        }

        event.setCursor(newCursor);
        if (cancled){
            event.setCancelled(true);
        }

        if (event.getWhoClicked() instanceof Player player){
            // I have no idea why InventoryDragEvent encourages to call this while the method itself doesn't,
            // however calling this should do no harm
            player.updateInventory();
        }*/
    }

    @EventHandler(ignoreCancelled = true)
    private void onCloseMenu(final InventoryCloseEvent event) {
        UUID playerId = event.getPlayer().getUniqueId();
        if (!activeMenus.containsKey(playerId))
            return;

        Menu menu = activeMenus.get(playerId).peek();

        final Inventory inv = event.getInventory();
        final UUID uuid = event.getPlayer().getUniqueId();

        if (menu.onClose()) {
            // the menu wants to stay open
            // wait for the event to pass to reopen the inventory
            Bukkit.getScheduler().runTaskLater(plugin, () -> {
                Player player = Bukkit.getPlayer(uuid);

                if (player != null) {
                    player.openInventory(inv);
                }

            }, 0);
        } else {
            //successfully closed
            activeMenus.get(uuid).pop();
        }
    }

    /**
     * closes all open menus of a player if they quit
     *
     * @param event
     */
    @EventHandler
    private void onPlayerQuit(final PlayerQuitEvent event) {
        if (activeMenus.containsKey(event.getPlayer().getUniqueId())) {
            closeAll(event.getPlayer());
        }
    }

    /**
     * forces to close all open menus of a player
     *
     * @param player
     */
    public void closeAll(HumanEntity player) {
        UUID playerId = player.getUniqueId();
        if (!activeMenus.containsKey(playerId))
            return;

        Stack<Menu> menus = activeMenus.get(playerId);
        while (activeMenus.get(playerId).size() > 0) {
            menus.pop().onClose();
        }
        activeMenus.remove(playerId);
        player.closeInventory();
    }
}
