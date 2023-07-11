package de.greensurvivors.greenui.menu.helper;

import net.kyori.adventure.text.TextComponent;
import org.bukkit.Material;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;

public record ItemStackInfo(@NotNull Material material, int amount, @Nullable TextComponent name) {

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
}
