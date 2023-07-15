package de.greensurvivors.greenui.menu.recipes;

import de.greensurvivors.greenui.menu.MenuManager;
import de.greensurvivors.greenui.menu.items.BasicMenuItem;
import org.bukkit.Bukkit;
import org.bukkit.event.inventory.TradeSelectEvent;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

public class RunnableMenuRecipe extends BasicMenuRecipe {
    // called whenever the state changes
    protected @NotNull Runnable runnable;

    public RunnableMenuRecipe(@NotNull MenuManager manager, @NotNull Runnable runnable, @NotNull ItemStack result, int maxUses) {
        super(manager, result, maxUses);

        this.runnable = runnable;
    }

    public RunnableMenuRecipe(@NotNull MenuManager manager, @NotNull Runnable runnable, @NotNull ItemStack result, int uses, int maxUses, boolean experienceReward) {
        super(manager, result, uses, maxUses, experienceReward);

        this.runnable = runnable;
    }

    public RunnableMenuRecipe(@NotNull MenuManager manager, @NotNull Runnable runnable, @NotNull ItemStack result, int uses, int maxUses, boolean experienceReward, int villagerExperience, float priceMultiplier) {
        super(manager, result, uses, maxUses, experienceReward, villagerExperience, priceMultiplier);

        this.runnable = runnable;
    }

    public RunnableMenuRecipe(@NotNull MenuManager manager, @NotNull Runnable runnable, @NotNull ItemStack result, int uses, int maxUses, boolean experienceReward, int villagerExperience, float priceMultiplier, int demand, int specialPrice) {
        super(manager, result, uses, maxUses, experienceReward, villagerExperience, priceMultiplier, demand, specialPrice);

        this.runnable = runnable;
    }

    public RunnableMenuRecipe(@NotNull MenuManager manager, @NotNull Runnable runnable, @NotNull ItemStack result, int uses, int maxUses, boolean experienceReward, int villagerExperience, float priceMultiplier, boolean ignoreDiscounts) {
        super(manager, result, uses, maxUses, experienceReward, villagerExperience, priceMultiplier, ignoreDiscounts);

        this.runnable = runnable;
    }

    public RunnableMenuRecipe(@NotNull MenuManager manager, @NotNull Runnable runnable, @NotNull ItemStack result, int uses, int maxUses, boolean experienceReward, int villagerExperience, float priceMultiplier, int demand, int specialPrice, boolean ignoreDiscounts) {
        super(manager, result, uses, maxUses, experienceReward, villagerExperience, priceMultiplier, demand, specialPrice, ignoreDiscounts);

        this.runnable = runnable;
    }


    /**
     * called when this recipe was clicked.
     * As a RunnableMenuRecipe we are calling our runnable
     * Also calls the {@link BasicMenuItem#onTradeSelect(TradeSelectEvent)} methode for every MenuItem,
     * that is part of this recipe.
     *
     * @param event the click event that was called
     */
    public void onSelect(@NotNull TradeSelectEvent event) {
        super.onSelect(event);

        Bukkit.getScheduler().runTask(this.manager.getPlugin(), () -> runnable.run());
    }
}
