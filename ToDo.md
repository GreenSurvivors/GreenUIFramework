modifiers 
  <br> -> takes a MenuItem in constructor and uses its calls
- [ ]  rotating through display items --> how do I know when to rotate? It would be a waste if we keep rotating items
  even if not currently viewed...; might also have a callback in case its used for random
- [ ] cooldown - can only get used again after time x (optional per player) --> the manager has to keep track of if...
- [ ] optional --> deletable

items

- [ ] long string --> just chat? If yes, how do we determine the player was done and maybe didn't just forget to write "
  close" or "done" or something?
- [ ] getter for block state - similar problem. Just giving the player a time frame like 20s might be to short, but
  4minutes might be way too long.
- [X] ~~get clone() working everywhere~~

menu

- [X] itemList --> can be expanded to white/blacklist by adding a button for white/blacklist and one for filter (
  Material, ItemMeta/blockData)
- [ ] ~~display map -> cartography table~~  cool idea, but all I would do is to write a wrapper - maybe if i could load
  images --> have to look into turis code
- [ ] ~~display armor -> smithing table~~ - same
- [ ] ~~display banners --> loom~~ - same
- [X] ~~villager - trades~~
- [ ] filter
- [ ] drag inventory event

general

- [X] ~~getter/setter for all protected~~
- [ ] lore of MenuItems (discretion / value)
- [ ] don't destroy lore just add to it
- [ ] translations and nice formatting
- [ ] more documenting
- [ ] permission checks and disabling of menu items if necessary