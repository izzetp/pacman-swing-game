package com.pacman.model;

import java.awt.Graphics2D;

/**
* Represents the Pac-Man maze map and handles rendering tiles using PNGs.
*/
public class Map {

    private final int TILE_SIZE;
    private final int COLS = 28;
    private final int ROWS = 36;
    private TileType[][] tiles;

    public Map(int tilesize) {
        this.TILE_SIZE = tilesize;
        initTiles();
    }

    private void initTiles() {
        tiles = new TileType[ROWS][COLS];
    
        // Temporary layout (walls at edges, pacdots inside)
        for (int row = 0; row < ROWS; row++) {
            for (int col = 0; col < COLS; col++) {
                if (row == 0 || row == ROWS - 1 || col == 0 || col == COLS - 1) {
                    tiles[row][col] = TileType.WALL;
                } else {
                    tiles[row][col] = TileType.PACDOT;
                }
            }
        }
    }
    
    public TileType getTile(int row, int col) {
        return tiles[row][col];
    }

    public void draw(Graphics2D g2, TileSet tileSet) {
        for(int row = 0; row < ROWS; row++) {
            for (int col = 0; col < COLS; col++) {
                g2.drawImage(tileSet.getImage(tiles[row][col]), col * TILE_SIZE, row * TILE_SIZE, TILE_SIZE, TILE_SIZE, null);
            }
        }
    }
}
