# In Range

Hover over attackable NPCs to tell whether you are in attack range.

## Features
- Customizable to be enabled only in Inferno, Colosseum, Fight Caves, Jad Challenges, etc.
- Updates as Player and/or NPC moves.
- Updates range when weapon changes e.g. Blowpipe -> Toxic Trident.
- Updates when attack style changes e.g. Rapid -> Longrange or Staff Bash -> Autocast.

## Modes
* In attack range and have line of sight.
* In attack range but no line of sight.
* Out of attack range.

## Settings
### 1. Highlight Color [Choose a Color]
   - (Default GREEN) In range and LOS.
   - (Default ORANGE) In range and no LOS.
   - (Default RED) Not in range.
### 2. Area Select [Checkbox]
(All checked by Default) Select which areas you'd like the plugin to be enabled.
  - The Inferno
  - Fortis Colosseum
  - TzHaar Fight Cave (Jad)
  - TzHaar-Ket-Rak's Challenges (Jad Challenges)

## Acknowledgements
* OSRS DPS Calculator for the script to pull all the weapon ranges from the Wiki (https://github.com/weirdgloop/osrs-dps-calc)
* RuneLite Attack Styles plugin for code on how to get different weapon types and attack styles (https://github.com/runelite/runelite/tree/master/runelite-client/src/main/java/net/runelite/client/plugins/attackstyles)
* RuneLite Interact Highlight plugin for the overlay code to actually highlight enemy NPCs on hover (https://github.com/runelite/runelite/tree/master/runelite-client/src/main/java/net/runelite/client/plugins/interacthighlight)
* TzHaar HP Tracker for code on how to check which map region the player is in (https://github.com/MoreBuchus/buchus-plugins/tree/tzhaar-hp-tracker)
