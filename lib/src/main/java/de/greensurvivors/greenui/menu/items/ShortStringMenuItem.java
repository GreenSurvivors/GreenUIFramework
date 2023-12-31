package de.greensurvivors.greenui.menu.items;

import de.greensurvivors.greenui.Translations.TranslationData;
import de.greensurvivors.greenui.menu.MenuManager;
import de.greensurvivors.greenui.menu.helper.MenuUtils;
import de.greensurvivors.greenui.menu.ui.AnvilMenu;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
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

    public ShortStringMenuItem(@NotNull MenuManager manager, @NotNull Consumer<String> stringConsumer, @NotNull Material displayMat) {
        this(manager, stringConsumer, null, null, displayMat, 1, true);
    }

    public ShortStringMenuItem(@NotNull MenuManager manager, @NotNull Consumer<String> stringConsumer, @Nullable Component name, @Nullable List<Component> description, @NotNull Material displayMat, int amount, boolean shouldReturnToParent) {
        super(manager, displayMat, amount, name, description,
                new AnvilMenu(manager, true, false, null, "",
                        resultItemStack -> stringConsumer.accept(PlainTextComponentSerializer.plainText().serialize(resultItemStack.displayName()))),
                shouldReturnToParent);

        this.stringConsumer = stringConsumer;

        super.menuToOpen.setItem(new BasicMenuItem(this.manager, Material.PAPER), MenuUtils.TwoCraftSlots.LEFT.getId());

        //set displayname for save button
        ItemStack saveButton = new ItemStack(MenuUtils.getSaveMaterial());
        ItemMeta meta = saveButton.getItemMeta();
        meta.displayName(this.manager.getTranslator().translateToComponent(TranslationData.MEMUITEM_GENERAL_SAVE.getKey()));
        saveButton.setItemMeta(meta);

        super.menuToOpen.setItem(saveButton, MenuUtils.TwoCraftSlots.RESULT.getId());
    }

    @Override
    public @NotNull ShortStringMenuItem clone() {
        ShortStringMenuItem clone = (ShortStringMenuItem) super.clone();
        clone.stringConsumer = stringConsumer;

        return clone;
    }
}
