package com.pacman.logic;

import com.pacman.model.Map;
import com.pacman.model.TileType;

public class GameSession {

  public enum State { MENU, PLAYING, GAME_OVER }

  private final TileType[][] levelSnapshot;
  private final int spawnX, spawnY;
  private Map currentMap;
  private State state = State.MENU;
  private int lives = 0; 

  public GameSession(Map initialMap, int spawnX, int spawnY) {
    this.currentMap = initialMap;
    this.spawnX = spawnX;
    this.spawnY = spawnY;
    this.levelSnapshot = deepCopy(initialMap);
  }

  public void start() {
    if (state == State.MENU) {
      lives = 3;
      restoreLevel();
      state = State.PLAYING;
    }
  }

  public void loseLife() {
    if (state != State.PLAYING) return;
    lives--;
    if (lives <= 0) {
      lives = 0;
      state = State.GAME_OVER;
    } else {
      restoreLevel(); 
    }
  }

  public void restart() {
    if (state == State.GAME_OVER) {
      lives = 3;
      restoreLevel();
      state = State.PLAYING;
    }
  }

  public State state() { return state; }
  public int lives() { return lives; }
  public Map map() { return currentMap; }
  public int spawnTileX() { return spawnX; }
  public int spawnTileY() { return spawnY; }

  private void restoreLevel() {
    for (int r = 0; r < currentMap.rows(); r++) {
      for (int c = 0; c < currentMap.cols(); c++) {
        currentMap.setTile(r, c, levelSnapshot[r][c]);
      }
    }
  }

  private static TileType[][] deepCopy(Map map) {
    TileType[][] copy = new TileType[map.rows()][map.cols()];
    for (int r = 0; r < map.rows(); r++) {
      for (int c = 0; c < map.cols(); c++) {
        copy[r][c] = map.getTile(r, c);
      }
    }
    return copy;
  }
}
