modifiers 
  <br> -> takes a MenuItem in constructor and uses its calls
- [ ]  rotating through display items --> how do I know when to rotate? It would be a waste if we keep rotating items
  even if not currently viewed...; might also have a callback in case It's used for random
- [X] ~~cooldown - can only get used again after time x (optional per player)~~

items

- [ ] long string --> just chat? If yes, how do we determine the player was done and maybe didn't just forget to write "
  close" or "done" or something?
- [X] ~~getter for block state~~
- [X] ~~get clone() working everywhere~~

menu

- [X] ~~itemList --> can be expanded to white/blacklist by adding a button for white/blacklist and one for filter (
  Material, ItemMeta/blockData)~~
- [ ] ~~display map -> cartography table~~  cool idea, but all I would do is to write a wrapper - maybe if i could load
  images --> have to look into turis code
- [ ] ~~display armor -> smithing table~~ - same
- [ ] ~~display banners --> loom~~ - same
- [X] ~~villager - trades~~
- [ ] filter
- [ ] ~~drag inventory event~~ - backlog since I can't figure out a good way to implement this

general

- [X] ~~getter/setter for all protected~~
- [ ] lore of MenuItems (discretion / value)
- [ ] don't destroy lore just add to it
- [ ] translations and nice formatting
- [ ] more documenting
- [ ] ~~permission checks and disabling of menu items if necessary~~ done by user of this