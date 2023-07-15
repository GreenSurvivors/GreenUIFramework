# GreenUI - Framework

this is a Framework to ease the creation of custom menus.<br>
The focus of this project doesn't ley in its big variety of different UIs -
you can easily adapt almost every inventory into the System - but to get easy user feedback.
All you need to do to get a String input is adding a single item into your menu!

---
Please note: this was made a plugin for testing purposes and
is likely to get refactored into a library in the future!
---

## Get Started

In order to use this you have to create a new Instance of ManuManager and store it somewhere.<br>
In Order to open a menu you have to create a new Menu and fill it with items and foremost MenuItems as you wish.<br>
After you are done, you can open it via the menus `.open()` methode. <br>
Every menu you open yourself also needs to get registered in the ManuManager by calling an `OpenGreenUIEvent`.
Every time a MenuItem opens a Menu for you, it will call the event itself.
<p>
Most of the work done by this project doesn't come from the menus but from the Items within, the <b>MenuItems</b>.
They have construction parameters like runnable and consumers to provide you callback every time the user changes the outcome of your setting.
<p>
You can add to the behavior of a MenuItem via a <b>Modifier</b> like disabling it. 

---
Notes

* You can't simply add recipes for the stonecutter without the whole server having access to the new recipe
* Entity container like Horse are also a bit hard, since the inventory gets stored inside the entity, and we can't
  just create one without also creating the entity as well
* Trade Inventory are getting only created at the time a player opens them and are not cached, every time you open one a
  new inventory gets created
* CommandBlocks get clientside opened, so without placing one in "reachable" distance there is no way to open its gui
* some inventories have no visual representation like the jukebox one but can store items nevertheless
* You can't set the selected Recipe of a merchant without mns.