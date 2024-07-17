package de.greensurvivors.greenui.Translations;

import org.jetbrains.annotations.NotNull;

/**
 * Default keys.
 * Don't worry you can still register you own, but these are the ones, this lib ships with.
 */
public enum BuildInLangPaths implements LangPath {
    MEMUITEM_GENERAL_SAVE("menuItem.general.save", "<b><color:#00a113>Save</color></b>"), // save.

    MENUITEM_CYCLIC_ERROR_NOMATCH("menuItem.cyclic.error.noMatch", "<b><dark_red>Error:</dark_red></b> <dark_gray>Couldn`t understand \"</dark_gray><gold><b><text></b></gold><dark_gray>\".</dark_gray>"), // "Error, couldn't understand '" + itemName + "'."
    MENUITEM_DECIMAL_ERROR_NOMATCH("menuItem.decimal.error.noMatch", "<b><dark_red>Error:</dark_red></b> <dark_gray>Couldn`t understand \"</dark_gray><gold><b><text></b></gold><dark_gray>\" as a decimal.</dark_gray>"), //"Error, couldn't understand '" + itemName + "' as a decimal."
    MENUITEM_INT_ERROR_NOMATCH("menuItem.int.error.noMatch", "<b><dark_red>Error:</dark_red></b> <dark_gray>Couldn`t understand \"</dark_gray><gold><b><text></b></gold><dark_gray>\" as a int.</dark_gray>"), //"Error, couldn't understand '" + itemName + "' as an int."

    MENUMANAGER_REOPENLATER("menuManager.reopenLater", "<dark_green>Enter your input, the menu will automatically reopen in </dark_green><yellow>\"</yellow><gold><time></gold><yellow>\"</yellow><gold>s</gold><dark_green> if nothing is entered.</dark_green>"), // "Do your input, the menu will reopen in " + x + "seconds. automatically."

    PLUGIN_PREFIX("plugin.prefix");

    private final @NotNull String path;
    private final @NotNull String defaultValue;

    BuildInLangPaths(@NotNull String path) {
        this.path = path;
        this.defaultValue = path;
    }

    BuildInLangPaths(@NotNull String path, @NotNull String defaultValue) {
        this.path = path;
        this.defaultValue = defaultValue;
    }

    @Override
    public @NotNull String getPath() {
        return path;
    }

    @Override
    public @NotNull String getDefaultValue() {
        return defaultValue;
    }
}
