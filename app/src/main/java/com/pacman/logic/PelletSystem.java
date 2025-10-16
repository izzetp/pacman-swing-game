package com.pacman.logic;

public final class PelletSystem {
  private PelletSystem() {}
  /** Returns points and clears the cell. */
  public static int eatAt(char[][] grid, int x, int y) {
    char ch = grid[y][x];
    if (ch=='.') {
         grid[y][x]=' '; return 10; 
    }
    if (ch=='o') { 
        grid[y][x]=' '; return 50; 
    }
    return 0;
  }
}
