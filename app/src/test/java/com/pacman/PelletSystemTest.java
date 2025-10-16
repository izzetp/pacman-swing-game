package com.pacman;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class PelletSystemTest {
  @Test void pelletOncePowerOnce() {
    char[][] g = {
      "WWWWW".toCharArray(),
      "W.o.W".toCharArray(),
      "W...W".toCharArray(),
      "W   W".toCharArray(),
      "WWWWW".toCharArray()
    };
    int score = 0;
    // eat pellet once
    score += eat(g, 1, 2);
    score += eat(g, 1, 2);
    assertEquals(10, score);
    // eat power pellet once
    score += eat(g, 2, 1);
    score += eat(g, 2, 1);
    assertEquals(60, score);
  }

  // will be replaced by production API in green phase
  private int eat(char[][] grid, int x, int y) {
    if (grid[y][x]=='.') { 
        grid[y][x]=' '; return 10; 
    }
    if (grid[y][x]=='o') { 
        grid[y][x]=' '; return 50; 
    }
    return 0;
  }
}
