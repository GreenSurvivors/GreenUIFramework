package de.greensurvivors.greenui.menu.items;

import de.greensurvivors.greenui.menu.helper.MenuDefaults;
import de.greensurvivors.greenui.menu.ui.AnvilMenu;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.function.Consumer;

/**
 * This MenuItem opens {@link AnvilMenu} in order to get a short string input,
 * therefor it extends MenuMenuItem
 */
public class ShortStringMenuItem extends MenuMenuItem implements Cloneable {
    protected @NotNull Consumer<@NotNull String> stringConsumer;

    public ShortStringMenuItem(@NotNull Plugin plugin, @NotNull Consumer<String> stringConsumer, @NotNull Material displayMat) {
        this(plugin, stringConsumer, null, null, displayMat, 1, true);
    }

    public ShortStringMenuItem(@NotNull Plugin plugin, @NotNull Consumer<String> stringConsumer, @Nullable Component name, @Nullable List<Component> description, @NotNull Material displayMat, int amount, boolean shouldReturnToParent) {
        super(plugin, displayMat, amount, name, description,
                new AnvilMenu(plugin, true, false, null, "",
                        resultItemStack -> {
                            if (resultItemStack != null) {
                                stringConsumer.accept(PlainTextComponentSerializer.plainText().serialize(resultItemStack.displayName()));
                            }
                        }),
                shouldReturnToParent);

        this.stringConsumer = stringConsumer;

        super.menuToOpen.setItem(new BasicMenuItem(this.plugin, Material.PAPER), MenuDefaults.TwoCraftSlots.LEFT.getId());

        //set displayname for save button
        ItemStack saveButton = new ItemStack(MenuDefaults.getSaveMaterial());
        ItemMeta meta = saveButton.getItemMeta();
        meta.displayName(LegacyComponentSerializer.legacyAmpersand().deserialize("")); //todo
        saveButton.setItemMeta(meta);

        super.menuToOpen.setItem(saveButton, MenuDefaults.TwoCraftSlots.RESULT.getId());
    }

    @Override
    public @NotNull ShortStringMenuItem clone() {
        ShortStringMenuItem clone = (ShortStringMenuItem) super.clone();
        clone.stringConsumer = stringConsumer;

        return clone;
    }
}
