package com.pacman;

import com.pacman.logic.MovementSystem;
import com.pacman.logic.GameClock;
import com.pacman.model.Direction;
import com.pacman.model.Map;
import com.pacman.model.TileType;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class GridAlignmentTest {

    private static class FixedClock implements GameClock {
        private final double dt;
        FixedClock(double dt) { this.dt = dt; }
        @Override public double deltaSeconds() { return dt; }
    }

    private Map openMap(int cols, int rows) {
        TileType[][] grid = new TileType[rows][cols];
        for (int r=0; r<rows; r++) for (int c=0; c<cols; c++) grid[r][c]=TileType.PACDOT;
        // place a wall ahead at (3,2) to test blocking
        grid[2][3] = TileType.WALL;
        return new Map(grid, 8);
    }

    @Test
    void turnOnlyWhenAlignedAndNextWalkable() {
        Map map = openMap(6,5);
        MovementSystem ms = new MovementSystem(map, 1.0); // 1 tile per second
        ms.setPosition(2,2); // centered on (2,2)
        ms.request(Direction.RIGHT); // initially moving right
        ms.tick(new FixedClock(1.0)); // move to (3,2)? but (3,2) is WALL -> should not move
        assertEquals(2, ms.tileX());
        assertEquals(2, ms.tileY());

        ms.request(Direction.LEFT); // legal turn opposite at center
        ms.tick(new FixedClock(1.0)); // should go to (1,2)
        assertEquals(1, ms.tileX());
        assertEquals(2, ms.tileY());
    }

    @Test
    void cannotTurnEarlyEvenIfKeyHeld() {
        Map map = openMap(10,5);
        MovementSystem ms = new MovementSystem(map, 2.0); // 2 tiles per sec
        ms.setPosition(2,2);
        ms.request(Direction.UP); // queued
        // half tick: not aligned yet -> no turn
        ms.tick(new FixedClock(0.25)); // moves half tile along NONE -> stays aligned
        assertEquals(2, ms.tileX());
        assertEquals(2, ms.tileY());
        // Only at center can the turn be committed; verify it does after full step
        ms.tick(new FixedClock(0.25));
        assertEquals(2, ms.tileX());
        assertEquals(1, ms.tileY());
    }

    @Test
    void speedInvariant_tilesPerTickAtOneTilePerTick() {
        Map map = openMap(20,3);
        MovementSystem ms = new MovementSystem(map, 1.0);
        ms.setPosition(1,1);
        ms.request(Direction.RIGHT);
        for (int i=0;i<5;i++) ms.tick(new FixedClock(1.0));
        assertEquals(6, ms.tileX()); // moved exactly 5 tiles
        assertEquals(1, ms.tileY());
    }
}
