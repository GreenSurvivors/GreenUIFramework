package de.greensurvivors.greenui.Translations;

import org.intellij.lang.annotations.Subst;
import org.jetbrains.annotations.NotNull;

public enum BuildInPlaceHolders implements PlaceHolder {
    ARGUMENT("argument"),
    PLAYER("player"),
    TEXT("text"),
    TIME("time");

    private final @NotNull String placeholder;

    BuildInPlaceHolders(@NotNull String placeholder) {
        this.placeholder = placeholder;
    }

    @Subst("")
    @Override
    public @NotNull String getPlaceholder() {
        return placeholder;
    }
}
