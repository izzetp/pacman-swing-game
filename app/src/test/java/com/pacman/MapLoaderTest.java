package com.pacman;

import org.junit.jupiter.api.Test;

import com.pacman.model.Map;
import com.pacman.model.TileType;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;

class MapLoaderTest {

    @Test
    void loadsTextMapCorrectly() throws IOException {
        // Arrange: create a small test map file
        File temp = File.createTempFile("pacmap", ".txt");
        try (FileWriter fw = new FileWriter(temp)) {
            fw.write("#####\n");
            fw.write("#...#\n");
            fw.write("#.o.#\n");
            fw.write("# G #\n");
            fw.write("#####\n");
        }

        // Act
        Map map = MapLoader.fromTextFile(temp.getAbsolutePath(), 8);

        // Assert dimensions
        assertEquals(5, map.rows());
        assertEquals(5, map.cols());

        // Assert correct tile types
        assertEquals(TileType.WALL, map.getTile(0,0));  // '#'
        assertEquals(TileType.PACDOT, map.getTile(1,1)); // '.'
        assertEquals(TileType.POWER_PACDOT, map.getTile(2,2)); // 'o'
        assertEquals(TileType.GHOST_GATE, map.getTile(3,2)); // 'G'
        assertTrue(map.isWalkable(1,1));
        assertFalse(map.isWalkable(0,0));
    }
}
