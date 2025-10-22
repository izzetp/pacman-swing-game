package com.pacman.logic;

import com.pacman.model.Map;
import com.pacman.model.TileType;

/** Handles player pickups (pellets & power pellets). */
public final class PlayerPickupSystem {
  private PlayerPickupSystem() {}

  /**
   * Eat whatever is on (tileX, tileY).
   * @return points awarded (10 for pellet, 50 for power pellet), 0 otherwise.
   */
  public static int eatAt(Map map, int tileX, int tileY) {
    // Map stores by (row, col) = (y, x)
    TileType t = map.getTile(tileY, tileX);
    if (t == TileType.PACDOT) {
      map.setTile(tileY, tileX, TileType.EMPTY);
      return 10;
    }
    if (t == TileType.POWER_PACDOT) {
      map.setTile(tileY, tileX, TileType.EMPTY);
      return 50;
    }
    return 0;
  }
}
