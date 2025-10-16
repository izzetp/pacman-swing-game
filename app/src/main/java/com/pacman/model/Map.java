package com.pacman.model;

import java.awt.Graphics2D;

public class Map {
    private final int TILE_SIZE;
    private final int COLS;
    private final int ROWS;
    private final TileType[][] tiles;

    public Map(TileType[][] tiles, int tileSize) {
        if (tiles == null || tiles.length == 0 || tiles[0].length == 0) {
            throw new IllegalArgumentException("tiles must be non-empty");
        }
        this.ROWS = tiles.length;
        this.COLS = tiles[0].length;
        this.tiles = tiles;
        this.TILE_SIZE = tileSize;
    }

    public int rows() { return ROWS; }
    public int cols() { return COLS; }
    public int tileSize() { return TILE_SIZE; }

    public boolean isWalkable(int col, int row) {
        if (row < 0 || row >= ROWS || col < 0 || col >= COLS) return false;
        TileType t = tiles[row][col];
        return switch (t) {
            case EMPTY, PACDOT, POWER_PACDOT, GHOST_GATE -> true;
            case WALL -> false;
        };
    }

    public int countPellets() {
        int count = 0;
        for (int r=0; r<ROWS; r++) for (int c=0; c<COLS; c++) {
            if (tiles[r][c] == TileType.PACDOT) count++;
            else if (tiles[r][c] == TileType.POWER_PACDOT) count++;
        }
        return count;
    }

    public TileType getTile(int row, int col) { return tiles[row][col]; }

    public void setTile(int row, int col, TileType type) { tiles[row][col] = type; }

    public void draw(Graphics2D g2, TileSet tileSet) {
        for (int row = 0; row < ROWS; row++) {
            for (int col = 0; col < COLS; col++) {
                g2.drawImage(
                    tileSet.getImage(tiles[row][col]),
                    col * TILE_SIZE, row * TILE_SIZE,
                    TILE_SIZE, TILE_SIZE, null
                );
            }
        }
    }
}
