# AGENTS.md

Compact, AI-oriented map of this project. Read this before the source tree.
The code is the source of truth; update this file when behavior changes.

## What this is

"Little Room" is a small Java 2D top-down, room-based game inspired by The
Binding of Isaac. One playable character, directional auto-fire, melee-style
active items, familiars, and a linear dungeon of rooms with collectible school
items. Pure Java + Swing/AWT, no external dependencies, no build tool, no test
framework.

- Language level: Java 17+
- Package root: `game` (all classes in `src/main/java/game`)
- Entry point: `game.Main` -> `GamePanel` -> `Dungeon`
- Window/room size: 800x600, non-resizable
- Assets: PNGs in `src/main/resources/sprites`

## Build and run (PowerShell, from repo root)

```powershell
New-Item -ItemType Directory -Force out | Out-Null
javac -d out src\main\java\game\*.java
Copy-Item -Recurse -Force src\main\resources\* out
java -cp out game.Main
```

- `out/` is generated and not gitignored; do not commit it.
- Resources must be copied for the packaged run. `Sprites` also falls back to
  `src/main/resources/sprites` relative to the working directory, which is why
  running from the repo root works even without the copy step.

## Architecture at a glance

```
Main -> GamePanel (Swing Timer, 16 ms ~= 60 fps)
          |  reads pressed keys -> movement/aim/shoot/activate
          v
       Dungeon (list of Room, currentRoomIndex, door transitions)
          |
          v
        Room (background image, enemies, shots, item pedestals, poison clouds)
          |-- Gilberto (enemy, A* grid pathfinding toward player)
          |-- Antonio (ranged enemy that advances when outside firing range)
          |-- Shot (velocity-based projectile)
          |-- EnemyShot (Antonio's damaging projectile)
          |-- ItemDisplay (SchoolItem + position + collected flag)
          |-- PoisonCloud (damage-over-time area)
          v
       PlayerCharacter (stats, shooting, active items, companions, sprites)
```

- `GamePanel.tick()` clamps delta time to 0.05 s, updates the Dungeon, repaints.
- Input is a `Set<String>` of pressed keys; arrow keys set aim and auto-fire
  while held; **Space** fires active items once per key press.
- `Dungeon` is index-based: `currentRoomIndex = 1` at startup (room 1); index 0
  is the item gallery to its left.

## File map

| File | Responsibility |
| --- | --- |
| `Main.java` | Creates the Swing frame and `GamePanel`. |
| `GamePanel.java` | Game loop, key bindings, HUD text/bars, room label, item info, cooldown bars. |
| `Dungeon.java` | Builds the room list (gallery + 15 rooms), handles door transitions and room bookkeeping. |
| `Room.java` | Per-room simulation and drawing: 16 px A* grid for enemies, shots, items, poison, prerendered background; also updates Antonio and enemy shots. |
| `RoomType.java` | `NORMAL`, `TREASURE`, `GALLERY` (+ display names). |
| `GameEntity.java` | Base: center position `x/y`, `width/height`, `setPosition`, abstract `update`/`draw`. |
| `CharacterEntity.java` | Health, max health, move speed, damage, attack speed, movement clamping, stat mutations with validation. |
| `PlayerCharacter.java` | Player stats/actions: shooting, active items, cooldowns, drone, cat, slash visuals. |
| `Gilberto.java` | Enemy: chases via `Room.getNextWaypoint`, contact damage, sleep/root status, shot handling. |
| `Antonio.java` | Ranged enemy: 1-3 per normal room, random spawn positions, approaches beyond firing range, no contact damage. |
| `EnemyShot.java` | Antonio's projectile: deals damage to the player and disappears outside the room. |
| `Shot.java` | Projectile: velocity, size, syringe/sleep/root flags, sprite drawing (`syringe` drawn pink). |
| `SchoolItem.java` | Abstract item + 27 nested item classes, rarity, stat modifiers, special effects, random pools. |
| `CompanionCat.java` | Familiar that follows the player and melees nearby enemies (not a `GameEntity`). |
| `DroneFamiliar.java` | Orbiting familiar that shoots the nearest enemy (not a `GameEntity`). |
| `PoisonCloud.java` | Temporary poison area spawned by the Malagetas item. |
| `Sprites.java` | Loads/caches all PNGs; directional frame lookup; classpath-then-source fallback. |
| `Facing.java` | `DOWN`, `UP`, `LEFT`, `RIGHT` sprite direction keys. |

## Dungeon and rooms

- 15 numbered rooms (1..15) plus gallery room number 0 at the far left.
- Player spawns in room 1; walk left once to reach the gallery.
- Every 5th room (5, 10, 15) is `TREASURE`: treasure rug + pedestal baked into
  the background, one random item displayed at `(width/2, height/2 + 60)`.
- Rooms with `number > 1` spawn 1..5 Gilbertos and 1..3 Antonios at random
  positions; treasure
  rooms are excluded. Gallery and room 1 have no enemies.
- Doors: `number < roomCount` gives a right door; every numbered room also has
  a left door. Gallery has only a right door.
- `Dungeon.moveThroughDoor` clears shots (and poison) on both rooms, changes
  index by +/-1, and places the player just inside the opposite door.
- `getRoomCount()` includes the gallery; use `getNumberedRoomCount()` for the
  "Room N / 15" HUD label.
- `Room` prerenders floors/walls/doors/treasure decor once in `createBackground()`;
  runtime drawing adds entities, items, and poison on top.
- Gallery: 27 items in a centered 7-column grid (rows: 7/7/7/6), 64 px scaled
  pedestals, rarity-colored icon, 10 pt name label, vertical layout centered in
  the room. Items are collected on contact and do not respawn.

## Player, enemy, and room constants

| Value | Player | Gilberto | Antonio |
| --- | --- | --- | --- |
| Size | 20x29 hitbox | 20x31 hitbox | 20x31 hitbox |
| Health | 100 | 100 | 100 |
| Move speed | 220 u/s | 165 u/s | 90 u/s when beyond range |
| Damage | 10 per shot | 1 contact damage (1 s cooldown) | 1 per projectile |
| Attack speed | 3 shots/s | unused (3) | one shot every 1.25 s |

- Shot: speed 420 u/s, default size 10 px (`SHOT_SIZE`), sprite drawn scaled.
- Antonio approaches when farther than 350 px; `EnemyShot` speed is 260 u/s.
- Room: 800x600; wall thickness 20; door half-height 48; A* cell size 16 px.
- Walk animation runs at 8 fps (player) and 7 fps (enemy).
- Damage is an `int`; movement/positions are `double`; entity coordinates are
  centers, not top-left corners.

## Items

`SchoolItem` is an abstract class with nested `static final` item classes.
Rarity tiers (name + color) are: `COMUM` gray, `INCOMUM` green, `RARO` blue,
`LENDARIO` gold. Items apply additive and multiplicative stat changes plus an
optional `applySpecialEffect(PlayerCharacter)` hook.

Rarity roll in `randomTreasureItem()`: 50% common, 30% uncommon, 15% rare,
5% legendary.

| Rarity | Items and effects |
| --- | --- |
| Comum | Caneta +2 damage; Lápis +10 speed; Borracha +0.5 attack/s; Mala cosmetic backpack; Gordura +20 max HP, -10% speed; Joelho fudido +25% damage, -10% speed; Óculos +50% projectile size, -10% speed, -20 max HP; Pantufas every 3 hits sleeps an enemy 2 s |
| Incomum | Régua +3 damage; Caderno +20 speed; Lasagna pingodoçe +50% damage, -20 max HP; Halteres +50% damage; Katana active frontal slash; Cabelo shots root 2 s; Capoeira every 3rd shot does a 64 px spin at 1.5x damage; Malagetas leaves poison while walking; Familiar de IA cat companion |
| Raro | Estojo +4 damage, +0.5 attack/s; Calculadora +1 damage, +1 attack/s; Pistola do Dred 2x damage, 0.6x attack speed; Token pirata no effect; SQL Injection 10% syringe instant kill; Familiar drone orbiting shooter; Razorblade +1 attack/s and paid slash; Fones shockwave; Bolso random item |
| Lendário | Mochila +5 damage, +20 speed, +1 attack/s |

Active items (fired together by **Space**, each with its own cooldown):

| Item | Effect | Cooldown |
| --- | --- | --- |
| Katana | Frontal slash, 76 px range, 120° arc, player damage | 0.6 s |
| Razorblade | Frontal slash, 92 px range, 180° arc, 3x damage, costs 5 HP (never below 1) | 5 s |
| Fones | Shockwave, 140 px radius, 2x damage | 8 s |
| Bolso | Applies a random passive item from `randomPocketItem()` | 15 s |

Other systems:

- SQL Injection: 10% of shots become syringes; a syringe kills on hit.
- Pantufas: sleep hit counter per enemy; 3 hits => 2 s sleep (no contact damage).
- Cabelo: every shot roots the enemy for 2 s.
- Capoeira: every 3rd shot sets a spin flag; `Room` applies the AoE.
- Malagetas: while moving >= 2 px, spawn a cloud every 0.3 s; cloud radius 30,
  lifetime 3 s, 2 damage every 0.75 s.
- Drone: orbits at 42 px, 2.2 rad/s, shoots nearest enemy every 0.8 s at 320 u/s.
- Cat: follows 34 px behind-left, 260 u/s, 8 damage every 1 s within 38 px.

Item pools:

- `SchoolItem.allItems()`: all 27 items (used by the gallery).
- `SchoolItem.randomTreasureItem()`: weighted rarity roll.
- `SchoolItem.randomPocketItem()`: 16 passive items for Bolso.
- When adding an item: create the subclass, then add it to `randomTreasureItem()`
  and `allItems()` (and `randomPocketItem()` if it is passive).

## Sprites and resource layout

```
src/main/resources/sprites/
  characters/player/   player_{down,up,left,right}_{0,1}.png   32x32
  characters/enemy/    enemy_{down,up,left,right}_{0,1}.png    32x32
  environment/         floor_normal.png, floor_treasure.png    40x40
                       wall_horizontal.png                    40x20
                       wall_vertical.png                      20x40
                       door.png                               20x96
  props/               pedestal.png                           96x96
                       treasure_rug.png                       240x144
  projectiles/         shot.png                               16x16
  ui/                  hud_bar_background.png, hud_bar_fill.png 160x14
```

- `Sprites` is package-private and static: constants for single images and
  `EnumMap<Facing, BufferedImage[]>` for 2-frame walk cycles.
- `Sprites.player(facing, frame)` / `Sprites.enemy(facing, frame)` wrap frames
  with `Math.floorMod`.
- To add art: place the PNG in the right subfolder and add a `load(...)` entry
  (and a frame array entry if directional/animated). Missing files throw with
  the expected path.
- Sprites are loaded via `getResourceAsStream("/sprites/<name>")`, then fall
  back to the source-resources path.

## HUD (drawn over the room each frame)

Fixed coordinates in `GamePanel` (800x600): title (16,26); room label right
aligned at x=784, y=26; controls y=48; room type right aligned y=48 (treasure
only); shot count y=68; health bar at (16,82) using the 160x14 sprite; stats
y=118; highlighted item line y=142; active-item cooldown bars starting at
(16,164), 17 px per row, 140 px wide.

The top ~150 px of the room is visually covered by HUD text, so keep important
room content below it (the gallery grid starts around y=190 at 600 px height).

## Conventions and invariants

- Keep the game dependency-free: JDK only (Swing/AWT, `java.awt.image`,
  `javax.imageio`).
- Style: 4-space indent, K&R braces, ~80-column lines, no wildcard imports,
  `final` fields where possible, braces even for one-line `if`s.
- Validate inputs in constructors/setters with `IllegalArgumentException`
  (positive sizes/speeds, non-negative damage, positive multipliers).
- Entities are updated with `deltaSeconds`; never assume a fixed frame rate.
- Enemies are `Gilberto`; `Room` owns per-room `List<Gilberto>`, `List<Shot>`,
  and poison clouds. Remove dead enemies with `removeIf(...)` or iterator
  removal, never while iterating with a for-each.
- `Room` uses iterator removal for shots/clouds; keep that pattern.
- Room layouts are computed from room width/height where practical; the gallery
  grid adapts horizontally/vertically.
- New room types: add to `RoomType`, handle in `Room` (constructor + background
  + draw), place it in `Dungeon`, and adjust the HUD label.
- New active item: add cooldown field + trigger in `PlayerCharacter.activateItems`,
  expose it in `getActiveItemCooldowns()` so the HUD shows a bar.
- New companion: follow the `CompanionCat`/`DroneFamiliar` pattern: owned by
  `PlayerCharacter`, updated from `Room.update`, drawn from `PlayerCharacter.draw`.
- README is player-facing; this file is agent-facing. Keep both current.

## Verification

There is no automated test suite. Minimum check after a change:

```powershell
javac -d out src\main\java\game\*.java
```

For logic/navigation checks, a headless harness works well: construct the
`Dungeon`, set movement/aim, call `update(1/60.0)` in a loop, and assert on room
type, positions, health, or item effects. For visual checks, render a `Room`
(or a whole frame) into a `BufferedImage` and write it with `ImageIO`:

```java
Room room = new Room(800, 600, 0, RoomType.GALLERY, false, true, player);
BufferedImage image = new BufferedImage(800, 600, BufferedImage.TYPE_INT_ARGB);
Graphics2D g = image.createGraphics();
room.draw(g);
g.dispose();
ImageIO.write(image, "png", new File("preview.png"));
```

This requires running from the repo root so the sprite source fallback resolves.
The images can then be inspected directly.

## Gotchas

- `Room.currentRoomIndex` starts at 1 (spawn room); gallery is index 0.
- Door transitions are index-based, not number-based; the gallery is not
  numbered and must not be counted in "Room N / 15".
- `Room.background` is cached at construction. Changes to floor/wall/door/
  treasure-decor drawing need a new `Room` instance to show up.
- Treasure items use `ItemDisplay` positions; gallery items are drawn at the
  same position with a pedestal sprite scaled to 64 px, while treasure decor is
  baked into the background.
- Item pickup is contact-based (within `player size/2 + 16`), including the
  gallery; walking through the gallery collects items.
- Level transitions clear shots and poison clouds but not item effects.
- The HUD activation key is **Space**; only the currently equipped activatable
  item is triggered. Picking up another activatable item drops the old one.
- The katana is not an activatable item: it replaces player shots with
  directional melee attacks while an arrow key is held.
- `SchoolItem` already declares `isActivatable()`, `getCooldownSeconds()`, and
  `activate(player, enemies)`. The live activation path is
  `GamePanel` -> `Dungeon.activateItems` -> `Room.activateItems` ->
  `PlayerCharacter.activateItems`; the player owns the active-item slot and
  its cooldown state.
