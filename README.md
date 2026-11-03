# Little Room

A tiny Java 2D game starter inspired by top-down room-based games. It has one
playable character and directional shots. The dungeon is a line of 15 rooms:
you start in the leftmost room and move right through side doors. Every fifth
room (5, 10, and 15) is a treasure room with an empty pedestal.

There are no enemies or win/lose objectives yet.

## Requirements

- Java 17 or newer

## Run on Windows (PowerShell)

From the repository root:

```powershell
New-Item -ItemType Directory -Force out
javac -d out src\main\java\game\*.java
java -cp out game.Main
```

## Controls

- **WASD**: Move
- **Hold arrow keys**: Shoot in that direction (combine two for diagonal shots)

Walk into the gap in the left or right wall to move to the next room. Shots do
not carry between rooms.

## Character attributes

The player starts with 100 HP, moves at 220 units per second, deals 10 damage
per shot, and fires 3 shots per second. HP, speed, damage, and attack speed
(shots per second) are reusable character attributes; damage is not yet applied
to anything because enemies are not implemented.

## Structure

- `GameEntity`: shared base for objects updated and drawn by the game.
- `CharacterEntity`: reusable movement base for player characters and future
  enemy characters.
- `PlayerCharacter`: the current playable character.
- `Shot`: a projectile with its own movement and drawing behavior.
- `Room`: owns the room layout, doors, player, and active shots.
- `Dungeon`: builds the line of rooms and handles moving between them.
- `RoomType`: identifies normal and treasure rooms.
- `GamePanel` and `Main`: Swing window, controls, and game loop.

This keeps characters, projectiles, dungeon layout, and room state separate so
more characters, enemies, and room types can be added without putting their
behavior into the game window.
