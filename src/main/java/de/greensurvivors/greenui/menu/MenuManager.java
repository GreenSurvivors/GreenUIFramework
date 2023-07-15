package de.greensurvivors.greenui.menu;

import de.greensurvivors.greenui.Translations.GreenTranslator;
import de.greensurvivors.greenui.Translations.TranslationData;
import de.greensurvivors.greenui.menu.helper.OpenGreenUIEvent;
import de.greensurvivors.greenui.menu.ui.Menu;
import de.greensurvivors.greenui.menu.ui.TradeMenu;
import io.papermc.paper.event.player.AsyncChatEvent;
import net.kyori.adventure.text.minimessage.MiniMessage;
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
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitTask;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Stack;
import java.util.UUID;
import java.util.logging.Level;

/**
 * keeps track of all open GreenUIs and keeps them working by providing event calls
 */
public class MenuManager implements Listener {
    private final static long REOPEN_TICKS = 20 /* ticks*/ * 60  /* seconds*/;

    private final @NotNull Plugin plugin;
    private final @NotNull GreenTranslator translator;
    // Stores all currently open inventories by all players, using a stack system we can easily add or remove child inventories.
    private final @NotNull HashMap<UUID, Stack<Menu>> activeMenus = new HashMap<>();
    private final @NotNull HashMap<Menu, BukkitTask> reopeningMenus = new HashMap<>();

    public MenuManager(@NotNull Plugin plugin, @NotNull GreenTranslator translator) {
        this.plugin = plugin;
        this.translator = translator;

        Bukkit.getPluginManager().registerEvents(this, plugin);
    }

    public @NotNull Plugin getPlugin() {
        return this.plugin;
    }

    public GreenTranslator getTranslator() {
        return this.translator;
    }

    @EventHandler(ignoreCancelled = true)
    private void onOpenMenu(final OpenGreenUIEvent event) {
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
    private void onTradeSelect(final TradeSelectEvent event) {
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
            this.plugin.getLogger().log(Level.WARNING, "Player " + event.getWhoClicked().getName() + " (" + playerId.toString() + ") is out of sync with the MenuManager. Please contact an Admin!");
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
    private void onDragMenu(final InventoryDragEvent event) {
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

        if (menu != null && event.getRawSlots().stream().anyMatch(slotId -> slotId < menu.getSize()) && !menu.allowModifyNonMenuItems()) {
            //todo
            event.setCancelled(true);
        }
    }

    @EventHandler(ignoreCancelled = true)
    private void onCloseMenu(final InventoryCloseEvent event) {
        UUID playerId = event.getPlayer().getUniqueId();
        if (!activeMenus.containsKey(playerId))
            return;

        Menu menu = activeMenus.get(playerId).peek();

        final Inventory inv = event.getInventory();
        final UUID uuid = event.getPlayer().getUniqueId();

        switch (menu.onClose()) {
            case CLOSE -> activeMenus.get(uuid).pop(); //successfully closed
            // the menu wants to stay open
            // wait for the event to pass to reopen the inventory
            case STAY_OPEN -> Bukkit.getScheduler().runTaskLater(plugin, () -> {
                Player player = Bukkit.getPlayer(uuid);

                if (player != null) {
                    player.openInventory(inv);
                }
            }, 0);
            case REOPEN_AFTER_TIME -> {
                BukkitTask task = Bukkit.getScheduler().runTaskLater(this.plugin, () -> menu.open(event.getPlayer()), REOPEN_TICKS);
                reopeningMenus.put(menu, task);
                event.getPlayer().sendMessage(MiniMessage.miniMessage().deserialize(this.translator.simpleTranslate(TranslationData.MENUMANAGER_REOPENLATER.getKey()).format(new long[]{REOPEN_TICKS / 20L})));
            }
            case REOPEN_EVENT -> {
                //todo implement me for long strings
            }
            default -> {
                plugin.getLogger().log(Level.WARNING, "Unknown close result. How did we get here? Removing Menu from Stack anyway");
                activeMenus.get(uuid).pop();
            }
        }
    }

    @EventHandler(ignoreCancelled = true)
    private void onBlockInteract(final PlayerInteractEvent event) {
        UUID playerId = event.getPlayer().getUniqueId();
        if (!activeMenus.containsKey(playerId) || !event.hasBlock()) {
            return;
        }

        Menu menu = activeMenus.get(playerId).peek();

        if (menu != null) {
            BukkitTask task = reopeningMenus.get(menu);

            if (task != null) {
                if (event.getClickedBlock() != null && menu.onBlockInteract(event.getClickedBlock())) {
                    // don't interact with anything, just observe!
                    event.setCancelled(true);

                    // reopen and cancel task that would do that for us
                    menu.open(event.getPlayer());
                    task.cancel();
                }
            } else {
                this.plugin.getLogger().log(Level.WARNING, "Player " + event.getPlayer().getName() + " (" + playerId.toString() + ") is out of sync with the MenuManager. Please contact an Admin!");
            }
        }
    }

    @EventHandler
    private void onChat(final AsyncChatEvent event) {
        UUID playerId = event.getPlayer().getUniqueId();
        if (!activeMenus.containsKey(playerId)) {
            return;
        }

        Menu menu = activeMenus.get(playerId).peek();

        if (menu != null) {
            BukkitTask task = reopeningMenus.get(menu);

            if (task != null) {
                if (menu.onChat(event.message())) {
                    // don't interact with anything, just observe!
                    event.setCancelled(true);

                    // reopen and cancel task that would do that for us
                    menu.open(event.getPlayer());
                    task.cancel();
                }
            } else {
                this.plugin.getLogger().log(Level.WARNING, "Player " + event.getPlayer().getName() + " (" + playerId.toString() + ") is out of sync with the MenuManager. Please contact an Admin!");
            }
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
