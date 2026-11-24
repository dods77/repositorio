# Little Room

A tiny Java 2D game starter inspired by top-down room-based games. It has one
playable character and directional shots. The dungeon is a line of 15 rooms:
you start at the left end and move right through side doors. A gallery room to
the left of the start displays every item on its own pedestal, like a museum
of everything the dungeon can drop. Every fifth room (5, 10, and 15) is a
treasure room with a collectible school item on its pedestal. Items have weighted Portuguese rarity tiers and permanently modify
damage, movement speed, attack speed, or maximum health when collected. Some
items also apply a cosmetic effect or have no gameplay effect.

Normal rooms after the first have 1-5 Gilbertos that chase and damage the player
on contact, plus 1-3 Antonios that approach if the player is out of range and
fire projectiles from random positions.
Both enemies can be defeated with shots. Treasure rooms have no enemies.

For a compact, AI-oriented map of the architecture, systems, and conventions,
see [`AGENTS.md`](AGENTS.md).

## Requirements

- Java 17 or newer

## Run on Windows (PowerShell)

From the repository root:

```powershell
New-Item -ItemType Directory -Force out | Out-Null
javac -d out src\main\java\game\*.java
Copy-Item -Recurse -Force src\main\resources\* out
java -cp out game.Main
```

The game art is rendered from PNG sprites in
`src/main/resources/sprites`, grouped by what they are:

- `characters/player`: the player walk frames
- `characters/enemy`: the enemy walk frames
- `environment`: floors, walls, and doors
- `props`: pedestal and treasure rug
- `projectiles`: shot sprite
- `effects`: melee attack slash sprite
- `ui`: HUD bar sprites

The build step above copies them into `out` so they are on the runtime
classpath.

## Controls

- **WASD**: Move
- **Hold arrow keys**: Shoot in that direction (combine two for diagonal shots);
  with the katana, slash in that direction instead
- **Hold the left mouse button**: Aim at the cursor and shoot continuously
- **Space**: Activate items that have an active ability

The katana replaces ranged shots: hold an arrow key to swing in that direction.
Picking up a different activatable item drops the currently held one nearby.

Walk into the gap in the left or right wall to move to the next room. Shots do
not carry between rooms. Walking left from the start room leads to the item
gallery; touch a pedestal's item to collect it.

Some items add melee attacks, root enemies, leave damaging poison clouds, or
grant a companion. The razorblade, headphones, and pocket item activate with
**Space**. The HUD shows the held active item's recharge bar, which turns green
when ready. The pocket item grants a random passive item and has a 15-second
cooldown; the razorblade costs 5 HP per activation but cannot reduce the player
below 1 HP. Melee attacks use an animated sweeping slash based on the sprite at
`src/main/resources/sprites/effects/melee_slash.png`.

## Character attributes

The player starts with 100 HP, moves at 220 units per second, deals 10 damage
per shot, and fires 3 shots per second. HP, speed, damage, and attack speed
(shots per second) are reusable character attributes. Gilberto starts with
100 HP, moves at 165 units per second, and deals 1 contact damage per second.
Antonio has 100 HP, approaches at 90 units per second when the player is more
than 350 units away, and fires 1-damage shots every 1.25 seconds while within
range. He deals no contact damage.

## Structure

- `GameEntity`: shared base for objects updated and drawn by the game.
- `CharacterEntity`: reusable movement base for the player and enemies.
- `PlayerCharacter`: the current playable character.
- `Gilberto`: the chasing enemy found in normal rooms.
- `Antonio`: ranged enemy found in normal rooms after room 1.
- `EnemyShot`: a projectile fired by Antonio that damages the player.
- `Shot`: a projectile with its own movement and drawing behavior.
- `Room`: composites the floor, walls, doors, and pedestal sprites, then draws
  the player, enemy, and active shots.
- `Dungeon`: builds the line of rooms and handles moving between them.
- `RoomType`: identifies normal, treasure, and gallery rooms.
- `SchoolItem`: shared item behavior and nested classes for each treasure item.
- `DroneFamiliar`: orbital ranged companion granted by the drone item.
- `CompanionCat`: follows the player and attacks nearby enemies.
- `PoisonCloud`: temporary damaging area left by the poison-trail item.
- `Sprites` and `Facing`: load the PNG sprite set and select the player and
  enemy directional walk frames.
- `GamePanel` and `Main`: Swing window, controls, and game loop.

This keeps characters, projectiles, dungeon layout, and room state separate so
more characters, enemies, and room types can be added without putting their
behavior into the game window.
