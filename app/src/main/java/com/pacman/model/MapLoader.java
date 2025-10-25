package com.pacman.model;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Loads a Pac-Man map from a .txt file.
 * Example legend:
 *  # = wall
 *  . = pellet
 *  o = power pellet
 *  G = ghost gate
 *  (space) = empty
 */
public final class MapLoader {

    private MapLoader() {}

    public static Map fromTextFile(String path, int tileSize) throws IOException {
        List<String> lines = new ArrayList<>();

        try (BufferedReader br = new BufferedReader(new FileReader(path))) {
            String line;
            while ((line = br.readLine()) != null) {
                if (!line.trim().isEmpty()) lines.add(line);
            }
        }

        if (lines.isEmpty()) {
            throw new IllegalArgumentException("Map file is empty: " + path);
        }

        int rows = lines.size();
        int cols = lines.get(0).length();
        TileType[][] grid = new TileType[rows][cols];

        for (int r = 0; r < rows; r++) {
            String line = lines.get(r);
            for (int c = 0; c < cols; c++) {
                char ch = (c < line.length()) ? line.charAt(c) : ' ';
                grid[r][c] = switch (ch) {
                    case '#' -> TileType.WALL;
                    case '.' -> TileType.PACDOT;
                    case 'o' -> TileType.POWER_PACDOT;
                    case 'G', 'g' -> TileType.GHOST_GATE;
                    default -> TileType.EMPTY;
                };
            }
        }

        return new Map(grid, tileSize);
    }
}
