package de.greensurvivors.greenui.menu.helper;

import de.greensurvivors.greenui.menu.ui.Menu;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

/**
 * This event is called whenever a new {@link Menu} is opened.
 * The {@link de.greensurvivors.greenui.menu.MenuManager} listens to this event to keep track witch player has what menu open
 */
public class OpenGreenUIEvent extends Event {
    private final @NotNull HandlerList handlers = new HandlerList();
    private final @NotNull UUID viewer;
    private @NotNull Menu menu;

    public OpenGreenUIEvent(@NotNull UUID viewer, @NotNull Menu menu) {
        this.viewer = viewer;
        this.menu = menu;
    }

    @Override
    public @NotNull HandlerList getHandlers() {
        return handlers;
    }

    public @NotNull UUID getViewer() {
        return viewer;
    }

    public @NotNull Menu getMenu() {
        return menu;
    }

    public void setMenu(@NotNull Menu menu) {
        this.menu = menu;
    }
}
