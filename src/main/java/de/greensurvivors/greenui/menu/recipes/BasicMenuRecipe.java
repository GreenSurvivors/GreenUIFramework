package de.greensurvivors.greenui.menu.recipes;

import de.greensurvivors.greenui.menu.items.BasicMenuItem;
import org.bukkit.event.inventory.TradeSelectEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.MerchantRecipe;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;

/**
 * A Recipe can hold menu items and will call their {@link de.greensurvivors.greenui.menu.items.BasicMenuItem#onTradeSelect(TradeSelectEvent)} methode when selected.
 */
public class BasicMenuRecipe extends MerchantRecipe {
    protected @NotNull Plugin plugin;

    public BasicMenuRecipe(@NotNull Plugin plugin, @NotNull ItemStack result, int maxUses) {
        super(result, maxUses);

        this.plugin = plugin;
    }

    public BasicMenuRecipe(@NotNull Plugin plugin, @NotNull ItemStack result, int uses, int maxUses, boolean experienceReward) {
        super(result, uses, maxUses, experienceReward);

        this.plugin = plugin;
    }

    public BasicMenuRecipe(@NotNull Plugin plugin, @NotNull ItemStack result, int uses, int maxUses, boolean experienceReward, int villagerExperience, float priceMultiplier) {
        super(result, uses, maxUses, experienceReward, villagerExperience, priceMultiplier);

        this.plugin = plugin;
    }

    public BasicMenuRecipe(@NotNull Plugin plugin, @NotNull ItemStack result, int uses, int maxUses, boolean experienceReward, int villagerExperience, float priceMultiplier, int demand, int specialPrice) {
        super(result, uses, maxUses, experienceReward, villagerExperience, priceMultiplier, demand, specialPrice);

        this.plugin = plugin;
    }

    public BasicMenuRecipe(@NotNull Plugin plugin, @NotNull ItemStack result, int uses, int maxUses, boolean experienceReward, int villagerExperience, float priceMultiplier, boolean ignoreDiscounts) {
        super(result, uses, maxUses, experienceReward, villagerExperience, priceMultiplier, ignoreDiscounts);

        this.plugin = plugin;
    }

    public BasicMenuRecipe(@NotNull Plugin plugin, @NotNull ItemStack result, int uses, int maxUses, boolean experienceReward, int villagerExperience, float priceMultiplier, int demand, int specialPrice, boolean ignoreDiscounts) {
        super(result, uses, maxUses, experienceReward, villagerExperience, priceMultiplier, demand, specialPrice, ignoreDiscounts);

        this.plugin = plugin;
    }

    /**
     * called when this recipe was clicked.
     * As a MenuRecipe we probably want to do something, like open a menu
     * Also calls the {@link BasicMenuItem#onTradeSelect(TradeSelectEvent)} methode for every MenuItem,
     * that is part of this recipe.
     *
     * @param event the click event that was called
     */
    public void onSelect(@NotNull TradeSelectEvent event) {
        if (this.getIngredients().get(0) instanceof BasicMenuItem menuItem) {
            menuItem.onTradeSelect(event);
        }

        if (this.getIngredients().get(1) instanceof BasicMenuItem menuItem) {
            menuItem.onTradeSelect(event);
        }

        if (this.getResult() instanceof BasicMenuItem menuItem) {
            menuItem.onTradeSelect(event);
        }

        //todo select the event manually
        event.setCancelled(true);
    }
}
