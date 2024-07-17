modifiers
<br> -> takes a MenuItem in constructor and uses its calls

- [ ]  rotating through display items --> how do I know when to rotate? It would be a waste if we keep rotating items
  even if not currently viewed...; might also have a callback in case It's used for random
- [X] ~~cooldown - can only get used again after time x (optional per player)~~

items

- [X] long string --> just chat? If yes, how do we determine the player was done and maybe didn't just forget to write "
  close" or "done" or something?
- [X] ~~getter for block state~~
- [X] ~~get clone() working everywhere~~

menu

- [X] ~~itemList --> can be expanded to white/blacklist by adding a button for white/blacklist and one for filter (
  Material, ItemMeta/blockData)~~
- [ ] display map -> cartography table - display custom images --> have to look into turis code
- [ ] ~~display armor -> smithing table~~ - just a wrapper
- [ ] ~~display banners --> loom~~ - same
- [X] ~~villager - trades~~
- [ ] filter
- [ ] ~~drag inventory event~~ - backlog since I can't figure out a good way to implement this
- [ ] some special menus like anvil are broken!

general

- [X] ~~getter/setter for all protected~~
- [ ] lore of MenuItems (discretion / value)
- [ ] don't destroy lore just add to it - see minigames system!
- [X] ~~translations and nice formatting~~
- [ ] more documenting
- [ ] ~~permission checks and disabling of menu items if necessary~~ done by user of this

translations

- [X] ~~rewrite Translator to get ready for localeGroups~~
- [X] ~~locale groups -> if one Locale doesn't have a translation for a key may ask the group~~
- [X] ~~fallback are shipped translations~~
- [ ] ~~rewrite Message pattern to have better Placeholders~~ Use ExtendedMessageFormat
- [ ] ~~Placeholder API optional dependency~~ Problem of the user of this framework
- [ ] Make user dependent messages optional
- [ ] Make invalidating all translations possible (to make translation reloading possible)
- [ ] event based api?