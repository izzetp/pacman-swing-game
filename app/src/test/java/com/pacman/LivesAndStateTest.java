package com.pacman;

import com.pacman.logic.GameSession;
import com.pacman.model.Map;
import com.pacman.model.TileType;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class LivesAndStateTest {

  private Map mapWithPellets() {
    TileType[][] grid = {
        {TileType.WALL, TileType.WALL, TileType.WALL},
        {TileType.WALL, TileType.PACDOT, TileType.WALL},
        {TileType.WALL, TileType.WALL, TileType.WALL}
    };
    return new Map(grid, 8);
  }

  @Test
  void startsInMenuAndGoesToPlayingOnStart() {
    GameSession s = new GameSession(mapWithPellets(), 0, 0, 1, 1);
    assertEquals(GameSession.State.MENU, s.state());
    s.start();
    assertEquals(GameSession.State.PLAYING, s.state());
    assertEquals(3, s.lives());
  }

  @Test
  void losingALifeResetsLevelAndKeepsPlayingUntilZero() {
    Map original = mapWithPellets();
    GameSession s = new GameSession(original, 0, 0, 1, 1);
    s.start();

    original.setTile(1,1, TileType.EMPTY);

    s.loseLife(); // -1 life + reset level
    assertEquals(2, s.lives());
    assertEquals(GameSession.State.PLAYING, s.state());

    assertEquals(TileType.PACDOT, s.map().getTile(1,1));
  }

  @Test
  void reachingZeroLivesSwitchesToGameOver() {
    GameSession s = new GameSession(mapWithPellets(), 0, 0, 1, 1);
    s.start();
    s.loseLife(); // 2
    s.loseLife(); // 1
    s.loseLife(); // 0 -> GAME_OVER
    assertEquals(0, s.lives());
    assertEquals(GameSession.State.GAME_OVER, s.state());
  }

  @Test
  void restartFromGameOverRestoresLivesAndLevelAndGoesToPlaying() {
    GameSession s = new GameSession(mapWithPellets(), 0, 0, 1, 1);
    s.start();
    s.loseLife(); s.loseLife(); s.loseLife(); // GAME_OVER

    s.map().setTile(1,1, TileType.EMPTY);

    s.restart();
    assertEquals(GameSession.State.PLAYING, s.state());
    assertEquals(3, s.lives());
    assertEquals(TileType.PACDOT, s.map().getTile(1,1));
  }
}
