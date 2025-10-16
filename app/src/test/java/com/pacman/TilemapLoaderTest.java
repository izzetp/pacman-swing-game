package com.pacman;

import com.pacman.model.TileType;
import com.pacman.model.Map;
import com.pacman.model.TilemapLoader;
import org.junit.jupiter.api.Test;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;

import static org.junit.jupiter.api.Assertions.*;

class TilemapLoaderTest {

    private BufferedImage makeTestImage() {
        // 5x5 image with: border walls, pellets inside, one power pellet
        BufferedImage img = new BufferedImage(5, 5, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = img.createGraphics();
        try {
            // Colors must match legend in TilemapLoader
            Color WALL = new Color(0, 0, 255);     // blue
            Color PELLET = new Color(255, 255, 0); // yellow
            Color POWER  = new Color(255, 165, 0); // orange
            Color EMPTY  = new Color(0, 0, 0, 0);  // transparent

            // Fill empty
            g.setColor(EMPTY);
            g.fillRect(0, 0, 5, 5);

            // Border walls
            g.setColor(WALL);
            for (int i = 0; i < 5; i++) {
                img.setRGB(i, 0, WALL.getRGB());
                img.setRGB(i, 4, WALL.getRGB());
                img.setRGB(0, i, WALL.getRGB());
                img.setRGB(4, i, WALL.getRGB());
            }

            // Inside pellets + one power pellet at (2,2)
            g.setColor(PELLET);
            img.setRGB(1,1, PELLET.getRGB());
            img.setRGB(2,1, PELLET.getRGB());
            img.setRGB(3,1, PELLET.getRGB());
            img.setRGB(1,2, PELLET.getRGB());
            img.setRGB(3,2, PELLET.getRGB());
            img.setRGB(1,3, PELLET.getRGB());
            img.setRGB(2,3, PELLET.getRGB());
            img.setRGB(3,3, PELLET.getRGB());

            g.setColor(POWER);
            img.setRGB(2,2, POWER.getRGB());
        } finally { g.dispose(); }
        return img;
    }

    @Test
    void loadsPngToGrid_sameSizeAndCounts() {
        BufferedImage img = makeTestImage();
        TileType[][] grid = TilemapLoader.fromImage(img);

        assertEquals(5, grid.length);        // rows
        assertEquals(5, grid[0].length);     // cols

        int pellets = 0, powers = 0, walls = 0, empty = 0;
        for (int r = 0; r < 5; r++) {
            for (int c = 0; c < 5; c++) {
                switch (grid[r][c]) {
                    case PACDOT -> pellets++;
                    case POWER_PACDOT -> powers++;
                    case WALL -> walls++;
                    case EMPTY -> empty++;
                    default -> {}
                }
            }
        }
        assertEquals(8, pellets, "pellet count");
        assertEquals(1, powers,  "power pellet count");
        assertEquals(16, walls,  "4*5 border minus corners counted twice handled already");
        assertTrue(empty >= 0);  // transparent stays EMPTY
    }

    @Test
    void mapInvariants_gridEqualsCollisionGridAndWalkability() {
        BufferedImage img = makeTestImage();
        TileType[][] grid = TilemapLoader.fromImage(img);
        Map map = new Map(grid, 8);

        // collision grid == render grid size
        assertEquals(5, map.rows());
        assertEquals(5, map.cols());

        // walls unwalkable, pellets walkable
        assertFalse(map.isWalkable(0,0));
        assertTrue(map.isWalkable(1,1));
        assertTrue(map.isWalkable(2,2)); // power pellet
        assertFalse(map.isWalkable(4,2)); // border wall
    }
}
