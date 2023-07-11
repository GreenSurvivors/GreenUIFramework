modifiers 
  <br> -> takes a MenuItem in constructor and uses its calls
*  rotating through display items --> how do I know when to rotate? It would be a waste if we keep rotating items even if not currently viewed...; might also have a callback in case its used for random
* cooldown - can only get used again after time x (optional per player) --> the manager has to keep track of if...
* optional --> deletable

items
* long string
* getter for block state

menu
* itemList --> can be expanded to white/blacklist by adding a button for white/blacklist and one for filter (Material, ItemMeta/blockData)
* display map
* villager - trades?
* filter
* drag inventory event

general

* lore of MenuItems (discretion / value)
* getter/setter for all protected
* don't destroy lore just add to it
* translations and nice formatting
* more documenting
* permissions