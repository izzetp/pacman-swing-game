package com.pacman;

import com.pacman.logic.PlayerPickupSystem;
import com.pacman.model.Map;
import com.pacman.model.TileType;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class PelletSystemTest {

    @Test
    void eatingPelletGives10AndClearsTile() {
        TileType[][] grid = {
                {TileType.WALL, TileType.WALL, TileType.WALL},
                {TileType.WALL, TileType.PACDOT, TileType.WALL},
                {TileType.WALL, TileType.WALL, TileType.WALL}
        };
        Map map = new Map(grid, 16);

        int score1 = PlayerPickupSystem.eatAt(map, 1, 1);
        int score2 = PlayerPickupSystem.eatAt(map, 1, 1); // second time should be zero

        assertEquals(10, score1);
        assertEquals(0, score2);
        assertEquals(TileType.EMPTY, map.getTile(1, 1));
    }

    @Test
    void eatingPowerPelletGives50AndClearsTile() {
        TileType[][] grid = {
                {TileType.WALL, TileType.WALL, TileType.WALL},
                {TileType.WALL, TileType.POWER_PACDOT, TileType.WALL},
                {TileType.WALL, TileType.WALL, TileType.WALL}
        };
        Map map = new Map(grid, 16);

        int score1 = PlayerPickupSystem.eatAt(map, 1, 1);
        int score2 = PlayerPickupSystem.eatAt(map, 1, 1);

        assertEquals(50, score1);
        assertEquals(0, score2);
        assertEquals(TileType.EMPTY, map.getTile(1, 1));
    }
}
