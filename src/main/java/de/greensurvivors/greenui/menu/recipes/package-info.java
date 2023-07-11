/**
 * Merchant recipes (for now) that you can add into {@link de.greensurvivors.greenui.menu.ui.TradeMenu} in order to build your menu.
 * A recipe is something between a {@link de.greensurvivors.greenui.menu.ui.Menu} and a {@link de.greensurvivors.greenui.menu.items.BasicMenuItem},
 * since it may be clicked and do something itself, but also contains MenuItems.
 * Most of the recipes have some kind of feedback mechanism, like a runnable or a consumer to receive a user input.
 * These are called in a separate Task to ensure safe and sync access.
 * <p>
 * All MenuRecipes must expand {@link de.greensurvivors.greenui.menu.recipes.BasicMenuRecipe} in some kind or form in order
 * to be recognized by the menu as special recipes.
 */
package de.greensurvivors.greenui.menu.recipes;