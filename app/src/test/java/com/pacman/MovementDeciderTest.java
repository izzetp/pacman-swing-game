package com.pacman;

import com.pacman.model.Direction;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import static org.junit.jupiter.api.Assertions.*;

class MovementDeciderTest {
  // Given current center tile, a requested direction, and whether the next tile is a wall
  // When deciding a turn, only accept if passable
  private static Direction decide(Direction requested, boolean nextIsWall) {
    boolean canTurn = !nextIsWall;
    return (requested != Direction.NONE && canTurn) ? requested : Direction.NONE;
  }

  @ParameterizedTest
  @CsvSource({
      "LEFT,false,LEFT",
      "RIGHT,true,NONE",
      "UP,false,UP",
      "DOWN,true,NONE",
      "NONE,false,NONE"
  })
  void turnsOnlyIntoPassableTiles(Direction requested, boolean wall, Direction expected) {
    assertEquals(expected, decide(requested, wall));
  }
}
