package de.greensurvivors.greenui.Translations;

import org.jetbrains.annotations.NotNull;

/**
 * placeholder strings used. will be surrounded in Minimassage typical format of <>
 */
public interface PlaceHolder {
    /**
     * Since this will be used in Mini-messages placeholder only the pattern "[!?#]?[a-z0-9_-]*" is valid.
     * if used inside an unparsed text you have to add surrounding <> yourself.
     */
    @NotNull
    String getPlaceholder();
}
