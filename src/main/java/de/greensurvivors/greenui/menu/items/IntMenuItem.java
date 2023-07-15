package de.greensurvivors.greenui.menu.items;

import de.greensurvivors.greenui.Translations.TranslationData;
import de.greensurvivors.greenui.Translations.Translator;
import de.greensurvivors.greenui.menu.helper.MenuUtils;
import de.greensurvivors.greenui.menu.helper.OpenGreenUIEvent;
import de.greensurvivors.greenui.menu.ui.AnvilMenu;
import de.greensurvivors.greenui.menu.ui.Menu;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.HumanEntity;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

/**
 * MenuItem to receive a decimal value from the user.
 * The user can click to in/decrease the value, with two step-sizes.
 */
public class IntMenuItem extends BasicMenuItem implements Cloneable {
    // optional upper / lower bounds
    protected @Nullable Integer min, max;
    // called whenever the value changes
    protected @NotNull Consumer<@NotNull Integer> intConsumer;
    // the state this menuItem is in
    protected int value;

    protected @NotNull Menu menuToOpen;
    protected @Nullable HumanEntity viewer;

    public IntMenuItem(@NotNull Plugin plugin, @NotNull Translator translator, @NotNull Material displayMat, @NotNull Consumer<Integer> consumer) {
        this(plugin, translator, displayMat, 1, null, consumer, 0, null, null);
    }

    public IntMenuItem(@NotNull Plugin plugin, @NotNull Translator translator, @NotNull Material displayMat, int amount, @Nullable Component name, @NotNull Consumer<Integer> consumer, int startingValue, @Nullable Integer min, @Nullable Integer max) {
        super(plugin, translator, displayMat, amount, name, null);

        this.intConsumer = consumer;
        this.value = startingValue;
        this.min = min;
        this.max = max;

        //stay in bounds
        if (min != null) {
            value = Math.max(min, value);
        }
        if (max != null) {
            value = Math.min(max, value);
        }

        this.menuToOpen = new AnvilMenu(plugin, this.translator, true, false, null, String.valueOf(this.value), this::acceptStringItem);

        //set displayname for save button
        ItemStack saveButton = new ItemStack(MenuUtils.getSaveMaterial());
        ItemMeta meta = saveButton.getItemMeta();
        meta.displayName(this.translator.translateToComponent(TranslationData.MEMUITEM_SAVE.getKey()));
        saveButton.setItemMeta(meta);

        //update result
        updateLore();
        consumer.accept(value);
    }

    /**
     * called when this Item was clicked.
     * in/decreases the integer value
     *
     * @param event the click event that was called
     */
    @Override
    public void onClick(@NotNull InventoryClickEvent event) {
        super.onClick(event);

        switch (event.getClick()) {
            case LEFT -> value++;
            case SHIFT_LEFT -> value += 10;
            case RIGHT -> value--;
            case SHIFT_RIGHT -> value -= 10;
            case DOUBLE_CLICK -> {// get input string via anvil
                Bukkit.getScheduler().runTask(
                        this.plugin, () -> {
                            (new OpenGreenUIEvent(event.getWhoClicked().getUniqueId(), menuToOpen)).callEvent();

                            viewer = event.getWhoClicked();
                            menuToOpen.open(event.getWhoClicked());
                        }
                );

                // skip value test
                return;
            }
        }

        //stay in bounds
        if (min != null) {
            value = Math.max(min, value);
        }
        if (max != null) {
            value = Math.min(max, value);
        }

        //update this
        updateLore();
        Bukkit.getScheduler().runTask(this.plugin, () -> this.intConsumer.accept(value));
    }

    /**
     * updates the lore to reflect changed Values
     */
    public void updateLore() {
        List<Component> newLore = new ArrayList<>();
        Component loreLine = Component.empty();

        if (min != null) {
            loreLine = loreLine.append(Component.text(min).append(Component.text(" < ")).color(NamedTextColor.GREEN));
        }

        loreLine = loreLine.append(Component.text(value).color(NamedTextColor.YELLOW));

        if (max != null) {
            loreLine = loreLine.append(Component.text(" < ").append(Component.text(max)).color(NamedTextColor.GREEN));
        }

        newLore.add(loreLine);
        this.lore(newLore);
    }

    /**
     * takes the displayname of an ItemStack and tries to parse an int value from it
     */
    protected void acceptStringItem(@NotNull ItemStack stringResult) {
        String itemName = PlainTextComponentSerializer.plainText().serialize(stringResult.displayName());

        Bukkit.getScheduler().runTask(this.plugin, () -> {
            if (MenuUtils.isInt(itemName)) {
                this.value = Integer.parseInt(itemName);
                this.intConsumer.accept(this.value);
            } else {

                if (this.viewer != null) {
                    this.viewer.sendMessage(MiniMessage.miniMessage().deserialize(this.translator.simpleTranslate(TranslationData.MENUITEM_INT_ERROR_NOMATCH.getKey()).format(new String[]{itemName})));
                }
            }
        });
    }

    /**
     * get the current value
     */
    public int getValue() {
        return value;
    }

    /**
     * set the current value
     * expects to be called in sync
     */
    public void setValue(int newValue) {
        this.value = newValue;

        //stay in bounds
        if (min != null) {
            value = Math.max(min, value);
        }
        if (max != null) {
            value = Math.min(max, value);
        }

        //update result
        updateLore();
        intConsumer.accept(value);
    }

    @Override
    public @NotNull IntMenuItem clone() {
        IntMenuItem clone = (IntMenuItem) super.clone();
        clone.min = this.min;
        clone.max = this.max;
        clone.value = this.value;
        clone.intConsumer = this.intConsumer;
        return clone;
    }
}
