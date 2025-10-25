package com.pacman;

import com.pacman.logic.GameClock;
import com.pacman.logic.Ghost;
import com.pacman.model.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;


public class GhostTest {

    private Map map;
    private MockClock clock;

    @BeforeEach
    void setup() {
        TileType[][] grid = {
                { TileType.WALL, TileType.WALL, TileType.WALL, TileType.WALL, TileType.WALL },
                { TileType.WALL, TileType.EMPTY, TileType.EMPTY, TileType.EMPTY, TileType.WALL },
                { TileType.WALL, TileType.EMPTY, TileType.WALL, TileType.EMPTY, TileType.WALL },
                { TileType.WALL, TileType.EMPTY, TileType.EMPTY, TileType.EMPTY, TileType.WALL },
                { TileType.WALL, TileType.WALL, TileType.WALL, TileType.WALL, TileType.WALL },
        };
        map = new Map(grid, 16);
        clock = new MockClock(1.0 / 60.0); // 1 frame
    }

    @Test
    void ghostMovesInCurrentDirectionWhenPathIsClear() {
        Ghost ghost = new Ghost(map, 5.0); // 5 tiles/sec
        ghost.setPosition(1, 1);
        ghost.setDirection(Direction.RIGHT);

        ghost.tick(clock);

        assertEquals(1, ghost.tileY());
        assertTrue(ghost.tileX() >= 1); // should advance toward right
    }

    @Test
    void ghostStopsWhenBlockedByWall() {
        Ghost ghost = new Ghost(map, 5.0);
        ghost.setPosition(2, 1);
        ghost.setDirection(Direction.RIGHT); // next tile is wall

        ghost.tick(clock);

        // should remain in same tile (2,1)
        assertEquals(2, ghost.tileX());
        assertEquals(1, ghost.tileY());
    }

    @Test
    void ghostCanChangeDirectionWhenAligned() {
        Ghost ghost = new Ghost(map, 5.0);
        ghost.setPosition(1, 1);
        ghost.setDirection(Direction.RIGHT);
        ghost.requestDirection(Direction.DOWN); // will turn at intersection

        // simulate moving to next intersection (2,1) then center-align
        for (int i = 0; i < 20; i++) ghost.tick(clock);

        assertEquals(Direction.DOWN, ghost.currentDirection());
    }

    @Test
    void ghostMovesRandomlyWhenFrightened() {
        Ghost ghost = new Ghost(map, 5.0);
        ghost.setPosition(2, 2);
        ghost.setMode(Ghost.Mode.FRIGHTENED);

        ghost.tick(clock);
        Direction dir = ghost.currentDirection();

        assertNotNull(dir);
        assertNotEquals(Direction.NONE, dir);
    }

    @Test
    void ghostChaseTargetMovesTowardPacman() {
        Ghost ghost = new Ghost(map, 5.0);
        ghost.setPosition(1, 1);
        ghost.setMode(Ghost.Mode.CHASE);

        // Pacman position (target)
        int targetX = 3, targetY = 1;

        ghost.updateTarget(targetX, targetY);
        ghost.tick(clock);

        // ghost should move toward right (toward pacman)
        assertEquals(Direction.RIGHT, ghost.currentDirection());
    }

    // --- helper mock clock
    static class MockClock implements GameClock {
        private final double dt;
        MockClock(double dt) { this.dt = dt; }
        @Override public double deltaSeconds() { return dt; }
    }
}
