package de.greensurvivors.guiexample;

import de.greensurvivors.greenui.Translations.GreenTranslator;
import de.greensurvivors.greenui.menu.MenuManager;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
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

        getCommand("testmenu").setExecutor(new Commands(this));


    }

    @Override
    public void onDisable() {
        // just close all menus gracefully, nothing should break in the raw famework if you don't do this,
        // but it might be best practice anyway
        for (Player player : Bukkit.getOnlinePlayers()){
            menuManager.closeAll(player);
        }
    }

    public @NotNull MenuManager getMenuManager() {
        return menuManager;
    }
}
