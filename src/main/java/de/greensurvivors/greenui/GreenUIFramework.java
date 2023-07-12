package de.greensurvivors.greenui;

import de.greensurvivors.greenui.menu.MenuManager;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;

public class GreenUIFramework extends JavaPlugin {
    private static Plugin greenUIFramework;
    private MenuManager menuManager;

    public static Plugin getGreenUInstance() {
        return greenUIFramework;
    }

    @Override
    public void onEnable() {
        greenUIFramework = this;

        new MenuManager(this);
    }

    @Override
    public void onDisable() {
    }
}
