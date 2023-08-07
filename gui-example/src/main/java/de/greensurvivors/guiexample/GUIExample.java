package de.greensurvivors.guiexample;

import de.greensurvivors.greenui.Translations.GreenTranslator;
import de.greensurvivors.greenui.menu.MenuManager;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

public class GUIExample extends JavaPlugin {
    private static Plugin greenUIFramework;
    private final @NotNull MenuManager menuManager = new MenuManager(this, new GreenTranslator(this));

    public static Plugin getGreenUInstance() {
        return greenUIFramework;
    }

    @Override
    public void onEnable() {
        greenUIFramework = this;
    }

    @Override
    public void onDisable() {
    }

    public @NotNull MenuManager getMenuManager() {
        return menuManager;
    }
}
