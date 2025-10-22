package com.pacman;

import com.pacman.logic.PlayerPickupSystem;
import com.pacman.model.Map;
import com.pacman.model.TileType;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class PlayerPickupSystemTest {

  private Map mapWith(int cols, int rows) {
    TileType[][] grid = new TileType[rows][cols];
    for (int r = 0; r < rows; r++) {
      for (int c = 0; c < cols; c++) grid[r][c] = TileType.EMPTY;
    }
    return new Map(grid, 8);
  }

  @Test
  void eatingPelletClearsTileAndAwards10() {
    Map map = mapWith(5, 5);
    map.setTile(2, 2, TileType.PACDOT);

    int points = PlayerPickupSystem.eatAt(map, 2, 2);

    assertEquals(10, points, "Normal pellet must award 10");
    assertEquals(TileType.EMPTY, map.getTile(2, 2), "Pellet must be cleared");
    // eat once only
    assertEquals(0, PlayerPickupSystem.eatAt(map, 2, 2));
  }

  @Test
  void eatingPowerPelletClearsTileAndAwards50() {
    Map map = mapWith(5, 5);
    map.setTile(1, 3, TileType.POWER_PACDOT);

    int points = PlayerPickupSystem.eatAt(map, 3, 1);

    assertEquals(50, points, "Power pellet must award 50");
    assertEquals(TileType.EMPTY, map.getTile(1, 3));
  }
}
