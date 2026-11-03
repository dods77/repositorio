# Little Room

A tiny Java 2D game starter inspired by top-down room-based games. It has one
playable character and directional shots. The dungeon is a line of 15 rooms:
you start in the leftmost room and move right through side doors. Every fifth
room (5, 10, and 15) is a treasure room with an empty pedestal.

Normal rooms are guarded by Gilberto, an enemy that chases the player, deals
contact damage, and can be defeated with shots.

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
`src/main/resources/sprites`. The build step above copies them into `out` so
they are on the runtime classpath.

## Controls

- **WASD**: Move
- **Hold arrow keys**: Shoot in that direction (combine two for diagonal shots)

Walk into the gap in the left or right wall to move to the next room. Shots do
not carry between rooms.

## Character attributes

The player starts with 100 HP, moves at 220 units per second, deals 10 damage
per shot, and fires 3 shots per second. HP, speed, damage, and attack speed
(shots per second) are reusable character attributes. Gilberto starts with
100 HP, moves at 165 units per second, and deals 1 contact damage per second.

## Structure

- `GameEntity`: shared base for objects updated and drawn by the game.
- `CharacterEntity`: reusable movement base for the player and enemies.
- `PlayerCharacter`: the current playable character.
- `Gilberto`: the chasing enemy found in normal rooms.
- `Shot`: a projectile with its own movement and drawing behavior.
- `Room`: composites the floor, walls, doors, and pedestal sprites, then draws
  the player, enemy, and active shots.
- `Dungeon`: builds the line of rooms and handles moving between them.
- `RoomType`: identifies normal and treasure rooms.
- `Sprites` and `Facing`: load the PNG sprite set and select the player and
  enemy directional walk frames.
- `GamePanel` and `Main`: Swing window, controls, and game loop.

This keeps characters, projectiles, dungeon layout, and room state separate so
more characters, enemies, and room types can be added without putting their
behavior into the game window.
