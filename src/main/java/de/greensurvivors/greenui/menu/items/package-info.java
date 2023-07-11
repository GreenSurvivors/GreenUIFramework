/**
 * Items that you can add into {@link de.greensurvivors.greenui.menu.ui.Menu} in order to build your menu.
 * Most of the items have some kind of feedback mechanism, like a runnable or a consumer to receive a user input.
 * These are called in a separate Task to ensure safe and sync access.
 * <p>
 * All MenuItems must expand {@link de.greensurvivors.greenui.menu.items.BasicMenuItem} in some kind or form in order
 * to be recognized by the menus as special items.
 */
package de.greensurvivors.greenui.menu.items;