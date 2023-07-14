package de.greensurvivors.greenui.menu.helper;

import net.kyori.adventure.text.Component;
import org.bukkit.block.Block;
import org.jetbrains.annotations.NotNull;

public interface DirectIntractable {
    /**
     * Event-handler when the menu is closed right now and awaits player input
     * The input given by this event is provided by clicking a block.
     *
     * @param block the interacted block
     * @return true, if the menu accepts the clicked block as input
     * Note: This doesn't necessarily mean the input is valid,
     * but it is feedback for {@link de.greensurvivors.greenui.menu.MenuManager}
     * to reopen this menu.
     */
    boolean onBlockInteract(@NotNull Block block);

    /**
     * Event-handler when the menu is closed right now and awaits player input
     * The input given by this event is provided by writing a message into chat.
     *
     * @param message the chat message
     * @return true, if the menu accepts the message as input
     * Note: This doesn't necessarily mean the input is valid,
     * but it is feedback for {@link de.greensurvivors.greenui.menu.MenuManager}
     * to reopen this menu.
     */
    boolean onChat(@NotNull Component message);

    /**
     * This tells the intractable to stop waiting for direct input
     */
    void cancel();
}
