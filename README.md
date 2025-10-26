# Pac-Man (CBL 2IP90 Assignment 5)

This project is a Pac-Man clone written entirely in Java using Swing.  

The game demonstrates object-oriented programming and test-driven development.  
It runs on Windows, macOS, and Linux.

---

## How to Run

### Option 1: Run the prebuilt JAR
1. Locate the file `app/build/libs/pacman-1.0.0.jar` (included in the submission zip or built by Gradle).
2. Double-click the JAR, or run it from a terminal:

   ```
   java -jar app/build/libs/pacman-1.0.0.jar
   ```

### Option 2: Build from source
If you prefer to build it yourself, make sure you have Java 21 or later installed.

```
./gradlew :app:clean :app:build
java -jar app/build/libs/pacman-1.0.0.jar
```

---

## Controls

| Key | Action |
|-----|---------|
| Enter | Start game or restart after Game Over |
| Arrow keys | Move Pac-Man |
| Esc | Quit the game |

---

## Gameplay Overview

- The maze is loaded from PNG tiles and a map file.  
- Collect pellets (+10 points) and power pellets (+50 points).  
- Power pellets activate Frightened Mode, where ghosts turn blue and flee.  
- The player starts with 3 lives. Colliding with a ghost in Chase or Scatter mode costs one life.  
- The Heads-Up Display shows score, remaining lives, and the frightened timer.  
- Game states: Menu -> Playing -> Win or Game Over.  
- The game starts quickly and is playable immediately after launch.

---

## Technical Details

| Category | Description |
|-----------|-------------|
| Language | Java 21 |
| UI Toolkit | Swing |
| Build System | Gradle (Kotlin DSL) |
| Main Class | `com.pacman.ui.Main` |
| Executable JAR | `app/build/libs/pacman-1.0.0.jar` |
| Game Loop | Fixed update loop |
| Collision System | Tile-based collision detection |
| Map Data | Text map with PNG tile resources |
| Testing | JUnit 5 |
| Continuous Integration | GitHub Actions (builds and tests automatically) |

---

## Topics of Choice

1. **Test-Driven Development (TDD)**  
   All core game systems such as map loading, collisions, ghost behavior, and scoring were built using unit tests in `app/src/test/java/com/pacman`.  
   Tests were written first, then code was implemented to make them pass.

2. **GitHub Actions (CI/CD)**  
   The file `.github/workflows/ci.yml` runs automated builds and tests on every push.  
   When a version tag is pushed, it also produces a runnable JAR artifact.

---

## Running Tests

Run all tests using:

```
./gradlew :app:test
```

After the tests finish, open  
`app/build/reports/tests/test/index.html`  
to view detailed results.

---

## Project Structure

```
pacman-swing-game/
│
├── app/
│   ├── src/
│   │   ├── main/java/com/pacman/...      (main source code)
│   │   ├── main/resources/tiles/...      (tile graphics)
│   │   ├── main/resources/maps/...       (map files)
│   │   └── test/java/com/pacman/...      (JUnit tests)
│   └── build.gradle.kts
│
├── .github/workflows/ci.yml
├── settings.gradle.kts
├── gradlew / gradlew.bat
├── gradle/wrapper/
└── README.md
```

---

## Product Backlog

See the file `Pac-Man Product Backlog (CBL 2IP90).pdf` for:

- Prioritized features and learning goals  
- How to demonstrate each feature  
- The two advanced topics selected for the assignment

---
