package de.greensurvivors.greenui.Translations;

import java.util.regex.Pattern;

/**
 * Default keys.
 * Don't worry you can still register you own, but these are the ones, this lib ships with.
 */
public enum TranslationData {
    MEMUITEM_SAVE,

    MENUITEM_CYCLIC_ERROR_NOMATCH, // "Error, couldn't understand '" + itemName + "'."
    MENUITEM_DECIMAL_ERROR_NOMATCH, //"Error, couldn't understand '" + itemName + "' as a decimal."
    MENUITEM_INT_ERROR_NOMATCH, //"Error, couldn't understand '" + itemName + "' as an int."

    MENUMANAGER_REOPENLATER // "Do your input, the menu will reopen in " + x + "seconds. automatically."

    ;

    // precompiled regex to be faster later
    private final static Pattern KEY_PATTERN = Pattern.compile("_");

    public String getKey() {
        return KEY_PATTERN.matcher(this.name()).replaceAll(".");
    }
}
