# Little Room

A tiny Java 2D game starter inspired by top-down room-based games. It currently
has one playable character and directional shots; there are no enemies or
win/lose objectives yet.

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

## Character attributes

The player starts with 100 HP, moves at 220 units per second, deals 10 damage
per shot, and fires 5 shots per second. HP, speed, damage, and attack speed
(shots per second) are reusable character attributes; damage is not yet applied
to anything because enemies are not implemented.

## Structure

- `GameEntity`: shared base for objects updated and drawn by the game.
- `CharacterEntity`: reusable movement base for player characters and future
  enemy characters.
- `PlayerCharacter`: the current playable character.
- `Shot`: a projectile with its own movement and drawing behavior.
- `Room`: owns the player and active shots, and draws the play area.
- `GamePanel` and `Main`: Swing window, controls, and game loop.

This keeps characters, projectiles, and room state separate so more characters,
enemies, and room types can be added without putting their behavior into the
game window.
