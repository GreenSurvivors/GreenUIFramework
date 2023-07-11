# GreenUI - Framework

this is a Framework to ease the creation of custom menus.

Please note: this was made a plugin for testing purposes and is likely to get refactored into a library in the future!

Notes

* You can't simply add recipes for the stonecutter without the whole server having access to the new recipe
* Entity container like Horse are also a bit hard, since the inventory gets stored inside the inventory, and we can't
  just create one
* Trade Inventory are getting only created at the time a player opens them and are not cached, every time you open one a
  new inventory gets created
* CommandBlocks get clientside opened, so without placing one in "reachable" distance there is no way to open its gui
* some inventories have no visual representation like the jukebox one but can store items nevertheless