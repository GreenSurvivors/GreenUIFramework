package de.greensurvivors.greenui.menu.helper;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import org.bukkit.Material;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;

/**
 * packs most common information about an itemStack.
 */
public record ItemStackInfo(@NotNull Material material, int amount, @Nullable TextComponent name) implements Cloneable {

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        if (obj == null || obj.getClass() != this.getClass()) return false;
        var that = (ItemStackInfo) obj;

        return Objects.equals(this.material, that.material) &&
                this.amount == that.amount &&
                (this.name == that.name) ||
                (this.name != null && that.name != null &&
                        this.name.content().equals(that.name.content()) &&
                        this.name.decorations().equals(that.name.decorations())
                );
    }

    /**
     * since you can't change values in a record once it's created,
     * we can't call super.clone() and instead create a new record instance
     */
    @Override
    public @NotNull ItemStackInfo clone() {
        return new ItemStackInfo(this.material, this.amount, name == null ? null : (TextComponent) Component.text(name.content()).decorations(name.decorations()));
    }

}
