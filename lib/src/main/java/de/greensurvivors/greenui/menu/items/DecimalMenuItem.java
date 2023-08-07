package de.greensurvivors.greenui.menu.items;

import de.greensurvivors.greenui.Translations.TranslationData;
import de.greensurvivors.greenui.menu.MenuManager;
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
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

/**
 * MenuItem to receive a decimal value from the user.
 * The user can click to in/decrease the value, with two configurable step-sizes.
 */
public class DecimalMenuItem extends BasicMenuItem implements Cloneable {
    protected @NotNull DecimalFormat form = new DecimalFormat("#.##");
    // optional upper / lower bounds
    protected @Nullable Double min, max;
    // big steps
    protected int intStepSize;
    // finer steps
    protected double fractionalStepSize;
    // called whenever the value changes
    protected @NotNull Consumer<@NotNull Double> decimalConsumer;
    // the state this menuItem is in
    protected double value;

    protected @NotNull Menu menuToOpen;
    protected @Nullable HumanEntity viewer;

    public DecimalMenuItem(@NotNull MenuManager manager, @NotNull Material displayMat, @NotNull Consumer<Double> decimalConsumer) {
        this(manager, displayMat, 1, null, decimalConsumer, 0, null, null, 1, 0.1);
    }

    public DecimalMenuItem(@NotNull MenuManager manager, @NotNull Material displayMat, int amount, @Nullable Component name, @NotNull Consumer<Double> decimalConsumer,
                           double startingValue, @Nullable Double min, @Nullable Double max, int intStepSize, double fractionalStepSize) {
        super(manager, displayMat, amount, name, null);

        this.decimalConsumer = decimalConsumer;
        this.value = startingValue;
        this.min = min;
        this.max = max;
        this.intStepSize = intStepSize;
        this.fractionalStepSize = fractionalStepSize;

        //stay in bounds
        if (min != null) {
            value = Math.max(min, value);
        }
        if (max != null) {
            value = Math.min(max, value);
        }

        this.menuToOpen = new AnvilMenu(manager, true, false, null, this.form.format(this.value), this::acceptStringItem);

        //set displayname for save button
        ItemStack saveButton = new ItemStack(MenuUtils.getSaveMaterial());
        ItemMeta meta = saveButton.getItemMeta();
        meta.displayName(this.manager.getTranslator().translateToComponent(TranslationData.MEMUITEM_GENERAL_SAVE.getKey()));
        saveButton.setItemMeta(meta);

        this.menuToOpen.setItem(saveButton, MenuUtils.TwoCraftSlots.RESULT.getId());

        //update result
        updateLore();
        decimalConsumer.accept(value);
    }

    /**
     * updates the lore to reflect changed Values
     */
    public void updateLore() {
        List<Component> newLore = new ArrayList<>();
        Component loreLine = Component.empty();

        if (this.min != null) {
            loreLine = loreLine.append(Component.text(this.min).append(Component.text(" < ")).color(NamedTextColor.GREEN));
        }

        loreLine = loreLine.append(Component.text(this.form.format(this.value)).color(NamedTextColor.YELLOW));

        if (this.max != null) {
            loreLine = loreLine.append(Component.text(" < ").append(Component.text(this.max)).color(NamedTextColor.GREEN));
        }

        newLore.add(loreLine);
        this.lore(newLore);
    }

    /**
     * called when this Item was clicked.
     * In/decreases the decimal value
     *
     * @param event the click event that was called
     */
    @Override
    public void onClick(@NotNull InventoryClickEvent event) {
        super.onClick(event);

        switch (event.getClick()) {
            case LEFT -> value += intStepSize;
            case SHIFT_LEFT -> value += fractionalStepSize;
            case RIGHT -> value -= intStepSize;
            case SHIFT_RIGHT -> value -= fractionalStepSize;
            case DOUBLE_CLICK -> {// get input string via anvil
                Bukkit.getScheduler().runTask(
                        this.manager.getPlugin(), () -> {
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
        Bukkit.getScheduler().runTask(this.manager.getPlugin(), () -> this.decimalConsumer.accept(value));
    }

    /**
     * get the current value
     */
    public double getValue() {
        return value;
    }

    /**
     * set the current value, expects to be called in sync
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
        decimalConsumer.accept(value);
    }

    /**
     * takes the displayname of an ItemStack and tries to parse a decimal value from it
     */
    protected void acceptStringItem(@NotNull ItemStack stringResult) {
        String itemName = PlainTextComponentSerializer.plainText().serialize(stringResult.displayName());

        Bukkit.getScheduler().runTask(this.manager.getPlugin(), () -> {
            if (MenuUtils.isDouble(itemName)) {
                this.value = Double.parseDouble(itemName);
                this.decimalConsumer.accept(this.value);
            } else {

                if (this.viewer != null) {
                    this.viewer.sendMessage(MiniMessage.miniMessage().deserialize(this.manager.getTranslator().simpleTranslate(TranslationData.MENUITEM_DECIMAL_ERROR_NOMATCH.getKey()).format(new String[]{itemName})));
                }
            }
        });
    }

    @Override
    public @NotNull DecimalMenuItem clone() {
        DecimalMenuItem clone = (DecimalMenuItem) super.clone();
        clone.decimalConsumer = this.decimalConsumer;
        clone.min = this.min;
        clone.max = this.max;
        clone.value = this.value;
        clone.intStepSize = this.intStepSize;
        clone.fractionalStepSize = this.fractionalStepSize;
        clone.form = (DecimalFormat) this.form.clone();
        return clone;
    }
}
