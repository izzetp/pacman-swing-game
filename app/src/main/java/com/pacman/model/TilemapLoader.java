package com.pacman.model;

import java.awt.Color;
import java.awt.image.BufferedImage;

public final class TilemapLoader {
    private TilemapLoader() {}

    // Legend 
    private static final Color WALL    = new Color(0, 0, 255);
    private static final Color PELLET  = new Color(255, 255, 0);
    private static final Color POWER   = new Color(255, 165, 0);
    private static final Color GATE    = new Color(255, 0, 255);
    // Anything else -> EMPTY

    public static TileType[][] fromImage(BufferedImage img) {
        final int rows = img.getHeight();
        final int cols = img.getWidth();
        TileType[][] grid = new TileType[rows][cols];

        for (int r = 0; r < rows; r++) {
            for (int c = 0; c < cols; c++) {
                int argb = img.getRGB(c, r);
                Color color = new Color(argb, true);

                if (same(color, WALL))      grid[r][c] = TileType.WALL;
                else if (same(color, PELLET)) grid[r][c] = TileType.PACDOT;
                else if (same(color, POWER))  grid[r][c] = TileType.POWER_PACDOT;
                else if (same(color, GATE))   grid[r][c] = TileType.GHOST_GATE;
                else                           grid[r][c] = TileType.EMPTY;
            }
        }
        return grid;
    }

    private static boolean same(Color a, Color b) {
        return a.getAlpha()==b.getAlpha() && a.getRed()==b.getRed() && a.getGreen()==b.getGreen() && a.getBlue()==b.getBlue();
    }
}
